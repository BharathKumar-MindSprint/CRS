package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.content.ContentValues
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olam.digital.ofis.OFISDBContract
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.DistributionModuleUseCases
import com.olam.digital.ofis.model.SpinnerDataDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.sql.Timestamp
import javax.inject.Inject

@HiltViewModel
class DistributionModuleViewModel @Inject constructor(
    private val distributionModuleUseCases: DistributionModuleUseCases
) : ViewModel() {

    private var _farmerOrStakeHolderState  : MutableLiveData<FarmerAndStakeHolderState> = MutableLiveData(FarmerAndStakeHolderState())
    val farmerOrStakeHolderState : LiveData<FarmerAndStakeHolderState> = _farmerOrStakeHolderState

    private var _distributionFormState : MutableLiveData<DistributionFormState> = MutableLiveData(DistributionFormState())
    val distributionFormState : LiveData<DistributionFormState> = _distributionFormState

    private var _status : MutableLiveData<Boolean> = MutableLiveData(false)
    val status : LiveData<Boolean> = _status

    private var _farmerGroups : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf())
    val farmerGroups : LiveData<List<SpinnerDataDto>> = _farmerGroups

    private var _sections : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf())
    val sections : LiveData<List<SpinnerDataDto>> = _sections

    private var _roles : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf())
    val roles : LiveData<List<SpinnerDataDto>> = _roles

    private var _distributionCategory : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf())
    val distributionCategory : LiveData<List<SpinnerDataDto>> = _distributionCategory

    private var _distributionGroups : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf(
        SpinnerDataDto(0, "")
    ))
    val distributionGroups : LiveData<List<SpinnerDataDto>> = _distributionGroups

    private var _distributionItem : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf(
        SpinnerDataDto(0, "")
    ))
    val distributionItem : LiveData<List<SpinnerDataDto>> = _distributionItem

    private var _currency : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf(
        SpinnerDataDto(0, "", "")
    ))
    val currency : LiveData<List<SpinnerDataDto>> = _currency

    private var _localUnits : MutableLiveData<List<SpinnerDataDto>> = MutableLiveData(mutableListOf(
        SpinnerDataDto(0, "", "")
    ))
    val localUnits : LiveData<List<SpinnerDataDto>> = _localUnits

    var isFromSyncScreen : MutableLiveData<Boolean> = MutableLiveData(null)

    init {
        loadFarmerOrStakeHolderData()
        getSpinnerData()
    }

    private val _uiEvent = Channel<DistributionModuleUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: DistributionModuleUiEvent) {
        when (event) {
            is DistributionModuleUiEvent.OnSearch -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.searchQuery = event.name
                loadFarmerOrStakeHolderData()
            }
            is DistributionModuleUiEvent.OnEntityChange -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                if (currentState.entity != event.enum) {
                    currentState.entity = event.enum
                    currentState.searchQuery = ""
                    loadFarmerOrStakeHolderData()
                }
            }
            is DistributionModuleUiEvent.OnFarmerGroupSelect -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.farmerGroupId = event.id.toMutableList()
                loadFarmerOrStakeHolderData()
            }
            is DistributionModuleUiEvent.OnSectionSelect -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.sectionId = event.id.toMutableList()
                loadFarmerOrStakeHolderData()
            }
            is DistributionModuleUiEvent.OnRoleSelect -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.roleId = event.id
                loadFarmerOrStakeHolderData()
            }
            is DistributionModuleUiEvent.OnItemClick -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.selectedFarmerOrStakeHolder = currentState.data[event.position]
            }
            is DistributionModuleUiEvent.LoadFarmerOrStakeHolderData -> {
                loadFarmerOrStakeHolderData()
            }
            is DistributionModuleUiEvent.OnCategoryChange -> {
                val currentState = _distributionFormState.value
                val categoryId = getCategoryId(event.enum)
                val newState = DistributionFormState(
                    distributionCategory = event.enum,
                    categoryId = categoryId,
                    id = currentState?.id,
                    tempId = currentState?.tempId)
                loadDistributionGroup()
                //loadUnits(event.enum)
                _distributionFormState.postValue(newState)

            }
            is DistributionModuleUiEvent.UpdateDistributionFormDetails -> {
                val currentState = _distributionFormState.value ?: DistributionFormState()
                val updateState = currentState.copy(
                    signatureFileName = event.signatureFileName,
                    signatureUrl = event.signatureUrl,
                    signatureBitmap = event.bitmap,
                    transactionValue = event.transactionValue,
                    pricePerUnit = event.pricePerUnit,
                    totalAmount = event.totalAmount
                )
                _distributionFormState.value = updateState
            }
            is DistributionModuleUiEvent.SaveDistributionForm -> {
                saveDistributionForm()
            }

            is DistributionModuleUiEvent.OnDistributionGroupChange -> {
                val currentState = _distributionFormState.value ?: DistributionFormState()
                val distributionGroup = _distributionGroups.value?.get(event.position) ?: SpinnerDataDto(0, "")

                currentState.distributionGroupId = distributionGroup.id.toString()
                currentState.distributionGroupName = distributionGroup.name
                loadDistributionItem()
            }
            is DistributionModuleUiEvent.OnDistributionItemChange -> {
                val currentState = _distributionFormState.value ?: DistributionFormState()
                val distributionGroup = _distributionItem.value?.get(event.position) ?: SpinnerDataDto(0,"")

                currentState.distributionItemId = distributionGroup.id.toString()
                currentState.distributionItemName = distributionGroup.name
            }

            DistributionModuleUiEvent.ResetDistributionFormState -> {
                _distributionFormState.postValue(DistributionFormState())
            }

            is DistributionModuleUiEvent.EvidenceUpload -> {
                val currentState = _distributionFormState.value ?: DistributionFormState()
                currentState.evidenceUri = event.uri
                currentState.evidenceName = event.filePath
            }

            is DistributionModuleUiEvent.OnUOMChange -> {
                val currentState = _distributionFormState.value ?: DistributionFormState()
                val data = if (currentState.distributionCategory == CategoryEnum.INPUT) {
                    _localUnits.value?.get(event.position)
                } else {
                    _currency.value?.get(event.position)
                } ?: SpinnerDataDto(0, "", "", false)

                currentState.localUnitId = if (data.isGlobalId != true) data.id else null
                currentState.currencyId = if (data.isGlobalId == true) data.id else null
                currentState.uomName = "${data.notation} (${data.name})"

            }

            is DistributionModuleUiEvent.GetDistributionData -> {
                getDistributionDetails()
            }

            is DistributionModuleUiEvent.LoadDistributionSpinnerData -> {
                loadDistributionGroup()
                loadDistributionItem()
            }

            is DistributionModuleUiEvent.setFarmerAndStakeHolderData -> {
                val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()
                currentState.selectedFarmerOrStakeHolder = event.farmerAndStakeHolderDto

                val currentDistributionForm = _distributionFormState.value ?: DistributionFormState()
                currentDistributionForm.tempId = event.tempId
            }
        }
    }

    private fun getCategoryId(enum: CategoryEnum): Int {
        val enumValue = if (enum == CategoryEnum.INPUT) "input" else "finance"
        val category = distributionCategory.value ?: mutableListOf()

        category.forEach {
            if (it.name.lowercase() == enumValue) {
                return it.id
            }
        }
        return if (enum == CategoryEnum.INPUT) 1 else 2
    }

    private fun loadDistributionGroup() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _distributionFormState.value ?: DistributionFormState()
            val result = distributionModuleUseCases.getSpinnerData.getDistributionGroups(currentState.categoryId).getOrDefault(
                mutableListOf(SpinnerDataDto(0, ""))
            )

            _distributionGroups.postValue(result)
        }
    }

    private fun loadUnits(enum: CategoryEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            val result: List<List<SpinnerDataDto>> = distributionModuleUseCases.getSpinnerData.getUnitsData()
                .getOrDefault(
                    listOf(
                        listOf(SpinnerDataDto(0, "", "", false)),
                        listOf(SpinnerDataDto(0, "", "", false))
                    )
                )
            if (enum == CategoryEnum.INPUT) _localUnits.postValue(result.getOrNull(1) ?: emptyList()) else _currency.postValue(result.getOrNull(0) ?: emptyList())
        }
    }

    private fun loadDistributionItem() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _distributionFormState.value ?: DistributionFormState()
            val result = distributionModuleUseCases.getSpinnerData.getDistributionItems(currentState.distributionGroupId).getOrDefault(
                mutableListOf(SpinnerDataDto(0, ""))
            )

            _distributionItem.postValue(result)
        }
    }

    private fun loadFarmerOrStakeHolderData() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _farmerOrStakeHolderState.value ?: FarmerAndStakeHolderState()

            val result = distributionModuleUseCases.searchFarmerOrStakeHolder(
                currentState.searchQuery,
                currentState.farmerGroupId,
                currentState.sectionId,
                currentState.entity,
                currentState.roleId
            )

            val updatedState = currentState.copy(data = result.getOrDefault(emptyList()))
            _farmerOrStakeHolderState.postValue(updatedState)
        }
    }

    private fun saveDistributionForm() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentState = _distributionFormState.value ?: return@launch
                val farmerOrStakeHolderState = _farmerOrStakeHolderState.value ?: return@launch

                //val username = AuthenticationHelper(context).getuserName().toLong()
                val time = System.currentTimeMillis()
                val olamFarmerId = currentState.tempId ?: (time).toString()

                distributionModuleUseCases.saveDistributionForm(
                    DistributionFormDto(
                        id = currentState.id,
                        distributionId = olamFarmerId,
                        distributionCategoryId = currentState.categoryId,
                        distributionItemId = currentState.distributionItemId.toInt(),
                        distributionGroupId = currentState.distributionGroupId.toInt(),
                        price = currentState.pricePerUnit,
                        localUnitId = currentState.localUnitId,
                        currencyId = currentState.currencyId,
                        totalAmount = currentState.totalAmount,
                        transactionValue = currentState.transactionValue,
                        evidenceUrl = currentState.evidenceName,
                        signatureUrl = currentState.signatureFileName,
                        farmerId = farmerOrStakeHolderState.selectedFarmerOrStakeHolder?.id ?: return@launch,
                        stakeHolderId = farmerOrStakeHolderState.selectedFarmerOrStakeHolder?.stakeHolderId
                    )
                )

                val nqiValues = ContentValues()
                if (currentState.id == null) {
                    val farmerAndStakeHolder = _farmerOrStakeHolderState.value?.selectedFarmerOrStakeHolder
                    val timestamp = Timestamp(System.currentTimeMillis())
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "Distribution Form")
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmerAndStakeHolder?.name ?: "")
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "Distribution Form")
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID, olamFarmerId)
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerAndStakeHolder?.id)
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, farmerAndStakeHolder?.stakeHolderId)
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, farmerAndStakeHolder?.displayId ?: "")
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString())
                    nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString())

                    val insertedId = distributionModuleUseCases.saveDistributionForm.insertNetworkQueue(nqiValues)
                    if (insertedId > 0) {
                        _status.postValue(true)
                    }
                    else {
                        _status.postValue(false)
                    }
                }
                else {
                    _status.postValue(true)
                }
            }
            catch (e : Exception) {
                e.printStackTrace()
                _status.postValue(false)
            }
        }
    }

    private fun getSpinnerData() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = distributionModuleUseCases.getSpinnerData().getOrDefault(emptyList())

            if (result.isNotEmpty()) {
                _farmerGroups.postValue(result[0])
                _sections.postValue(result[1])
                _roles.postValue(result[2])
                _distributionCategory.postValue(result[3])
                _distributionGroups.postValue(result[4])
                _currency.postValue(result[5])
                _localUnits.postValue(result[6])
            }
        }
    }

    private fun getDistributionDetails() {
        val distributionId = _distributionFormState.value?.tempId
        val id = _distributionFormState.value?.id

        viewModelScope.launch(Dispatchers.IO) {
            if (isFromSyncScreen.value == null || id != null) {
                return@launch
            }


            var category = _distributionCategory.value ?: mutableListOf()

            if (category.isEmpty()) {
                val result = distributionModuleUseCases.getSpinnerData.getCategory().getOrDefault(
                    mutableListOf()
                )
                category = result
                _distributionCategory.postValue(result)
            }

            val distributionDetails = distributionId?.let { id ->
                distributionModuleUseCases.getDistributionDetails(id).getOrDefault(null)
            }

            val currentDistributionFormState = _distributionFormState.value ?: DistributionFormState()
            val categoryName = category
                .find { it.id == distributionDetails?.distributionCategoryId }
                ?.name?.lowercase()

            val result: List<List<SpinnerDataDto>> = distributionModuleUseCases.getSpinnerData.getUnitsData()
                .getOrDefault(
                    listOf(
                        listOf(SpinnerDataDto(0, "", "", false)),
                        listOf(SpinnerDataDto(0, "", "", false))
                    )
                )

            _localUnits.postValue(result.getOrNull(1) ?: emptyList())
            _currency.postValue(result.getOrNull(0) ?: emptyList())


            distributionDetails.let {
                if (distributionDetails != null) {
                    val state = currentDistributionFormState.copy(
                        id = distributionDetails.id,
                        tempId = distributionDetails.distributionId,
                        distributionGroupId = distributionDetails.distributionGroupId.toString(),
                        distributionItemId = distributionDetails.distributionItemId.toString(),
                        distributionCategory = if (categoryName == "input") CategoryEnum.INPUT else CategoryEnum.FINANCE,
                        categoryId = distributionDetails.distributionCategoryId,
                        currencyId = distributionDetails.currencyId,
                        transactionValue = distributionDetails.transactionValue,
                        localUnitId = distributionDetails.localUnitId,
                        pricePerUnit = distributionDetails.price ?: "",
                        totalAmount = distributionDetails.totalAmount ?: "",
                        signatureUrl = distributionDetails.signatureUrl,
                        evidenceName = distributionDetails.evidenceUrl,

                    )
                    _distributionFormState.postValue(state)
                }
            }
        }
    }

}