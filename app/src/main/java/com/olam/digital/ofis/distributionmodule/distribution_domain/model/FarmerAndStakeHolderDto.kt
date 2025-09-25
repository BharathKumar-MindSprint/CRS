package com.olam.digital.ofis.distributionmodule.distribution_domain.model

data class FarmerAndStakeHolderDto(
    val name:String,
    val id: String,
    val displayId: String,
    val stakeHolderId : String? = null
)
