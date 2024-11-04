package com.olam.digital.ofis.mvvmmodule.model.multistakeholder.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;

@Dao
public interface MultiStakeHolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MultiStakeHolder... multiStakeHolders);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MultiStakeHolder multiStakeHolder);

    @Delete
    void delete(MultiStakeHolder multiStakeHolder);

    @Query("DELETE FROM MultiStakeHolder")
    void deleteAll();
}
