package com.olam.digital.ofis.mvvmmodule.model.multistakeholder.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;

import java.util.List;

@Dao
public interface MultiStakeHolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MultiStakeHolder... multiStakeHolders);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAndReturnId(MultiStakeHolder multiStakeHolder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MultiStakeHolder multiStakeHolder);

    @Delete
    void delete(MultiStakeHolder multiStakeHolder);

    @Query("DELETE FROM MultiStakeHolder")
    void deleteAll();

    @Query("SELECT * FROM MultiStakeHolder WHERE msName=:farmerName AND msDOB=:dob")
    boolean isFarmerEntryUnique(String farmerName, String dob);

    @Query("SELECT * FROM MultiStakeHolder WHERE msId=:stakeholderId")
    MultiStakeHolder getStakeHolderById(long stakeholderId);

    @Update
    int updateStakeholder(MultiStakeHolder multiStakeholder);

    @Query("UPDATE MULTISTAKEHOLDER SET msId=:msId, farmId=:farmId, olamMultiStakeholderId=:olamFarmerId, msDisplayId=:olamFarmerId, synced=:syncedStatus WHERE msId=:localFarmerId")
    int updateStakeholderAfterSync(long localFarmerId, long msId, String farmId, long olamFarmerId, int syncedStatus);

}
