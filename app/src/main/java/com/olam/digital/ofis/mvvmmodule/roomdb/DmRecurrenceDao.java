package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;

import java.util.List;

@Dao
public interface DmRecurrenceDao {
    @Query("SELECT * FROM DmRecurrence")
    DmRecurrence getDmRecurrence();

    @Insert
    void insert(DmRecurrence... dmRecurrences);

    @Delete
    void delete(DmRecurrence dmRecurrences);


    @Query("SELECT * FROM DmRecurrence WHERE moduleId =:id")
    DmRecurrence getRecurrenceByModuleId(String id);

    @Query("DELETE FROM DmRecurrence")
    void deleteAll();
}
