package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.olam.digital.ofis.R
import com.olam.digital.ofis.databinding.FragmentDistributionSummaryBinding

class DistributionSummaryFragment : Fragment() {
    private lateinit var binding: FragmentDistributionSummaryBinding
    private lateinit var navController : NavController
    private lateinit var viewModel : DistributionModuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DistributionModuleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDistributionSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)

        initUiState()
        initObservers()

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)
    }

    @SuppressLint("SetTextI18n")
    private fun initUiState() {
        val distributionFormState = viewModel.distributionFormState.value ?: DistributionFormState()
        val state = viewModel.farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()

        binding.apply {
            tvName.text = state.selectedFarmerOrStakeHolder?.name ?: ""
            tvDisplayId.text = state.selectedFarmerOrStakeHolder?.displayId ?: ""
            tvDistributionGroup.text = distributionFormState.distributionGroupName
            btnCategory.text = if(distributionFormState.distributionCategory == CategoryEnum.INPUT) getString(R.string.input) else getString(R.string.finance)
            tvDistributionItem.text = distributionFormState.distributionItemName
            tvTransactionValue.text = "${distributionFormState.transactionValue} ${distributionFormState.uomName}"
            tvPricePerUnit.text = distributionFormState.pricePerUnit
            tvTotalAmount.text = distributionFormState.totalAmount
            tvAttachedFileName.text = distributionFormState.evidenceName
            ivSignature.setImageBitmap(distributionFormState.signatureBitmap)
            clPriceAndTotal.visibility = if (distributionFormState.distributionCategory == CategoryEnum.INPUT) View.VISIBLE else View.GONE
        }

        binding.btnNext.setOnClickListener {
            viewModel.onEvent(DistributionModuleUiEvent.SaveDistributionForm)
        }

        binding.btnBack.setOnClickListener {
            navController.popBackStack()
        }

    }

    private fun initObservers() {
        viewModel.status.observe(viewLifecycleOwner) { value ->
            if (value) {
                requireActivity().finish()
            }
        }
    }
}