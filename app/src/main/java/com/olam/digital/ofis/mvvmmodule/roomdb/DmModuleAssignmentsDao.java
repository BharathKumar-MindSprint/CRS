package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmModuleAssignments;

import java.util.List;

@Dao
public interface DmModuleAssignmentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDmModuleAssignMents(List<DmModuleAssignments> dmModuleAssignments);

    @Query("DELETE FROM DmModuleAssignments")
    void deleteAll();

}
