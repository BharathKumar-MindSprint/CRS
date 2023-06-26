package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import java.util.List;

@Dao
public interface FollowUpItemMainTableDao {

    @Query("SELECT * FROM FollowUpItemMainTable")
    List<FollowUpItemMainTable> getFollowUpItemMainTable();

    @Insert
    void insert(FollowUpItemMainTable... followUpItemMainTables);

    @Query("DELETE FROM FollowUpItemMainTable")
    void deleteAll();

    @Query("SELECT * FROM FollowUpItemMainTable WHERE farmerId =:farmerId AND moduleId =:moduleId")
    List<FollowUpItemMainTable> getFollowupByFarmerId(long farmerId, int moduleId);

}
