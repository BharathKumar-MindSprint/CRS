package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;

import java.util.List;

@Dao
public interface DmQuestionsModuleDao {
    @Query("SELECT * FROM DmQuestionsModule")
    List<DmQuestionsModule> getDmQuestionsModule();

    @Insert
    void insert(DmQuestionsModule... dmQuestionsModules);

    @Delete
    void delete(DmQuestionsModule dmQuestionsModule);
}
