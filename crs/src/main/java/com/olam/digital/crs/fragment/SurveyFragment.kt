// fragment/SurveyFragment.kt
package com.olam.digital.crs.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.gson.Gson
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.FragmentSurveyCrsBinding
import com.olam.digital.crs.dynamicAdapter.CrsDynamicQustionsAdapter
import com.olam.digital.crs.extension.CrsSkipLogicEvaluator
import com.olam.digital.crs.extension.ensureFocusedViewAboveKeyboard
import com.olam.digital.crs.extension.getSecureAndroidId
import com.olam.digital.crs.extension.longToast
import com.olam.digital.crs.extension.showOrHide
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.room.ConditionalQuestionDetailEntity
import com.olam.digital.crs.room.ConditionalRule
import com.olam.digital.crs.room.CrsRepeaterMappingEntity
import com.olam.digital.crs.room.conditionalRules
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class SurveyFragment : Fragment() {

    private var _binding: FragmentSurveyCrsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrsQuestionaryViewModel by activityViewModels()
    private lateinit var adapter: CrsDynamicQustionsAdapter

    // Pending ids for actions
    private var pendingPhotoQuestionId: Long? = null
    private var pendingFileQuestionId: Long? = null

    lateinit var navController: androidx.navigation.NavController
    private lateinit var settingsClient: SettingsClient

    private var pendingLocationQuestionId: Long? = null
    private var pendingLocationQuestionIdIndex: Int? = null
    private var pendingLocationQuestionIdIndexRepeaterId: Long? = null

    private fun clearAnswersAfterSubmit() {
        // simplest: ask VM to replace answers map with empty map
        viewModel.saveAnswer(-1, null, null, null) // not great—better add a clear function

    }

    // OpenDocument launchers
    private val openImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val qId = pendingPhotoQuestionId ?: return@registerForActivityResult
            uri?.let {
                // Copy to internal storage so the URI doesn't expire
                val fileName = "img_${qId}_${System.currentTimeMillis()}.jpg"
                val internalPath = copyUriToInternalStorage(it, fileName)

                // Save the local path instead of the external URI
                viewModel.saveAnswer(
                    qId,
                    pendingLocationQuestionIdIndexRepeaterId,
                    pendingLocationQuestionIdIndex,
                    internalPath ?: it.toString()
                )//need to save from current repeater index if applicable
            }
            pendingPhotoQuestionId = null
        }

    // OpenDocument launchers
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val qId = pendingFileQuestionId ?: return@registerForActivityResult
            uri?.let {
                // Use a background scope if the files are very large to prevent ANR,
                // but for simple file picking, this is usually fine.
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
                    requireContext().contentResolver.getType(it)
                ) ?: "bin"
                val fileName = "file_${qId}_${System.currentTimeMillis()}.$extension"
                val internalPath = copyUriToInternalStorage(it, fileName)
                viewModel.saveAnswer(
                    qId,
                    pendingLocationQuestionIdIndexRepeaterId,
                    pendingLocationQuestionIdIndex,
                    internalPath ?: it.toString()
                )//need to save from current repeater index if applicable
            }
            pendingFileQuestionId = null
        }


    private lateinit var currentPhotoFile: File
    private lateinit var cameraUri: Uri

    private fun launchCamera() {
        val fileName = "img_CRS_${System.currentTimeMillis()}.jpg"
        currentPhotoFile = File(requireContext().filesDir, fileName)
        Timber.d("Runtime package: ${context?.applicationContext?.packageName}")

        cameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            currentPhotoFile
        )
        takePhotoLauncher.launch(cameraUri)
    }


    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            Timber.d("Take picture result: success=$success, pendingPhotoQuestionId=$pendingPhotoQuestionId")
            val qId = pendingPhotoQuestionId ?: return@registerForActivityResult
            if (success) {
                val internalPath = currentPhotoFile.absolutePath
                Timber.d("Photo saved to internal storage: $internalPath")
                viewModel.saveAnswer(
                    qId,
                    pendingLocationQuestionIdIndexRepeaterId,
                    pendingLocationQuestionIdIndex,
                    internalPath ?: ""
                )//need to validate this path before saving
            }
        }


    // Location
    private lateinit var fusedLocation: FusedLocationProviderClient

    // Permission launcher
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Timber.d("Permission result (FINE) = $granted")
            if (granted) checkLocationSettingsAndCapture()
            else Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                .show()
        }

    // Settings resolution launcher
    private val resolveSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            Timber.d("Settings resolution result code = ${result.resultCode}")
            // If user accepted turning on location, retry capture
            checkLocationSettingsAndCapture()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        settingsClient = LocationServices.getSettingsClient(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyCrsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // In your Fragment onViewCreated(...)

        navController = findNavController()
       /* binding.recyclerView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // push the bottom bar above the nav bar
            binding.mBottom.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = bars.bottom + resources.getDimensionPixelSize(R.dimen.margin_12)
            }
            // Also add extra padding to RecyclerView so last items are visible
            binding.recyclerView.setPadding(
                binding.recyclerView.paddingLeft,
                binding.recyclerView.paddingTop,
                binding.recyclerView.paddingRight,
                bars.bottom + resources.getDimensionPixelSize(R.dimen.margin_72)
            )
            insets
        }*/
        binding.imgBack.setOnClickListener {
            viewModel.clearAnswers()
            viewModel.signatureCache.clear()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setupAdapter()

        /* ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
             binding.recyclerView.ensureFocusedViewAboveKeyboard(binding.root, insets)
             insets // ✅ always return insets
         }*/




        setupObservers()
        setUpBindingButtons()
        viewModel.loadModule(viewModel.selectedModule?.value?.data?.moduleId ?: 0)
    }


    private fun findConditionalBlocksNew(
        allQuestions: List<UIQuestionUi>,
        rules: List<ConditionalRule>
    ): Map<Long, List<UIQuestionUi>> {
        val result = mutableMapOf<Long, List<UIQuestionUi>>()

        for (rule in rules) {
            val parent = allQuestions.firstOrNull {
                it.base.question.questionText?.contains(rule.keyword, ignoreCase = true) == true
            }
            if (parent != null) {
                val children = allQuestions.filter {
                    it.base.question.displayOrder in ((parent.base.question.displayOrder
                        ?: 0) + 1..(parent.base.question.displayOrder ?: 0) + rule.childCount)
                }
                result[parent.base.question.questionId] = children
            }
        }
        return result
    }

    fun expandRepeaterChildren(
        parent: UIQuestionUi,
        children: List<UIQuestionUi>,
        inputCount: Int
    ): List<UIQuestionUi> {
        val maxRepeats = parent.base.question.repeaterTimes ?: inputCount
        val repeatCount = inputCount.coerceAtMost(maxRepeats)

        val expanded = mutableListOf<UIQuestionUi>()
        for (i in 1..repeatCount) {
            for (child in children) {
                expanded += child.copy(
                    base = child.base.copy(
                        question = child.base.question.copy(
                            questionText = "${child.base.question.questionText} ($i)"
                        )
                    )
                )
            }
        }
        return expanded
    }



    private fun findRepeaterBlocks(allQuestions: List<UIQuestionUi>): Map<Long, List<UIQuestionUi>> {
        val result = mutableMapOf<Long, MutableList<UIQuestionUi>>()
        val mappings = viewModel.repeaterMappings.value ?: emptyList()

        for (map in mappings) {
            val parent = allQuestions.firstOrNull { it.base.question.questionId == map.parentQuestionId }
                ?: continue

            // ✅ Pick the MASTER child (no repeat metadata) — not an expanded copy carrying
            //    a per-instance answer. Falls back to first match if no master is present.
            val masterChild = allQuestions.firstOrNull {
                it.base.question.questionId == map.childQuestionId &&
                        it.repeaterId == null && it.repeatIndex == null
            } ?: allQuestions.firstOrNull { it.base.question.questionId == map.childQuestionId }

            if (masterChild != null) {
                // Strip any answer that might be on the master, just to be safe.
                val cleanTemplate = masterChild.copy(answer = null, repeatIndex = null, repeaterId = null, childIndex = null)
                result.getOrPut(parent.base.question.questionId) { mutableListOf() }
                    .let { existing ->
                        if (existing.none { it.base.question.questionId == cleanTemplate.base.question.questionId }) {
                            existing += cleanTemplate
                        }
                    }
            }
        }

        Timber.d("Repeater Mappings : ${Gson().toJson(result.mapValues { entry -> entry.value.map { it.base.question.questionId } })}")
        return result
    }



    private fun setUpBindingButtons() {
        binding.mBottom.setOnClickListener {

            val focusedView = requireActivity().currentFocus

            Timber.d("Focused view before submit: ${focusedView?.id}, ${focusedView?.contentDescription}")

            if (focusedView is EditText) {
                val text = focusedView.text?.toString()
                Timber.d("Focused EditText value: $text")

                // Trigger your persistence logic
                focusedView.clearFocus() // fires your onFocusChangeListener
            }
            viewModel.commitPendingTextEdits()
            val (ok, error) = viewModel.validate()

            if (!ok) {
                binding.mBottom.context.longToast(getString(R.string.please_answer_all_required_questions))
                return@setOnClickListener
            }


            val answers = viewModel.getAllAnswers()
            if (answers.isEmpty()) {
                binding.mBottom.context.longToast(getString(R.string.please_answer_at_least_one_question))
                return@setOnClickListener
            }

            //submit answers
            Timber.d("Submitting answers: $answers")

            // Show blocking progress & submit
            setBlockingLoading(true)
            viewModel.submitSurvey(
                farmerId = (if (viewModel.isCrsFragment.value == false)
                    viewModel.selectedUser.value?.data?.id ?: "N/A"
                else (viewModel.selectedCrsProfiles.value?.data?.id.toString())),
                moduleId = viewModel.selectedModule.value?.data?.moduleId ?: 0L,
                deviceId = binding.root.context.getSecureAndroidId(),
                "",
                context = requireContext(),
                viewModel.selectedModule.value?.data?.surveyInstanceId ?: 0L
            )
        }
    }


    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera() // safe to open camera
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    private fun checkAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    private fun showImageChooser() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_image))
            .setItems(
                arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery)
                )
            ) { _, which ->
                when (which) {
                    0 -> checkAndLaunchCamera() // Take Photo
                    1 -> openImageLauncher.launch("image/*") // Gallery
                }
            }
            .show()
    }


    private fun setupAdapter() {
        adapter = CrsDynamicQustionsAdapter(
            onAnswer = { key, value ->
                viewModel.saveAnswer(
                    key.questionId,
                    key.repeaterId,
                    key.repeatIndex,
                    value
                )
            },
            onAnswerLive = {key, value ->
                viewModel.saveAnswerLive(key.questionId, key.repeaterId, key.repeatIndex, value)
            },
            onPickPhoto = { key ->
                pendingPhotoQuestionId = key.questionId
                pendingLocationQuestionIdIndexRepeaterId = key.repeaterId
                pendingLocationQuestionIdIndex = key.repeatIndex
                showImageChooser()
            },
            onPickFile = { key ->
                pendingFileQuestionId = key.questionId
                pendingLocationQuestionIdIndex = key.repeatIndex
                pendingLocationQuestionIdIndexRepeaterId = key.repeaterId
                openFileLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/zip",
                        "text/plain"
                    )
                )
            },
            onCaptureLocation = { key ->
                Timber.d("Capture location clicked for qId=${key.questionId}, repeatIndex=${key.repeatIndex}")
                pendingLocationQuestionId = key.questionId
                pendingLocationQuestionIdIndex = key.repeatIndex
                pendingLocationQuestionIdIndexRepeaterId = key.repeaterId
                requestOrCaptureLocation()
            },
            viewModel.signatureCache, // now a MutableMap<AnswerKey, String>,
            viewModel = viewModel
        )


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SurveyFragment.adapter
        }
    }


    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val recycler = _binding?.recyclerView ?: return@OnGlobalLayoutListener
        val focusedView = recycler.findFocus() ?: return@OnGlobalLayoutListener
        val vh = recycler.findContainingViewHolder(focusedView) ?: return@OnGlobalLayoutListener
        recycler.smoothScrollToPosition(vh.adapterPosition)
    }


    private fun setupObservers() {
        //setObservers for module, questions, submit state, etc.
        viewModel.postModuleShimmer.observe(viewLifecycleOwner) { show ->
            binding.apply {
                shimmerLayout.showOrHide(show)
                recyclerView.showOrHide(!show)
                //star shimmer when loading module, hide when loaded
                if (show) shimmerLayout.startShimmer() else shimmerLayout.stopShimmer()
            }
        }
        // Observe the reactive UI list
      /*  viewModel.uiQuestions.observe(viewLifecycleOwner) { list ->
            val details = viewModel.conditionalDetails.value ?: emptyList()
            val evaluator = CrsSkipLogicEvaluator(details, viewModel.conditionalTypeMap)
            val skipped = evaluator.triggeredQuestionIds(viewModel.topLevelAnswers())
            if (viewModel.clearAnswersFor(skipped)) return@observe

            val conditionalChildrenMap = findConditionalBlocks(list, rules = conditionalRules)
            val repeaterChildrenMap = findRepeaterBlocks(list)
            viewModel.repeaterChildrenMap.putAll(repeaterChildrenMap)

            // NEW: keep VM's view in sync for validate()
            viewModel.conditionalChildrenMap.clear()
            viewModel.conditionalChildrenMap.putAll(conditionalChildrenMap)

            val repeaterChildOrders = repeaterChildrenMap.values.flatten().map { it.base.question.displayOrder }
            val conditionalChildOrders = conditionalChildrenMap.values.flatten().map { it.base.question.displayOrder }

            val mainQuestions = list
                .filterNot { it.base.question.displayOrder in repeaterChildOrders }
                .filterNot { it.base.question.displayOrder in conditionalChildOrders }
                .filterNot { it.base.question.questionId in skipped }

            adapter.submitList(mainQuestions)
            adapter.setConditionalChildrenMap(conditionalChildrenMap)
            adapter.setRepeaterChildrenMap(repeaterChildrenMap)
        }*/


        viewModel.uiQuestions.observe(viewLifecycleOwner) { list ->
            val details = viewModel.conditionalDetails.value ?: emptyList()
            val evaluator = CrsSkipLogicEvaluator(details, viewModel.conditionalTypeMap)
            val skipped = evaluator.triggeredQuestionIds(viewModel.topLevelAnswers())
            if (viewModel.clearAnswersFor(skipped)) return@observe

            val repeaterChildrenMap = findRepeaterBlocks(list)
            viewModel.repeaterChildrenMap.putAll(repeaterChildrenMap)

            val repeaterChildOrders = repeaterChildrenMap.values.flatten().map { it.base.question.displayOrder }

            val mainQuestions = list
                .filterNot { it.base.question.displayOrder in repeaterChildOrders }
                .filterNot { it.base.question.questionId in skipped }

            adapter.submitList(mainQuestions)
            adapter.setRepeaterChildrenMap(repeaterChildrenMap)
        }




        // Existing module state (show blocking overlay)
        viewModel.selectedModule.observe(viewLifecycleOwner) { res ->
            when (res.state) {
                ResourceState.LOADING -> setBlockingLoading(true)
                ResourceState.SUCCESS -> {
                    setBlockingLoading(false)
                    Timber.d("Module loaded: ${res.data}")
                    // viewModel.loadModule(res.data?.moduleId ?: 1) // if needed
                }

                ResourceState.ERROR -> {
                    setBlockingLoading(false)
                    Toast.makeText(requireContext(), "Failed to load module", Toast.LENGTH_SHORT)
                        .show()
                }

                ResourceState.NO_DATA -> {
                    setBlockingLoading(false)
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // If questions load asynchronously separately, you can expose a Resource for questions
        // and toggle the same overlay here.

        viewModel.submitState.observe(viewLifecycleOwner) { res ->
            when (res.state) {
                ResourceState.LOADING -> {
                    setBlockingLoading(true)
                    binding.mBottom.isEnabled = false
                }

                ResourceState.SUCCESS -> {
                    setBlockingLoading(false)
                    binding.mBottom.isEnabled = true
                    Toast.makeText(requireContext(), getString(R.string.survey_submitted_successfully), Toast.LENGTH_SHORT).show()
                    viewModel.clearAnswers()                // ✅ clears _answers + signatureCache + pendingTextEdits
                    viewModel.clearSubmitSurvey(viewLifecycleOwner)
                    val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                    navController.navigate(R.id.action_surveyFragment_to_crsSuccessFragment)
                }
                ResourceState.ERROR -> {
                    setBlockingLoading(false)
                    binding.mBottom.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        res.message ?: getString(R.string.submit_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Unit
            }
        }

    }



    private fun findConditionalBlocks(
        allQuestions: List<UIQuestionUi>,
        details: List<ConditionalQuestionDetailEntity> = emptyList(),
        rules: List<ConditionalRule> = emptyList()
    ): Map<Long, List<UIQuestionUi>> {
        val result = mutableMapOf<Long, MutableList<UIQuestionUi>>()
        val byId = allQuestions.associateBy { it.base.question.questionId }

        fun addChildren(parentId: Long, children: List<UIQuestionUi>) {
            if (children.isEmpty()) return
            val list = result.getOrPut(parentId) { mutableListOf() }
            children.forEach { child ->
                if (list.none { it.base.question.questionId == child.base.question.questionId }) {
                    list += child
                }
            }
        }

        // Source 1: explicit entities — parent by questionId, children by skipQuestionIds
        for (detail in details) {
            val parent = byId[detail.questionId] ?: continue
            val childIds = detail.skipQuestionIds
                ?.split(",")
                ?.mapNotNull { it.trim().toLongOrNull() }
                ?.toSet()
                .orEmpty()
            if (childIds.isEmpty()) continue
            val children = allQuestions.filter { it.base.question.questionId in childIds }
            addChildren(parent.base.question.questionId, children)
        }

        // Source 2: keyword rules — parent by questionText match, children by next N display orders
        for (rule in rules) {
            val parent = allQuestions.firstOrNull {
                it.base.question.questionText?.contains(rule.keyword, ignoreCase = true) == true
            } ?: continue
            val parentOrder = parent.base.question.displayOrder ?: 0
            val children = allQuestions.filter {
                val order = it.base.question.displayOrder ?: return@filter false
                order in (parentOrder + 1)..(parentOrder + rule.childCount)
            }
            addChildren(parent.base.question.questionId, children)
        }

        return result
    }

    private fun findConditionalBlocksOldNew(
        allQuestions: List<UIQuestionUi>,
        details: List<ConditionalQuestionDetailEntity>
    ): Map<Long, List<UIQuestionUi>> {
        val result = mutableMapOf<Long, List<UIQuestionUi>>()

        for (detail in details) {
            val parent =
                allQuestions.firstOrNull { it.base.question.questionId == detail.questionId }
            if (parent != null) {
                val children = allQuestions.filter {
                    detail.skipQuestionIds?.split(",")?.mapNotNull { it.toLongOrNull() }
                        ?.contains(it.base.question.questionId) == true
                }
                result[parent.base.question.questionId] = children
            }
        }
        return result
    }


    private fun setBlockingLoading(show: Boolean) {
        binding.mProgress.visibility = if (show) View.VISIBLE else View.GONE
        val act = activity ?: return
        if (show) {
            act.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    /* --------------- Location --------------- */

    private fun requestOrCaptureLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {
            Timber.d("Requesting ACCESS_FINE_LOCATION")
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        checkLocationSettingsAndCapture()
    }

    private fun checkLocationSettingsAndCapture() {
        val req = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L
        ).setMinUpdateIntervalMillis(5_000L).build()

        val settingsReq = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true) // show dialog if needed
            .build()

        settingsClient.checkLocationSettings(settingsReq)
            .addOnSuccessListener {
                Timber.d("Location settings OK")
                captureLocationNow()
            }
            .addOnFailureListener { ex ->
                Timber.w(ex, "Location settings not satisfied")
                if (ex is ResolvableApiException) {
                    try {
                        val intentSender =
                            ex.resolution?.intentSender ?: return@addOnFailureListener
                        resolveSettingsLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    } catch (sie: IntentSender.SendIntentException) {
                        Timber.e(sie, "Settings resolution launch failed")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.enable_location_in_settings), Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.location_is_disabled_please_enable_it),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    @SuppressLint("MissingPermission")
    private fun captureLocationNow() {
        val qId = pendingLocationQuestionId
        val index = pendingLocationQuestionIdIndex
        val repeaterId = pendingLocationQuestionIdIndexRepeaterId
        if (qId == null) {
            Timber.w("No pendingLocationQuestionId set")
            return
        }

        Timber.d("Trying lastLocation...")
        fusedLocation.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    Timber.d("lastLocation: ${loc.latitude}, ${loc.longitude}")
                    viewModel.saveAnswer(
                        qId,
                        repeaterId,
                        pendingLocationQuestionIdIndex,
                        "${loc.latitude},${loc.longitude}"
                    )
                    pendingLocationQuestionId = null
                    pendingLocationQuestionIdIndex = null
                    pendingLocationQuestionIdIndexRepeaterId = null
                } else {
                    Timber.d("lastLocation is null, fallback to getCurrentLocation")
                    getSingleFix(qId, index, repeaterId)
                }
            }
            .addOnFailureListener { e ->
                Timber.w(e, "lastLocation failed; fallback to getCurrentLocation")
                getSingleFix(qId, index, repeaterId)
            }
    }

    @SuppressLint("MissingPermission")
    private fun getSingleFix(qId: Long, index: Int?, repeaterId: Long?) {
        val cts = com.google.android.gms.tasks.CancellationTokenSource()

        // Timeout after 10 seconds
        binding.recyclerView.postDelayed({
            Timber.w("getCurrentLocation timeout; cancelling")
            cts.cancel()
        }, 10_000L)

        fusedLocation.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    Timber.d("getCurrentLocation: ${loc.latitude}, ${loc.longitude}")
                    viewModel.saveAnswer(qId, repeaterId, index, "${loc.latitude},${loc.longitude}")
                } else {
                    Timber.w("getCurrentLocation returned null")
                    Toast.makeText(
                        requireContext(),
                        "Unable to capture location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                pendingLocationQuestionId = null
                pendingLocationQuestionIdIndex = null
                pendingLocationQuestionIdIndexRepeaterId = null
            }
            .addOnFailureListener { e ->
                Timber.e(e, "getCurrentLocation failed")
                Toast.makeText(requireContext(), "Unable to capture location", Toast.LENGTH_SHORT)
                    .show()
                pendingLocationQuestionId = null
                pendingLocationQuestionIdIndex = null
                pendingLocationQuestionIdIndexRepeaterId = null
            }
    }

    private fun copyUriToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath // Save this path in your ViewModel/DB
        } catch (e: Exception) {
            Timber.e(e, "Error copying file")
            null
        }
    }

    private fun persistReadPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (permission: SecurityException) {
            // ignore if provider doesn't support persistable perms
            Timber.w("Persistable permission not supported for uri: ${permission.message}")
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroyView()
        viewModel.clearAnswers()
        _binding = null
    }
}