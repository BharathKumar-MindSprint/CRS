package com.olam.digital.ofis.distributionmodule.distribution_data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.agrisupplier.entity.Supplier
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionGroup
import com.olam.digital.ofis.model.SpinnerDataDto

@Dao
interface DistributionGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDistributionGroup(suppliers: List<DistributionGroup>)

    @Query("Select id, name from DistributionGroup WHERE categoryId = :distributionCategoryId ORDER by name ASC")
    fun getAllDistributionGroups(distributionCategoryId : Int) : MutableList<SpinnerDataDto>

    @Query("DELETE FROM Currency")
    fun deleteAll()
}