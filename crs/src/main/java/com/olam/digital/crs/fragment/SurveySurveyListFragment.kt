package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.ItemsSurveyListBinding
import com.olam.digital.crs.databinding.LayoutSurveyHomeBinding
import com.olam.digital.crs.extension.ModuleStatus
import com.olam.digital.crs.extension.RecurrenceValidator
import com.olam.digital.crs.extension.hide
import com.olam.digital.crs.extension.longToast
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.extension.show
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.ShowRecycler
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import kotlinx.serialization.json.JsonArray
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class SurveySurveyListFragment : Fragment() {
    lateinit var binding: LayoutSurveyHomeBinding
    private val crsViewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutSurveyHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            action = ShowRecycler.SHOW_SHIMMER
            // Initialize UI components here
            imgBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            setUi(null)
            setObservers()
        }
    }

    private fun setCrsCommunityUiData(lData: RespondentEntity?) {
        binding.apply {
            binding.apply {
                // Initialize UI components here
                lData?.let {
                    imgBack.setOnClickListener {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    showQuickAdd = false
                    edtSearch.addTextChangedListener {
                        setRecyclerData(crsViewModel.modulesCrsSurvey.value?.data, it.toString())
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        crsViewModel.clearData()
    }

    private fun setObservers() {
        // Set up observers here
        binding.apply {
            crsViewModel.apply {
                selectedUser.observe(viewLifecycleOwner)
                {
                    setUi(selectedUser.value?.data)
                }
                modules.observe(viewLifecycleOwner)
                {
                    when (it.state) {
                        ResourceState.LOADING -> {
                            // Show loading state if needed
                            action = ShowRecycler.SHOW_SHIMMER
                        }

                        ResourceState.SUCCESS -> {
                            // Handle success state if needed
                            action = ShowRecycler.SHOW_FIRST_RECYLCERVIEW
                            setRecyclerData(it.data)
                        }

                        ResourceState.ERROR -> {
                            // Handle error state if needed
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }

                        ResourceState.NO_DATA -> {
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }
                    }

                }
                modulesCrsSurvey.observe(viewLifecycleOwner)
                {
                    when (it.state) {
                        ResourceState.LOADING -> {
                            // Show loading state if needed
                            action  = ShowRecycler.SHOW_SHIMMER
                        }

                        ResourceState.SUCCESS -> {
                            // Handle success state if needed
                            action = ShowRecycler.SHOW_FIRST_RECYLCERVIEW
                            setRecyclerData(it.data)
                        }

                        ResourceState.ERROR -> {
                            // Handle error state if needed
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }

                        ResourceState.NO_DATA -> {
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }
                    }

                }
                selectedCrsProfiles.observe(viewLifecycleOwner)
                {
                    // Handle selected CRS profiles if needed
                    when (it.state) {
                        ResourceState.LOADING -> {
                            // Show loading state if needed
                        }

                        ResourceState.SUCCESS -> {
                            // Handle success state if needed
                            action =  ShowRecycler.SHOW_SHIMMER
                            setCrsCommunityUiData(it.data)
                            getAllCrsSurvey(it.data ?: RespondentEntity())
                        }

                        ResourceState.ERROR -> {
                            // Handle error state if needed
                        }

                        ResourceState.NO_DATA -> {

                        }
                    }
                }
            }
        }
    }


    fun setRecyclerData(lRecylerData: List<FarmerModuleView>?, query: String? = null) {
        binding.apply {
            lRecylerData?.let {

                val filteredList = if (!query.isNullOrEmpty()) {
                    it.filter { module ->
                        module.name?.contains(query, ignoreCase = true) == true
                    }.toMutableList()
                } else {
                    it.toMutableList()
                }
                var newListData = filteredList

                Timber.d("setRecyclerData: selectedCrsProfiles ${Gson().toJson(crsViewModel.selectedCrsProfiles.value?.data)}")
                if (crsViewModel.isCrsFragment.value == true) {
                    newListData = filteredList.filter { module ->
                        module.communityIds?.contains(
                            crsViewModel.selectedCrsProfiles.value?.data?.profileId ?: 0L
                        ) == true
                    }.toMutableList()
                }

// Convert submittedModules string to JSONArray safely
                val submittedModulesArray: JSONArray? = try {
                    val submittedModulesString =
                        crsViewModel.selectedUser?.value?.data?.submittedModules
                    if (!submittedModulesString.isNullOrEmpty()) {
                        val jsonModuleObject = JSONObject(submittedModulesString)
                        jsonModuleObject.getJSONArray("modules")
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Timber.e("Error parsing submittedModules: ${e.message}")
                    e.printStackTrace()
                    null
                }



                mRecylerView.setUpAdapter(
                    items = newListData, R.layout.items_survey_list,
                    ItemsSurveyListBinding::inflate,
                    { items, pos, bin ->
                        bin.apply {
                            Timber.d("Selected module data ids ${items.communityIds}")
                            txtSurveyName.text = items.name
                            txtSurveyDesc.text =
                                "${getString(R.string.module_id)} ${items.moduleId}"
                            txtNoOfQuestions.text =
                                "${items.questionCount} ${getString(R.string.questions)}"
                            layDisplayStatus.show()
                            txtSurveyDesc.hide()

                            Timber.d("Module ${items.moduleId} - module name - ${items.name} Start date epoch: ${items.startDateEpoch}, Recurrence type: ${items.recurrenceType}, Recurrence frequency: ${items.recurrenceFrequency}, Disabled months: ${items.disabledMonths}, Assignment type: ${items.assignmentType}")

                            // 🔑 Recurrence + submission check
                            val status = RecurrenceValidator.getModuleStatus(
                                item = items,
                                submittedModulesArray = submittedModulesArray ?: JSONArray(),
                                farmerId = crsViewModel.selectedUser.value?.data?.id?.toLong()
                                    ?: 0L,
                                disabledMonths = items.disabledMonths?.split(",")?.map { it.trim() }
                                    ?: emptyList(), // inject actual disabled months if available
                                assignmentType = items.assignmentType ?: "1",
                                startDateEpoch = items.startDateEpoch
                            )

                            // UI state mapping
                            txtActiveState.text = when (status) {
                                ModuleStatus.ACTIVE -> getString(R.string.active)
                                ModuleStatus.IN_PROGRESS -> getString(R.string.in_progress)
                                ModuleStatus.COMPLETED -> getString(R.string.completed)
                                ModuleStatus.IN_ACTIVE -> getString(R.string.in_active)
                                ModuleStatus.NOT_AVAILABLE -> "${getString(R.string.not_available)}"
                            }

                            txtActiveState.setTextColor(
                                ContextCompat.getColor(
                                    txtActiveState.context,
                                    when (status) {
                                        ModuleStatus.ACTIVE -> R.color.colorActiveGreen
                                        ModuleStatus.IN_PROGRESS -> R.color.darkGray
                                        ModuleStatus.COMPLETED -> R.color.white
                                        ModuleStatus.IN_ACTIVE -> R.color.ofis_list_item_disabled_text
                                        ModuleStatus.NOT_AVAILABLE -> R.color.ofis_list_item_disabled_text
                                    }
                                )
                            )

                            layDisplayStatus.setBackgroundResource(
                                when (status) {
                                    ModuleStatus.ACTIVE -> R.drawable.bg_fill_green_color
                                    ModuleStatus.IN_PROGRESS -> R.drawable.bg_status_inprogress
                                    ModuleStatus.COMPLETED -> R.drawable.bg_status_completed
                                    ModuleStatus.IN_ACTIVE -> R.drawable.bg_selector_disable
                                    ModuleStatus.NOT_AVAILABLE -> R.drawable.bg_selector_disable
                                }
                            )

                            // Click handling
                            root.setOnClickListener {
                                when (status) {
                                    ModuleStatus.COMPLETED -> {
                                        root.context.longToast(getString(R.string.survey_completed_cannot_edit))
                                    }

                                    ModuleStatus.IN_ACTIVE -> {
                                        root.context.longToast(getString(R.string.survey_disabled_cannot_access))
                                    }

                                    else -> {
                                        crsViewModel.selectModule(items)
                                        crsViewModel.loadModule(items.moduleId)

                                        if (items.statusStarted == 1) {
                                            crsViewModel.loadPreviousAnswers(
                                                items.moduleId,
                                                crsViewModel.selectedUser.value?.data?.id ?: "",
                                                items.surveyInstanceId ?: 0L
                                            )
                                        }

                                        Timber.d("Navigating to consent or survey. Consent enabled: ${items.isConsentEnabled}, Consent ID: ${items.consentId}")

                                       if (items.isConsentEnabled==true && items.consentId!=null)
                                       {
                                           val navController =
                                               requireActivity().findNavController(R.id.fragmentContainerView)
                                           navController.navigate(R.id.crsConsentFragment)
                                       }
                                        else{
                                           val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                                           navController.navigate(R.id.surveyFragment)
                                       }
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }


    fun calculateRecurrenceCount(
        startDate: Date,
        recurrenceType: String,
        recurrenceFrequency: Int
    ): Int {
        val now = Date()
        val calStart = Calendar.getInstance().apply { time = startDate }
        val calNow = Calendar.getInstance().apply { time = now }

        return when (recurrenceType.uppercase(Locale.getDefault())) {
            "DAILY" -> {
                val diffDays =
                    ((calNow.timeInMillis - calStart.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                diffDays / recurrenceFrequency
            }

            "MONTHLY" -> {
                val diffMonths = (calNow.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)) * 12 +
                        (calNow.get(Calendar.MONTH) - calStart.get(Calendar.MONTH))
                diffMonths / recurrenceFrequency
            }

            "YEARLY" -> {
                val diffYears = calNow.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)
                diffYears / recurrenceFrequency
            }

            else -> 0
        }
    }


    fun getNewStartDate(
        startDate: Date,
        recurrenceType: String,
        recurrenceFrequency: Int,
        recurrenceCount: Int
    ): Date {
        val cal = Calendar.getInstance()
        cal.time = startDate
        when (recurrenceType.uppercase(Locale.getDefault())) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, recurrenceFrequency * recurrenceCount)
            "MONTHLY" -> cal.add(Calendar.MONTH, recurrenceFrequency * recurrenceCount)
            "YEARLY" -> cal.add(Calendar.YEAR, recurrenceFrequency * recurrenceCount)
        }
        return cal.time
    }

    fun getNewEndDate(
        newStartDate: Date,
        recurrenceType: String,
        recurrenceFrequency: Int
    ): Date {
        val cal = Calendar.getInstance()
        cal.time = newStartDate
        when (recurrenceType.uppercase(Locale.getDefault())) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, recurrenceFrequency)
            "MONTHLY" -> cal.add(Calendar.MONTH, recurrenceFrequency)
            "YEARLY" -> cal.add(Calendar.YEAR, recurrenceFrequency)
        }
        return cal.time
    }


    private fun setUi(lData: FarmerEntity?) {
        binding.apply {
            // Initialize UI components here
            lData?.let {
                crsViewModel.loadFarmerModules(lData)
                imgBack.setOnClickListener {
                    requireActivity().onBackPressed()
                }
                showQuickAdd = false
                edtSearch.addTextChangedListener {
                    setRecyclerData(crsViewModel.modules.value?.data, it.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

}