package com.olam.digital.ofis.distributionmodule.distribution_data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class DistributionCategory(
    @PrimaryKey
    val id : Int,
    val name : String
)

@Entity
data class DistributionGroup(
    @PrimaryKey
    val id : Int,
    @SerializedName("groupName")
    val name : String,
    @SerializedName("categoryId")
    val categoryId : Int
)

@Entity
data class DistributionItem(
    @PrimaryKey
    val id : Int,
    @SerializedName("itemName")
    val name : String,
    @SerializedName("groupId")
    val distributionGroupId : Int,
    @SerializedName("categoryId")
    val categoryId : Int
)
