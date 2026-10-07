package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.ItemLayoutFragmentCrsBinding
import com.olam.digital.crs.databinding.LayoutFragmentCrsProfileBinding
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.room.CrsProfileEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrsProfileFragment : Fragment(){
    lateinit var binding: LayoutFragmentCrsProfileBinding
    private val viewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutFragmentCrsProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setUi()
            setObserver()
        }
    }

    fun setRecyclerData(farmerCrsProfile: List<CrsProfileEntity>) {
        binding.apply {
            // Set up RecyclerView adapter with farmerCrsProfile data
            recyclerView.setUpAdapter(farmerCrsProfile.toMutableList(),
                R.layout.item_layout_fragment_crs, ItemLayoutFragmentCrsBinding::inflate,
                { opt: CrsProfileEntity, _, bin ->
                    bin.apply {
                        // Bind data to views here, for example:
                        txtViewName.text = opt.profileName
                        bin.root.setOnClickListener {
                            // Handle item click, e.g., navigate to details
                            viewModel.selectedCrsProfile(opt)
                            val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                            navController.navigate(R.id.action_crsProfileFragment_to_surveyHomeFragment)
                        }
                    }
                })
        }
    }

    private fun setObserver() {
        binding.apply {
            viewModel.apply {
                farmersCrsProfile.observe(viewLifecycleOwner) { resource ->
                    when (resource.state) {
                        ResourceState.LOADING -> {
                            // Show loading state
                        }
                        ResourceState.SUCCESS -> {
                            val farmerCrsProfile = resource.data ?: emptyList()
                            // Update UI with farmers data
                            setRecyclerData(farmerCrsProfile)
                        }
                        ResourceState.ERROR -> {
                            val errorMessage = resource.message ?: "An error occurred"
                            // Show error message
                        }
                        ResourceState.NO_DATA -> {
                            // Show no data state
                        }
                    }
                }
            }
        }
    }

    private fun setUi() {
        binding.apply {
            viewModel.apply {
                getAllCrsProfile()
                imgBack.setOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}