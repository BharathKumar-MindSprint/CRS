package com.olam.digital.ofis.distributionmodule.distribution_data.mapper

import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder

fun MultiStakeHolder.toStakeHolder() : FarmerAndStakeHolderDto {
    return FarmerAndStakeHolderDto(
        name = msName,
        id = farmerId.toString(),
        displayId = olamMultiStakeholderId,
        stakeHolderId = msId.toString()
    )
}