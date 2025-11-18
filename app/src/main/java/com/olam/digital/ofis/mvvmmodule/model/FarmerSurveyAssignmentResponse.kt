package com.olam.digital.ofis.mvvmmodule.model

import com.google.gson.annotations.SerializedName

data class FarmerSurveyAssignmentResponse(
    @SerializedName("moduleId")
    val moduleId : Int,
    @SerializedName("farmerIds")
    val farmerIds : List<Int>
)
