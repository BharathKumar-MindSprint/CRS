package com.olam.digital.ofis.distributionmodule.distribution_data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionCategory
import com.olam.digital.ofis.model.SpinnerDataDto

@Dao
interface DistributionCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDistributionCategory(suppliers: List<DistributionCategory>)

    @Query("Select id,name from DistributionCategory")
    fun getAllDistributionCategory() : MutableList<SpinnerDataDto>

    @Query("DELETE FROM DistributionCategory")
    fun deleteAll()
}