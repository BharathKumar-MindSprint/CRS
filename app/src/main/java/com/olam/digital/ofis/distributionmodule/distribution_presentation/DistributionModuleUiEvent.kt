package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.graphics.Bitmap
import android.net.Uri
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import java.io.File

sealed class DistributionModuleUiEvent {
    data class OnSearch(val name: String) : DistributionModuleUiEvent()
    data class OnFarmerGroupSelect(val id : List<Int>) : DistributionModuleUiEvent()
    data class OnSectionSelect(val id : List<Int>) : DistributionModuleUiEvent()
    data class OnRoleSelect(val id : Int) : DistributionModuleUiEvent()
    data class OnEntityChange(val enum: EntityEnum) : DistributionModuleUiEvent()
    data class OnItemClick(val position: Int) : DistributionModuleUiEvent()
    data object LoadFarmerOrStakeHolderData : DistributionModuleUiEvent()
    data class OnCategoryChange(val enum: CategoryEnum) : DistributionModuleUiEvent()
    data class OnDistributionGroupChange(val position: Int) : DistributionModuleUiEvent()
    data class OnDistributionItemChange(val position: Int) : DistributionModuleUiEvent()
    data class OnUOMChange(val position: Int) : DistributionModuleUiEvent()
    data class UpdateDistributionFormDetails(
        val signatureFileName : String,
        val signatureUrl : String,
        val bitmap: Bitmap,
        val transactionValue : String,
        val pricePerUnit : String,
        val totalAmount : String,
    ) : DistributionModuleUiEvent()
    data object SaveDistributionForm : DistributionModuleUiEvent()
    data object ResetDistributionFormState : DistributionModuleUiEvent()
    data class EvidenceUpload(val uri : Uri, val filePath : String) : DistributionModuleUiEvent()
    data object GetDistributionData : DistributionModuleUiEvent()
    data object LoadDistributionSpinnerData : DistributionModuleUiEvent()
    data class setFarmerAndStakeHolderData(val farmerAndStakeHolderDto: FarmerAndStakeHolderDto, val tempId : String?) : DistributionModuleUiEvent()
}