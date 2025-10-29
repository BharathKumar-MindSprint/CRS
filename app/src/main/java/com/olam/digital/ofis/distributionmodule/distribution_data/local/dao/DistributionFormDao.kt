package com.olam.digital.ofis.distributionmodule.distribution_data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionFormEntity
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto

@Dao
interface DistributionFormDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistributionForm(distributionFormEntity: DistributionFormEntity)

    @Query("SELECT * FROM DistributionFormEntity WHERE distributionId = :distributionId AND isFilesSynced = :isFileSynced LIMIT 1")
    fun getDistributionFormByTempId(distributionId: String, isFileSynced : String) : DistributionFormEntity

    @Query("UPDATE distributionformentity SET isSync = 'true', distributionId = :distributionId WHERE distributionId = :tempId")
    fun updateSyncStatusForDistributionForm(distributionId: String, tempId : String)

    @Query("UPDATE distributionformentity SET isFilesSynced = 'true' WHERE distributionId = :tempId")
    fun updateFileSyncStatus(tempId: String)

    @Query("""
    SELECT * FROM DistributionFormEntity 
    WHERE distributionId = :distributionId
    AND isSync LIKE 'false'
""")
    fun getDistributionFormDetails(distributionId: String?): DistributionFormDto

    @Query("""
        UPDATE distributionformentity SET stakeHolderId = :msId WHERE stakeHolderId = :tempMsId AND isSync LIKE 'false'
    """)
    fun updateStakeHolderInDistribution(msId: Long, tempMsId: Long)

    @Query("""
        UPDATE distributionformentity SET farmerId = :farmerId WHERE farmerId = :localFarmerId AND isSync LIKE 'false'
    """)
    fun updateFarmerIdInDistribution(localFarmerId: Int, farmerId: Int)
}