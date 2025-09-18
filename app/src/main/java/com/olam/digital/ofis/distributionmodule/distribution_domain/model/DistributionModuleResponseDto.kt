package com.olam.digital.ofis.distributionmodule.distribution_domain.model

import com.google.gson.annotations.SerializedName
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionCategory
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionGroup
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionItem

data class DistributionModuleResponseDto(
    @SerializedName("distributionCategories")
    val distributionCategories : List<DistributionCategory>,
    @SerializedName("distributionGroups")
    val distributionGroups : List<DistributionGroup>,
    @SerializedName("distributionItems")
    val distributionItems : List<DistributionItem>,
)
