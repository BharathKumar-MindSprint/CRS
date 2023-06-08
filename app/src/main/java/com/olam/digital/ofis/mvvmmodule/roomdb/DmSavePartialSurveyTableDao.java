package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSavePartialSurveyTable;


@Dao
public interface DmSavePartialSurveyTableDao {

    @Query("SELECT * FROM DmSavePartialSurveyTable")
    DmSavePartialSurveyTable getDmSavePartialSurveyTable();

    @Insert
    void insert(DmSavePartialSurveyTable... dmSavePartialSurveyTables);

    @Delete
    void delete(DmSavePartialSurveyTable dmSavePartialSurveyTables);

}
