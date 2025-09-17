package com.olam.digital.ofis.distributionmodule.distribution_domain.model

data class DistributionFormDto(
    val id : Int? = null,
    val distributionId : String? = null,
    val distributionCategoryId : Int,
    val distributionGroupId : Int,
    val distributionItemId : Int,
    val transactionValue : String,
    val localUnitId : Int?,
    val currencyId : Int?,
    val price : String? = null,
    val totalAmount : String? = null,
    val evidenceUrl : String,
    val signatureUrl : String,
    val farmerId : String,
    val stakeHolderId : String? = null
)
