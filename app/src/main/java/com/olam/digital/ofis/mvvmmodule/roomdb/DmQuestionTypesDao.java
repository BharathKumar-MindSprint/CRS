package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionTypes;

import java.util.List;

@Dao
public interface DmQuestionTypesDao {
    @Query("SELECT * FROM DmQuestionTypes")
    List<DmQuestionTypes> getDmQuestionTypes();

    @Insert
    void insert(DmQuestionTypes... dmQuestionTypes);

    @Delete
    void delete(DmQuestionTypes dmQuestionTypes);

    @Query("DELETE FROM DmQuestionTypes")
    void deleteAll();

}
