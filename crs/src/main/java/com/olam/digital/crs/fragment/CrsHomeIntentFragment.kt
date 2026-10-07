package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutFragmentHomeCrsSelectBinding
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrsHomeIntentFragment: Fragment() {
lateinit var binding: LayoutFragmentHomeCrsSelectBinding
private val crsQuestionaryViewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            imgBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            layFarmerSurvey.setOnClickListener {
                crsQuestionaryViewModel.isCrsFragment.postValue(false)
                val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                navController.navigate(R.id.action_crsHomeIntentFragment_to_surveyHomeFragment)
            }
            layCommunitySurvey.setOnClickListener {
                crsQuestionaryViewModel.isCrsFragment.postValue(true)
                val navController = requireActivity().findNavController(R.id.fragmentContainerView)
                navController.navigate(R.id.action_crsHomeIntentFragment_to_crsProfileFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutFragmentHomeCrsSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

}