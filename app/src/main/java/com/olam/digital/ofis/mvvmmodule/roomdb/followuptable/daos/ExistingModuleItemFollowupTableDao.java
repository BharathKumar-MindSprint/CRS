package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.ExistingModuleItemFollowupTable;

import java.util.List;

@Dao
public interface ExistingModuleItemFollowupTableDao {

    @Query("SELECT * FROM ExistingModuleItemFollowupTable")
    List<ExistingModuleItemFollowupTable> getExistingModuleItemFollowupTable();

    @Insert
    void insert(ExistingModuleItemFollowupTable... existingModuleItemFollowupTables);

    @Query("DELETE FROM ExistingModuleItemFollowupTable")
    void deleteAll();
}
