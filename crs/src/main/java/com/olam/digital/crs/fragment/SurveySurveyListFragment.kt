package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.ItemsSurveyListBinding
import com.olam.digital.crs.databinding.LayoutSurveyHomeBinding
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.extension.show
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
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
            setUi(null)
            setObservers()
        }
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
                        }

                        ResourceState.SUCCESS -> {
                            // Handle success state if needed
                            setRecyclerData(it.data)
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
                mRecylerView.setUpAdapter(
                    items = filteredList, R.layout.items_survey_list,
                    ItemsSurveyListBinding::inflate,
                    { items, pos, bin ->
                        bin.apply {
                            txtSurveyName.text = items.name
                            txtSurveyDesc.text = "${getString(R.string.module_id)} ${items.moduleId}"
                            txtNoOfQuestions.text = "${items.questionCount} ${getString(R.string.questions)}"
                            layDisplayStatus.show()

                            txtActiveState.text = if (items.statusFinished == 1) {
                                getString(R.string.completed)
                            } else if (items.statusStarted == 1) {
                                getString(R.string.in_progress)
                            } else {
                                getString(R.string.active)
                            }


                            //setbackgroudnd
                            layDisplayStatus.setBackgroundResource(
                                if (items.statusFinished == 1) {
                                    R.drawable.bg_status_completed
                                } else if (items.statusStarted == 1) {
                                    R.drawable.bg_status_inprogress
                                } else {
                                    R.drawable.bg_fill_green_color
                                }
                            )

                            root.setOnClickListener {
                                // Handle module click
                                crsViewModel.selectModule(items)
                                crsViewModel.loadModule(items.moduleId)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.fragmentContainerView,
                                        CrsConsentFragment()
                                    )
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    })
            }
        }
    }

    private fun setUi(lData: FarmerEntity?) {
        binding.apply {
            // Initialize UI components here
            lData?.let {
                crsViewModel.loadFarmerModules(lData)
                imgBack.setOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
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