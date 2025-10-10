package com.olam.digital.ofis.distributionmodule.distribution_data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DistributionFormEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    val distributionId : String? = null,
    val farmerId : String,
    val stakeHolderId : String? = null,
    val distributionCategoryId : Int,
    val distributionGroupId : Int,
    val distributionItemId : Int,
    val transactionValue : String,
    val localUnitId : Int? = null,
    val currencyId : Int? = null,
    val price : String? = null,
    val totalAmount : String? = null,
    val evidenceUrl : String,
    val signatureUrl : String,
    val isSync : String = "false",
    val isFilesSynced : String = "false"

)
