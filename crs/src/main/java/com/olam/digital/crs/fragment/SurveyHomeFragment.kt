package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.ItemsSurveyListBinding
import com.olam.digital.crs.databinding.LayoutSurveyHomeBinding
import com.olam.digital.crs.extension.hide
import com.olam.digital.crs.extension.invisible
import com.olam.digital.crs.extension.isVisible
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.ShowRecycler
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import timber.log.Timber
import kotlin.getValue

class SurveyHomeFragment : Fragment() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            binding.imgBack.setOnClickListener {
                // Prefer NavController pop when inside a nav flow
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            // Initialize UI components here
            setUi()
            Timber.e("Is Imgback visible ${imgBack.isVisible()}")

            setObservers()
        }
    }

    fun setRecyclerData(farmerList: List<FarmerEntity>?, searchText: String? = null) {
        binding.apply {
            crsViewModel.apply {
                Timber.d(
                    "setRecyclerData(): total=%d, searchText=%s",
                    farmerList?.size ?: -1,
                    searchText
                )
                farmerList?.let {
                    val filteredList = if (!searchText.isNullOrEmpty()) {
                        it.filter { farmer ->
                            farmer.farmerName?.contains(searchText, ignoreCase = true) == true ||
                                    farmer.olamFarmerId?.contains(
                                        searchText,
                                        ignoreCase = true
                                    ) == true ||
                                    farmer.location?.contains(searchText, ignoreCase = true) == true
                        }.toMutableList()
                    } else {
                        it.toMutableList()
                    }
                    mRecylerView.setUpAdapter(
                        filteredList, R.layout.items_survey_list,
                        ItemsSurveyListBinding::inflate, { item, position, bi ->
                            bi.apply {
                                txtSurveyName.text = item.farmerName
                                txtSurveyDesc.text = item.olamFarmerId
                                txtNoOfQuestions.text = item.farmCode//need to get farmer location
                                layDisplayStatus.hide()
                                bi.root.setOnClickListener {
                                   onItemClick(item)
                                }
                            }
                        })


                    Timber.d("setRecyclerData(): filtered=%d", filteredList.size)
                    binding.mRecylerView.post {
                        Timber.d(
                            "RV attached=%s, adapter=%s, itemCount=%d",
                            binding.mRecylerView.isAttachedToWindow,
                            binding.mRecylerView.adapter?.javaClass?.simpleName,
                            binding.mRecylerView.adapter?.itemCount ?: -1
                        )
                    }


                }
            }
        }
    }


    // Reusable adapter setup method
    fun setCrsAdapter(
        recycler: RecyclerView,
        data: MutableList<RespondentEntity>,
        isNew: Boolean = false
    ) {
        val navController = findNavController()
        recycler.setUpAdapter(
            data,
            R.layout.items_survey_list,
            ItemsSurveyListBinding::inflate,
         { item, _, bi ->
            bi.apply {
                txtSurveyName.text = item.name
                txtSurveyDesc.text = "${item.countryCode} ${item.phoneNumber}"
                txtNoOfQuestions.text = item.respondentCode
                layDisplayStatus.hide()

                if (isNew) {
                    mainRoot.background = ContextCompat.getDrawable(
                        mainRoot.context,
                        R.drawable.rounded_corner_green_edge
                    )
                    txtNoOfQuestions.invisible()
                }

                root.setOnClickListener {
                    onItemClickRespondent(item)
                }
            }
        })
    }

    private fun onItemClickRespondent(item: RespondentEntity) {
        Timber.e("Current NavDestination = ${findNavController().currentDestination?.id}")
        crsViewModel.selectedCrsProfile(item)
        findNavController().navigate(R.id.action_surveyHomeFragment_to_surveySurveyListFragment)
    }

    private fun onItemClick(item: FarmerEntity) {
        Timber.e("Current NavDestination = ${findNavController().currentDestination?.id}")
        crsViewModel.selectedUser(item)
        findNavController().navigate(R.id.action_surveyHomeFragment_to_surveySurveyListFragment)
    }

    fun setRecyclerCrsData(
        farmerList: List<RespondentEntity>?,
        searchText: String? = null
    ) = binding.apply {

        crsViewModel.apply {

            val actualList = farmerList ?: emptyList()
            Timber.d("setRecyclerCrsData(): total=%d", actualList.size)

            // Filter list by user search text
            val filteredList = if (!searchText.isNullOrEmpty()) {
                actualList.filter { farmer ->
                    farmer.name?.contains(searchText, ignoreCase = true) == true ||
                            farmer.respondentCode?.contains(searchText, ignoreCase = true) == true
                }.toMutableList()
            } else {
                actualList.toMutableList()
            }

            // Split by synced status
            val newFarmers = filteredList.filter { it.isSynced == 1 }
            val oldFarmers = filteredList.filter { it.isSynced == 0 }

            Timber.d(
                "setRecyclerCrsData(): synced=%d, unsynced=%d",
                newFarmers.size, oldFarmers.size
            )

            action = if (newFarmers.isEmpty())  ShowRecycler.SHOW_FIRST_RECYLCERVIEW else ShowRecycler.SHOW_SECOND_RECYLCERVIEW

            if (action==ShowRecycler.SHOW_FIRST_RECYLCERVIEW) {
                // Show only the full list
                setCrsAdapter(mRecylerView, filteredList)
            } else {
                // Show segregated new vs old
                setCrsAdapter(mNewRegisteredRecylerView, newFarmers.toMutableList(), isNew = true)
                setCrsAdapter(mExitingRecylerView, oldFarmers.toMutableList())
            }

            Timber.d("setRecyclerCrsData(): displayed=%d", filteredList.size)
        }
    }



    private fun setObservers() {
        // Set up observers here
        binding.apply {
            crsViewModel.apply {
                farmersCrs.observe(viewLifecycleOwner) { farmersList ->
                    when (farmersList.state) {
                        ResourceState.SUCCESS -> {
                            val data = farmersList.data
                            action = ShowRecycler.SHOW_FIRST_RECYLCERVIEW
                            Timber.d("Farmers fetched CRS viewmodel: ${data?.size}")
                            setRecyclerData(data)
                        }

                        ResourceState.ERROR -> {
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }

                        ResourceState.LOADING -> {
                            action = ShowRecycler.SHOW_SHIMMER
                        }

                        ResourceState.NO_DATA -> {

                        }
                    }
                }

                crsProfileCommunity.observe(viewLifecycleOwner) { farmersList ->
                    when (farmersList.state) {
                        ResourceState.SUCCESS -> {
                            action = ShowRecycler.SHOW_SECOND_RECYLCERVIEW
                            val data = farmersList.data
                            Timber.d("Farmers fetched CRS viewmodel: ${data?.size}")
                            setRecyclerCrsData(data)
                        }

                        ResourceState.ERROR -> {
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }

                        ResourceState.LOADING -> {
                            action = ShowRecycler.SHOW_SHIMMER
                        }

                        ResourceState.NO_DATA -> {
                            action = ShowRecycler.SHOW_NODATA_RECYLCERVIEW
                        }
                    }
                }
            }
        }
    }

    private fun setUi() {
        binding.apply {
            Timber.d("SurveyHomeFragment: setUi() called")
            crsViewModel.apply {
                if (isCrsFragment.value == false) {
                    edtSearch.hint = getString(R.string.search_farmer)
                    getAllFarmersCrs()
                    showQuickAdd = false
                } else {
                    edtSearch.hint = getString(R.string.search_respondent)
                    getAllCrsRespondents(crsViewModel.selectedCrsProfileData.value?.data?.id?:0)
                    showQuickAdd = true
                }

            }
            laylast.setOnClickListener {
                val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                navController.navigate(R.id.action_surveyHomeFragment_to_crsCommunityRegisterFragment)
            }

            //getAllFarmers()
            //crsViewModel.getAllFarmersCrs()
            edtSearch.addTextChangedListener {
                if (crsViewModel.isCrsFragment.value == false) {
                    setRecyclerData(crsViewModel.farmersCrs.value?.data, it.toString())
                } else {
                    setRecyclerCrsData(crsViewModel.crsProfileCommunity.value?.data, it.toString())
                }
            }

            Timber.e("AFTER setUi/Observers: visible=${binding.layOne.isShown}, size=${binding.layOne.width}x${binding.layOne.height}")
            // Re-apply the click listener LAST in case earlier code overrides it
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

}