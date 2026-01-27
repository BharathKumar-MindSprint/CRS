package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.ItemsSurveyListBinding
import com.olam.digital.crs.databinding.LayoutSurveyHomeBinding
import com.olam.digital.crs.extension.hide
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import timber.log.Timber

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setUi()
            setObservers()
        }
    }

    fun setRecyclerData(farmerList: List<FarmerEntity>?,searchText:String?=null) {
        binding.apply {
            crsViewModel.apply {
                Timber.d("setRecyclerData(): total=%d, searchText=%s", farmerList?.size ?: -1,searchText)
                farmerList?.let {
                    val filteredList = if (!searchText.isNullOrEmpty()) {
                        it.filter { farmer ->
                            farmer.farmerName?.contains(searchText, ignoreCase = true)==true ||
                            farmer.olamFarmerId?.contains(searchText, ignoreCase = true)==true ||
                            farmer.location?.contains(searchText, ignoreCase = true)==true
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
                                    crsViewModel.selectedUser(item)
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(
                                            R.id.fragmentContainerView,
                                            SurveySurveyListFragment()
                                        )
                                        .addToBackStack(null)
                                        .commit()
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

    private fun setObservers() {
        // Set up observers here
        binding.apply {
            crsViewModel.apply {
                farmersCrs.observe(viewLifecycleOwner) { farmersList ->
                    when (farmersList.state) {
                        ResourceState.SUCCESS -> {
                            val data = farmersList.data
                            Timber.d("Farmers fetched CRS viewmodel: ${data?.size}")
                            setRecyclerData(data)
                        }

                        ResourceState.ERROR -> {

                        }

                        ResourceState.LOADING -> {

                        }

                        ResourceState.NO_DATA -> {

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
                getAllFarmersCrs()
            }
            // Initialize UI components here
            imgBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            showQuickAdd = true
            //getAllFarmers()
            crsViewModel.getAllFarmersCrs()
            edtSearch.addTextChangedListener {
               setRecyclerData(crsViewModel.farmersCrs.value?.data,it.toString())
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