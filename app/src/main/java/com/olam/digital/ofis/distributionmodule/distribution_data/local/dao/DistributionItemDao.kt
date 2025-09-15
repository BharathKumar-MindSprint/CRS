package com.olam.digital.ofis.distributionmodule.distribution_data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionGroup
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionItem
import com.olam.digital.ofis.model.SpinnerDataDto

@Dao
interface DistributionItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDistributionItem(suppliers: List<DistributionItem>)

    @Query("Select id, name from DistributionItem WHERE distributionGroupId = :distributionGroupId ORDER BY name ASC")
    fun getAllDistributionItem(distributionGroupId : String) : MutableList<SpinnerDataDto>

    @Query("DELETE FROM DistributionItem")
    fun deleteAll()
}