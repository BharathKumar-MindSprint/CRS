package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;

import java.util.List;

@Dao
public interface FollowUpAnswerTableDao {

    @Query("SELECT * FROM FollowUpAnswerTable")
    List<FollowUpAnswerTable> getFollowUpAnswerTable();

    @Insert
    void insert(FollowUpAnswerTable... followUpAnswerTables);

    @Query("DELETE FROM FollowUpAnswerTable")
    void deleteAll();

}
