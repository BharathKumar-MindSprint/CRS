package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.olam.digital.ofis.R
import com.olam.digital.ofis.databinding.FragmentInputOrFinanceDistributionBinding
import com.olam.digital.ofis.model.SpinnerDataDto
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class InputOrFinanceDistributionFragment : Fragment() {
    private lateinit var binding: FragmentInputOrFinanceDistributionBinding
    private lateinit var viewModel : DistributionModuleViewModel
    private lateinit var navController: NavController
    private lateinit var distributionGroupSpinner : Spinner
    private lateinit var distributionGroupAdapter: ArrayAdapter<String>
    private lateinit var distributionItemSpinner: Spinner
    private lateinit var distributionItemAdapter: ArrayAdapter<String>
    private lateinit var uomSpinner : Spinner
    private lateinit var uomAdapter: ArrayAdapter<String>
    private var storageDir : File? = null


    companion object {
        const val REQUEST_PERMISSIONS_FOR_TIRAMISU: Int = 2509
    }

    private var isUploadPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DistributionModuleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        requireActivity().setTitle(R.string.distribution_form)
        binding = FragmentInputOrFinanceDistributionBinding.inflate(layoutInflater, container, false)
        storageDir = requireActivity().getExternalFilesDir("")
        distributionGroupSpinner = binding.spDistributionGroup
        distributionItemSpinner = binding.spDistributionItem
        uomSpinner = binding.spUOM
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)

        overrideMenuBar()
        initUiState()
        initObservers()

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)
    }

    private fun overrideMenuBar() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Do NOT inflate a new menu here
                // Just override handling of existing menu items
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        if (viewModel.isFromSyncScreen.value == true) {
                            requireActivity().finish()
                            return true
                        }

                        viewModel.onEvent(DistributionModuleUiEvent.ResetDistributionFormState)
                        navController.popBackStack()
                        true
                    }

                    else -> return onMenuItemSelected(menuItem)
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initUiState() {
        val state = viewModel.farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
        val distributionFormState = viewModel.distributionFormState.value ?: DistributionFormState()
        binding.apply {
            tvName.text = state.selectedFarmerOrStakeHolder?.name ?: ""
            tvDisplayId.text = state.selectedFarmerOrStakeHolder?.displayId ?: ""
            etTransaction.setText(distributionFormState.transactionValue)
            etPricePerUnit.setText(distributionFormState.pricePerUnit)
            etTotalAmount.setText(distributionFormState.totalAmount)

            addTextWatchers()
            setupCategoryButtons()

            setSpannableString(R.string.distribution_group, tvDistributionGroup)
            setSpannableString(R.string.distribution_item, tvDistributionItem)
            setSpannableString(R.string.transaction_value, tvTransactionValue)

            btnBack.setOnClickListener {
                if (viewModel.isFromSyncScreen.value == true) {
                    requireActivity().finish()
                    return@setOnClickListener
                }

                viewModel.onEvent(DistributionModuleUiEvent.ResetDistributionFormState)
                navController.popBackStack()
            }

            clTakePhoto.setOnClickListener {
                requestPermissions(false)
            }

            clUploadFile.setOnClickListener {
                requestPermissions(true)
            }

        }

        viewModel.distributionGroups.value?.let { data ->
            val names = data.map { it.name.ifEmpty { getString(R.string.distribution_group) } }.toMutableList()
            distributionGroupAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, names)
            bindSpinner(distributionGroupSpinner, distributionGroupAdapter) { position ->
                DistributionModuleUiEvent.OnDistributionGroupChange(position)
            }
        }

        viewModel.distributionItem.value?.let { data ->
            val names = data.map { it.name.ifEmpty { getString(R.string.distribution_item) } }.toMutableList()
            distributionItemAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, names)
            bindSpinner(distributionItemSpinner, distributionItemAdapter) { position ->
                DistributionModuleUiEvent.OnDistributionItemChange(position)
            }
        }

        val uOMItem = if (distributionFormState.distributionCategory == CategoryEnum.INPUT) viewModel.localUnits else viewModel.currency
        val stringId = if (distributionFormState.distributionCategory == CategoryEnum.INPUT) R.string.select_unit else R.string.please_select_currency

        uOMItem.value?.let { uomList ->
            val names = uomList.mapIndexed { index, data ->
                if (index == 0) getString(stringId)
                else "${data.notation} (${data.name.ifEmpty { getString(stringId) }})"
            }

            uomAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, names)
            bindSpinner(uomSpinner, uomAdapter) { position ->
                DistributionModuleUiEvent.OnUOMChange(position)
            }
        }

        binding.btnNext.setOnClickListener {
            if (validateMandatoryFields()) {
                showSignatureDialogBox()
            }
        }
        resetUiState(CategoryEnum.INPUT)
    }

    private fun setupCategoryButtons() {
        val categoryMap = mapOf(
            binding.btnInput to CategoryEnum.INPUT,
            binding.btnFinance to CategoryEnum.FINANCE
        )

        categoryMap.forEach { (button, category) ->
            button.setOnClickListener {
                resetUiState(category)
                viewModel.onEvent(DistributionModuleUiEvent.OnCategoryChange(category))
            }
        }
    }


    private fun resetUiState(categoryEnum: CategoryEnum) {
        distributionGroupSpinner.setSelection(0)
        distributionItemSpinner.setSelection(0)
        uomSpinner.setSelection(0)
        binding.apply {
            clTakePhoto.visibility = View.VISIBLE
            clUploadFile.visibility = View.VISIBLE
            ivDisplayEvidence.visibility = View.GONE
        }
    }

    private fun validateMandatoryFields(): Boolean {
        val state = viewModel.distributionFormState.value ?: DistributionFormState()
        if (state.distributionGroupId.isEmpty() || state.distributionGroupId == "0") {
            Toast.makeText(context, getString(R.string.error_message_distribution_groups), Toast.LENGTH_SHORT).show()
            return false
        }

        if (state.distributionItemId.isEmpty() || state.distributionItemId == "0") {
            Toast.makeText(context, getString(R.string.error_message_distribution_items), Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.etTransaction.text.toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.error_message_transaction_value), Toast.LENGTH_SHORT).show()
            return false
        }

        val uom = state.localUnitId ?: state.currencyId
        if (uom == null || uom == 0) {
            val id = if (state.distributionCategory == CategoryEnum.INPUT) R.string.error_message_local_units else R.string.error_messgae_currency
            Toast.makeText(context, getString(id), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addTextWatchers() {
        binding.apply {
            etPricePerUnit.filters = getDecimalInputFilters()
            etTransaction.filters = getDecimalInputFilters()

            etPricePerUnit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(value: Editable?) {
                    val transactionValue = etTransaction.text.toString()
                    if (value != null && value.toString()
                            .isNotEmpty() && transactionValue.isNotEmpty()
                    ) {
                        val pricePerUnit = BigDecimal(value.toString())
                        val transaction = BigDecimal(transactionValue)
                        val totalAmount = pricePerUnit.multiply(transaction)
                        etTotalAmount.setText(totalAmount.toString())
                    } else {
                        etTotalAmount.setText("")
                    }
                }

            })

            etTransaction.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(value: Editable?) {
                    val transactionValue = etPricePerUnit.text.toString()
                    if (value != null && value.toString()
                            .isNotEmpty() && transactionValue.isNotEmpty()
                    ) {
                        val pricePerUnit = BigDecimal(value.toString())
                        val transaction = BigDecimal(transactionValue)
                        val totalAmount = pricePerUnit.multiply(transaction)
                        etTotalAmount.setText(totalAmount.toString())
                    }
                    else {
                        etTotalAmount.setText("")
                    }
                }

            })
        }
    }

    fun getDecimalInputFilters(): Array<InputFilter> {
        val decimalFilter = object : InputFilter {
            private val regex = Regex("^[0-9]*+((\\.[0-9]{0,3})?)?\$")

            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val newText = dest.toString().substring(0, dstart) +
                        source.subSequence(start, end) +
                        dest.toString().substring(dend)

                return if (regex.matches(newText)) null else ""
            }
        }

        return arrayOf(
            decimalFilter,
            InputFilter.LengthFilter(20)
        )
    }


    private fun initObservers() {
        viewModel.onEvent(DistributionModuleUiEvent.GetDistributionData)
        viewModel.distributionFormState.observe(viewLifecycleOwner) { state ->

            binding.apply {
                val isFinance = state.distributionCategory == CategoryEnum.FINANCE
                etTransaction.setText(state.transactionValue)
                etPricePerUnit.setText(state.pricePerUnit)
                etTotalAmount.setText(state.totalAmount)

                btnFinance.apply {
                    setBackgroundResource(
                        if (isFinance) R.drawable.btn_purple_background
                        else R.drawable.btn_rounded_corner_for_distribution
                    )
                    setTextColor(
                        resources.getColor(
                            if (isFinance) R.color.white
                            else R.color.light_grey
                        )
                    )
                }

                btnInput.apply {
                    setBackgroundResource(
                        if (!isFinance) R.drawable.btn_purple_background
                        else R.drawable.btn_rounded_corner_for_distribution
                    )
                    setTextColor(
                        resources.getColor(
                            if (!isFinance) R.color.white
                            else R.color.light_grey
                        )
                    )
                }

                val hasEvidence = state.evidenceName.isNotEmpty()
                if (hasEvidence) {
                    clTakePhoto.visibility = View.GONE
                    clUploadFile.visibility = View.GONE

                    ivDisplayEvidence.apply {
                        visibility = View.VISIBLE
                        val file = File(storageDir.toString() + "/", state.evidenceName)
                        val evidenceUri = getUriFromFile(file)
                        setImageURI(evidenceUri)
                    }
                }
            }
            viewModel.onEvent(DistributionModuleUiEvent.LoadDistributionSpinnerData)
        }

        viewModel.distributionItem.observe(viewLifecycleOwner) { data ->
            val names = data.map { it.name.ifEmpty { getString(R.string.distribution_item) } }.toMutableList()
            distributionItemAdapter.clear()
            distributionItemAdapter.addAll(names)
            distributionItemAdapter.notifyDataSetChanged()

            val state = viewModel.distributionFormState.value ?: DistributionFormState()
            val id = if (state.distributionItemId.isEmpty()) 0 else state.distributionItemId.toInt()
            distributionItemSpinner.setSelection(getIndexPosition(id, viewModel.distributionItem.value ?: emptyList()))
        }

        viewModel.distributionGroups.observe(viewLifecycleOwner) { data ->
            val names = data.map { it.name.ifEmpty { getString(R.string.distribution_group) } }.toMutableList()
            distributionGroupAdapter.clear()
            distributionGroupAdapter.addAll(names)
            distributionGroupAdapter.notifyDataSetChanged()

            val state = viewModel.distributionFormState.value ?: DistributionFormState()
            val id = if (state.distributionGroupId.isEmpty()) 0 else state.distributionGroupId.toInt()
            distributionGroupSpinner.setSelection(getIndexPosition(id, viewModel.distributionGroups.value ?: emptyList()))
        }

        viewModel.localUnits.observe(viewLifecycleOwner) {
            binding.clPricePerUnit.visibility = View.VISIBLE
            binding.clTotalAmount.visibility = View.VISIBLE

            viewModel.localUnits.observe(viewLifecycleOwner) { localUnitList ->
                val names = localUnitList.mapIndexed { index, data ->
                    if (index == 0) getString(R.string.select_unit)
                    else "${data.notation} (${data.name.ifEmpty { getString(R.string.select_unit) }})"
                }

                val state = viewModel.distributionFormState.value ?: DistributionFormState()

                if (state.distributionCategory == CategoryEnum.FINANCE) {
                    return@observe
                }

                uomAdapter.apply {
                    clear()
                    addAll(names)
                    notifyDataSetChanged()
                }

                val isGlobalId = state.currencyId != null
                uomSpinner.setSelection(getIndexPosition(state.currencyId ?: state.localUnitId, viewModel.localUnits.value ?: emptyList(), isGlobalId))

            }
        }

        viewModel.currency.observe(viewLifecycleOwner) { currencyList ->
            binding.clPricePerUnit.visibility = View.GONE
            binding.clTotalAmount.visibility = View.GONE

            val names = currencyList.mapIndexed { index, data ->
                if (index == 0) getString(R.string.please_select_currency)
                else "${data.notation} (${data.name.ifEmpty { getString(R.string.please_select_currency) }})"
            }

            val state = viewModel.distributionFormState.value ?: DistributionFormState()

            if (state.distributionCategory == CategoryEnum.INPUT) {
                return@observe
            }

            uomAdapter.apply {
                clear()
                addAll(names)
                notifyDataSetChanged()
            }

            val isGlobalId = state.currencyId != null
            uomSpinner.setSelection(getIndexPosition(state.currencyId ?: state.localUnitId, viewModel.currency.value ?: emptyList(), isGlobalId))
        }

    }

    private fun getIndexPosition(value: Int?, data: List<SpinnerDataDto>, isGlobalId: Boolean = false): Int {
        val id = value ?: 0
        return data.indexOfFirst { spinnerDataDto ->
            if (isGlobalId && spinnerDataDto.isGlobalId == true && spinnerDataDto.id == id) {
                true
            } else {
                spinnerDataDto.id == id && spinnerDataDto.isGlobalId != true
            }
        }.takeIf { it >= 0 } ?: 0
    }

    private fun bindSpinner(spinner: Spinner, adapter: ArrayAdapter<String>, event: (Int) -> DistributionModuleUiEvent) {
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.onEvent(event(position))
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showSignatureDialogBox() {
        var isSigned = false
        val signatureDialog = Dialog(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
        signatureDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        signatureDialog.setContentView(R.layout.dialog_signature)
        val mSignaturePad = signatureDialog.findViewById<SignaturePad>(R.id.signature_pad)

        val btnClear = signatureDialog.findViewById<View>(R.id.imgv_clear_signature) as Button
        val btnConfirm = signatureDialog.findViewById<View>(R.id.btn_add_signature) as Button

        btnConfirm.isEnabled = true
        btnClear.isEnabled = true
        btnClear.setOnClickListener { mSignaturePad.clear() }

        mSignaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                //Event triggered when the pad is touched
            }

            override fun onSigned() {
                isSigned = true
            }

            override fun onClear() {
                isSigned = false
            }
        })

        signatureDialog.findViewById<View>(R.id.btn_cancel_signature).setOnClickListener { signatureDialog.dismiss() }
        btnConfirm.setOnClickListener {
            try {
                if (isSigned) {
                    deletePreviousSignatureFile()
                    val photoFile: File? = createSignatureFile()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                            m.invoke(null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    try {
                        FileOutputStream(photoFile).use { out ->
                            mSignaturePad.transparentSignatureBitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                out
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    viewModel.onEvent(DistributionModuleUiEvent.UpdateDistributionFormDetails(
                        photoFile?.name ?: "",
                        photoFile?.path ?: "",
                        mSignaturePad.transparentSignatureBitmap,
                        binding.etTransaction.text.toString(),
                        binding.etPricePerUnit.text.toString(),
                        binding.etTotalAmount.text.toString()
                    ))
                    navController.navigate(R.id.input_or_finance_distribution_to_distribution_summary_fragment)
                    signatureDialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.no_signature_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        signatureDialog.show()
    }

    @Throws(IOException::class)
    private fun createSignatureFile(): File? {
        val imageFileName = getFileName("distribution_signature")

        return try {
            val file = File(storageDir, imageFileName)
            file.createNewFile()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deletePreviousSignatureFile() {
        val path = viewModel.distributionFormState.value?.signatureFileName ?: ""
        if (path.isNotEmpty()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun setSpannableString(label : Int, labelTextView : TextView) {
        val labelName = getString(label)
        val asterisk = "*"
        val spannable = SpannableString("$labelName $asterisk")

        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            labelName.length + 1,
            labelName.length + 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        labelTextView.text = spannable
    }

    private fun requestPermissions(isUpload: Boolean) {
        isUploadPending = isUpload

        val permissions = arrayOf(Manifest.permission.CAMERA)

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            if (isUpload) openPhotoPicker() else openCamera()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (cameraGranted || isUploadPending) {
            if (isUploadPending) openPhotoPicker() else openCamera()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showSettingsDialog(getString(R.string.go_to_settings_msg))
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.permission_denial_msg_android_13_above,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    fun showSettingsDialog(msg: String?) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(R.string.permission_permanent_denial_title)
        dialog.setMessage(msg).setPositiveButton(
            R.string.yes
        ) { _, _ ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + requireContext().packageName)
                )
            )
        }.setNegativeButton(
            R.string.app_update_cancel
        ) { _, _ ->
            Toast.makeText(
                context,
                R.string.permission_permanent_denial_msg,
                Toast.LENGTH_LONG
            ).show()
        }
        dialog.show()
    }

    private fun openPhotoPicker() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1)
            }
        } else {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri = data?.data

            binding.apply {
                ivDisplayEvidence.setImageURI(selectedImageUri)
                ivDisplayEvidence.visibility = View.VISIBLE
                clUploadFile.visibility = View.GONE
                clTakePhoto.visibility = View.GONE
            }

            val imageFile = selectedImageUri?.let { createFileFromUri(requireContext(), it) }
            if (imageFile != null) {
                viewModel.onEvent(DistributionModuleUiEvent.EvidenceUpload(selectedImageUri, imageFile.name))
            }
        }
        else {
            Toast.makeText(context, "Error in Upload Image", Toast.LENGTH_LONG).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            binding.apply {
                ivDisplayEvidence.setImageBitmap(imageBitmap)
                ivDisplayEvidence.visibility = View.VISIBLE
                clUploadFile.visibility = View.GONE
                clTakePhoto.visibility = View.GONE
            }

            val imageFile = imageBitmap?.let { saveBitmapToFile(it) }
            if (imageFile != null) {
                val uri = getUriFromFile(imageFile)
                viewModel.onEvent(DistributionModuleUiEvent.EvidenceUpload(uri, imageFile.name))
            }
        }
        else {
            Toast.makeText(context, "Error in Capture Image", Toast.LENGTH_LONG).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        val imageFileName = getFileName("distribution_evidence")

        return try {
            val file = File(storageDir, imageFileName)
            file.createNewFile()

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName("distribution_evidence")
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(storageDir, fileName)

            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "${context?.packageName}.fileprovider",
            file
        )
    }

    @SuppressLint("Range")
    fun getFileName(fileNamePrefix : String): String {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(Date())
        return "${fileNamePrefix}_$timestamp.jpg"
    }

}