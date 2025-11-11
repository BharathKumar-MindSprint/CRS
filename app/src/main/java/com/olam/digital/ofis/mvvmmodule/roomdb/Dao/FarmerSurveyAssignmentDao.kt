package com.olam.digital.ofis.mvvmmodule.roomdb.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.FarmerSurveyAssignment

@Dao
interface FarmerSurveyAssignmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssignedFarmers(assignedFarmers: List<FarmerSurveyAssignment>)

    @Query("Select DISTINCT moduleId from farmer_survey_assignment WHERE farmerId = :farmerId")
    fun getModuleIdsByFarmerId(farmerId : Long) : MutableList<Int>

    @Query("DELETE FROM farmer_survey_assignment")
    fun deleteAll()
}