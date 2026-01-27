package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutCrsConsentsBinding
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel

class CrsConsentFragment : Fragment() {
    lateinit var binding: LayoutCrsConsentsBinding
    private val crsViewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutCrsConsentsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setUi(null)
            setObservers()
        }
    }

    private fun setUi(lData: FarmerModuleView?) {
        binding.apply {
            crsViewModel.apply {
                lData.apply {
                    txtSurveyId.text = lData?.consentId?.toString() ?: "N/A"
                    txtFarmerId.text = crsViewModel.selectedUser.value?.data?.olamFarmerId ?: "N/A"
                    txtSurveyContent.text = lData?.consentText ?: "N/A"
                    imgBack.setOnClickListener {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    txtClear.setOnClickListener {
                        signaturePad.clear()
                    }

                    chkBox.setOnCheckedChangeListener { btn, isChecked ->
                        if (isChecked) {
                            btn.alpha = 1.0f
                        } else {
                            btn.alpha = 0.5f
                        }
                    }
                    layBottom.setOnClickListener {
                        if (!chkBox.isChecked) {
                            return@setOnClickListener
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragmentContainerView,
                                SurveyFragment()
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }
    }

    private fun setObservers() {
        binding.apply {
            crsViewModel.apply {
                selectedModule.observe(viewLifecycleOwner)
                {
                    when (it.state) {
                        ResourceState.NO_DATA -> {

                        }

                        ResourceState.ERROR -> {

                        }

                        ResourceState.LOADING -> {

                        }

                        ResourceState.SUCCESS -> {
                            setUi(it.data)
                        }
                    }
                }
            }
        }
    }


}