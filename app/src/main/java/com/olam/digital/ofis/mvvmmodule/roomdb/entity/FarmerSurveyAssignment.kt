package com.olam.digital.ofis.mvvmmodule.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_survey_assignment")
data class FarmerSurveyAssignment @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    val moduleId : Int,
    val farmerId : Int
)
