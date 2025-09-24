package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.olam.digital.ofis.MultiSpinnerSearch
import com.olam.digital.ofis.R
import com.olam.digital.ofis.databinding.FragmentFindFarmerAndStakeHolderBinding
import com.olam.digital.ofis.model.SpinnerDataDto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindFarmerAndStakeHolderFragment : Fragment() {
    private lateinit var binding: FragmentFindFarmerAndStakeHolderBinding
    private lateinit var navController: NavController
    private val viewModel : DistributionModuleViewModel by hiltNavGraphViewModels(R.id.distributionModuleNavigation)

    private lateinit var rvFarmerAndStakeHolder: RecyclerView
    private lateinit var adapter: FarmerAndStakeHolderAdapter
    private lateinit var context: Context
    private lateinit var farmerGroupSpinner: MultiSpinnerSearch
    private lateinit var sectionSpinner : MultiSpinnerSearch
    private lateinit var roleSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().setTitle(R.string.distribution)
        binding = FragmentFindFarmerAndStakeHolderBinding.inflate(layoutInflater, container, false)
        context = requireContext()
        farmerGroupSpinner = binding.spinnerFarmerGroup
        sectionSpinner = binding.spinnerSection
        roleSpinner = binding.spinnerRole

        initUiState()
        initObservers()
        initRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)

    }

    private fun initUiState() {
        val state = viewModel.farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
        val entityEnum = state.entity

        if (entityEnum == EntityEnum.FARMER) {
            binding.apply {
                view5.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                view6.setBackgroundColor(resources.getColor(R.color.inactive))
            }
        }
        else {
            binding.apply {
                view6.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                view5.setBackgroundColor(resources.getColor(R.color.inactive))
            }
        }

        binding.findFarmerSearchBox.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onEvent(DistributionModuleUiEvent.OnSearch(s.toString()))
            }

        })

        binding.apply {
            btnFarmer.setOnClickListener {
                view5.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                view6.setBackgroundColor(resources.getColor(R.color.inactive))
                viewModel.onEvent(DistributionModuleUiEvent.OnEntityChange(EntityEnum.FARMER))
            }

            btnStakeHolder.setOnClickListener {
                view6.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                view5.setBackgroundColor(resources.getColor(R.color.inactive))
                viewModel.onEvent(DistributionModuleUiEvent.OnEntityChange(EntityEnum.STAKEHOLDER))
            }

        }
    }

    private fun initRecyclerView() {
        rvFarmerAndStakeHolder = binding.rvFarmerAndStakeHolder
        setFarmerAndStakeHolderView()
    }

    private fun initObservers() {
        val state = viewModel.farmerOrStakeHolderState
        state.observe(viewLifecycleOwner) { state ->
            val noStatusMessage = if (state.entity == EntityEnum.FARMER) getString(R.string.farmer_not_found) else getString(R.string.stakeholder_not_found)
            if (state.data.isEmpty()) {
                rvFarmerAndStakeHolder.visibility = View.GONE
                binding.tvNoFarmerFound.visibility = View.VISIBLE
                binding.tvNoFarmerFound.text = noStatusMessage
            }
            else {
                adapter.updateSupplierList(state.data)
                rvFarmerAndStakeHolder.visibility = View.VISIBLE
                binding.tvNoFarmerFound.visibility = View.GONE
            }
            val id = if (state.entity == EntityEnum.FARMER) R.string.primary_farmers_list else R.string.Stakeholders

            if (state.entity == EntityEnum.FARMER) {
                binding.apply {
                    findFarmerSearchBox.hint = getString(R.string.farmer_search_hint)
                    val params = findFarmerSearchBox.layoutParams as LinearLayout.LayoutParams
                    params.weight = 1f
                    findFarmerSearchBox.layoutParams = params
                    roleSpinner.visibility = View.GONE
                }
            }
            else {
                binding.apply {
                    findFarmerSearchBox.hint = getString(R.string.stakeholder_hint)
                    val params = findFarmerSearchBox.layoutParams as LinearLayout.LayoutParams
                    params.weight = 0.5f
                    findFarmerSearchBox.layoutParams = params
                    roleSpinner.visibility = View.VISIBLE
                }
            }
            binding.tvListName.text = resources.getString(id)

            roleSpinner.setSelection(getPositionByRoleId(state.roleId))
        }

        viewModel.farmerGroups.observe(viewLifecycleOwner) { farmerGroupList ->
            bindSpinnerData(farmerGroupList, farmerGroupSpinner, R.string.spinner_farmergroup_default, state.value?.farmerGroupId ?: emptyList())
        }

        viewModel.sections.observe(viewLifecycleOwner) { sectionList ->
            bindSpinnerData(sectionList, sectionSpinner, R.string.spinner_section_default, state.value?.sectionId ?: emptyList())
        }

        viewModel.roles.observe(viewLifecycleOwner) { roles ->
            bindFarmerRoleSpinner(roles.toMutableList())
        }
    }

    private fun getPositionByRoleId(roleId: Int): Int {
        viewModel.roles.value?.forEachIndexed { index, spinnerDataDto ->
            if (roleId == spinnerDataDto.id) {
                return index + 1
            }
        }
        return 0
    }

    private fun setFarmerAndStakeHolderView() {
        val data = viewModel.farmerOrStakeHolderState.value?.data ?: FarmerAndStakeHolderState().data
        adapter = FarmerAndStakeHolderAdapter(data.toMutableList()) { position ->
            viewModel.onEvent(DistributionModuleUiEvent.OnItemClick(position))
            navController.navigate(R.id.find_farmer_and_stake_holder_to_input_or_finance_distribution)
        }

        rvFarmerAndStakeHolder.adapter = adapter
        rvFarmerAndStakeHolder.layoutManager = LinearLayoutManager(requireContext())
        viewModel.onEvent(DistributionModuleUiEvent.LoadFarmerOrStakeHolderData)
    }

    private fun bindSpinnerData(
        farmerGroupList: List<SpinnerDataDto>,
        spinner: MultiSpinnerSearch,
        stringId: Int,
        selectedIds : List<Int>
    ) {
        bindSpinnerSpec(spinner, getString(stringId))

        val spinnerDataStatus = farmerGroupList.map {
            KeyPairBoolData().apply {
                id = it.id.toLong()
                name = it.name
                isSelected = selectedIds.contains(it.id)
            }
        }

        val eventDispatcher: (List<Int>) -> DistributionModuleUiEvent = { id ->
            when (stringId) {
                R.string.spinner_farmergroup_default -> DistributionModuleUiEvent.OnFarmerGroupSelect(id)
                else -> DistributionModuleUiEvent.OnSectionSelect(id)
            }
        }

        spinner.setItems(spinnerDataStatus) { selectedItems ->
            viewModel.onEvent(eventDispatcher(
                selectedItems.map { item ->
                    item.id.toInt()
                }
            ))
        }
    }


    private fun bindSpinnerSpec(
        multiSpinnerSearch: MultiSpinnerSearch,
        hintText: String?
    ) {
        multiSpinnerSearch.isSearchEnabled = true
        multiSpinnerSearch.setSearchHint(hintText)
        multiSpinnerSearch.setEmptyTitle("Not Data Found!")
        multiSpinnerSearch.isShowSelectAllButton = false
        multiSpinnerSearch.setClearText(null)
    }

    private fun bindFarmerRoleSpinner(roles: MutableList<SpinnerDataDto>) {
        roles.add(0, SpinnerDataDto(0, getString(R.string.stakeholder_role)))
        val roleNames = roles.map { it.name }.toMutableList()
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, roleNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                view?.let {
                    viewModel.onEvent(DistributionModuleUiEvent.OnRoleSelect(roles[position].id))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


    }
}