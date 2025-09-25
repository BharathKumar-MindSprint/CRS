package com.olam.digital.ofis.distributionmodule.distribution_domain.repository

import android.content.ContentValues
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import com.olam.digital.ofis.distributionmodule.distribution_presentation.EntityEnum
import com.olam.digital.ofis.model.SpinnerDataDto

interface DistributionModuleRepository {

    suspend fun searchFarmerOrStakeHolder(
        query : String,
        farmerGroupId : List<Int>,
        sectionId : List<Int>,
        entityName : EntityEnum,
        roleId : Int
    ) : Result<List<FarmerAndStakeHolderDto>>

    suspend fun saveDistributionForm(distributionFormDto: DistributionFormDto)

    suspend fun getSpinnerData() : Result<List<List<SpinnerDataDto>>>

    suspend fun getDistributionGroups(categoryId : Int) : Result<List<SpinnerDataDto>>

    suspend fun getDistributionItems(distributionId : String) : Result<List<SpinnerDataDto>>

    suspend fun insertNetworkQueueEntry(values: ContentValues): Long

    suspend fun getDistributionDetails(farmerId: String?, stakeHolderId: String?): Result<DistributionFormDto>

}