package com.olam.digital.ofis.distributionmodule.distribution_presentation

import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto

data class FarmerAndStakeHolderState(
    var entity : EntityEnum = EntityEnum.FARMER,
    val data : List<FarmerAndStakeHolderDto> = emptyList(),
    val productId : Int = 0,
    val countryId : Int = 0,
    var farmerGroupId : MutableList<Int> = mutableListOf(),
    var sectionId : MutableList<Int> = mutableListOf(),
    var roleId : Int = 0,
    var searchQuery : String = "",
    var selectedFarmerOrStakeHolder : FarmerAndStakeHolderDto? = null
)
