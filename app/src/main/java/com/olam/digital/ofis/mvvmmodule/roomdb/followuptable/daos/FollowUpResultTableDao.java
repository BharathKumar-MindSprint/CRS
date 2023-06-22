package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpResultTable;

import java.util.List;

@Dao
public interface FollowUpResultTableDao {
    @Query("SELECT * FROM FollowUpResultTable")
    List<FollowUpResultTable> getFollowUpResultTable();

    @Insert
    void insert(FollowUpResultTable... followUpResultTables);

    @Query("DELETE FROM FollowUpResultTable")
    void deleteAll();

}
