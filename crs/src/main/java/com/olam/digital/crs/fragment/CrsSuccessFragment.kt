package com.olam.digital.crs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutSurveyHomeBinding
import com.olam.digital.crs.databinding.LayoutSurveySubmitSuccessBinding
import com.olam.digital.crs.extension.hide
import com.olam.digital.crs.extension.millisToLocalTime
import com.olam.digital.crs.extension.show
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class CrsSuccessFragment : Fragment() {
    lateinit var binding: LayoutSurveySubmitSuccessBinding
    lateinit var navController: androidx.navigation.NavController
    private var backCallback: OnBackPressedCallback? = null
    private val crsViewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutSurveySubmitSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.apply {
            // Initialize UI components here
            layFilter.setOnClickListener {
                val startDesc = navController.graph.startDestinationId
                navController.popBackStack(startDesc,false)
            }
            backCallback = object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    Timber.e("Back Icon clicked")
                    return
                }

            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,backCallback!!)

            setUi()
            setObservers()
        }
    }

    private fun setObservers() {
        binding.apply {
            crsViewModel.apply {

            }
        }
    }

    override fun onDestroyView() {
        backCallback?.remove()
        backCallback = null
        super.onDestroyView()
    }

    private fun setUi() {
        binding.apply {
            crsViewModel.apply {
                layFilter.hide()
                showShimmer =true
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    showShimmer = false
                    requireActivity().runOnUiThread {
                        layFilter.show()
                        tvSurveyId.text = selectedModule?.value?.data?.name.toString()

                        if (isCrsFragment.value==true)
                        {
                            tvLabelFarmerId.text = getString(R.string.respondent_id)
                            tvFarmerId.text = selectedCrsProfiles.value?.data?.respondentCode
                        }
                        else{
                            tvFarmerId.text = selectedUser.value?.data?.olamFarmerId
                        }
                        tvSubmittedDate.text = System.currentTimeMillis().millisToLocalTime()
                    }
                    regSucess = true
                }
            }
        }
    }
}