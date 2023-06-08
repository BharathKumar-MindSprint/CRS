package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;

import java.util.List;

@Dao
public interface DmQuestionsModuleDao {
    @Query("SELECT * FROM DmQuestionsModule")
    List<DmQuestionsModule> getDmQuestionsModule();

    @Query("SELECT * FROM DmQuestionsModule WHERE moduleId=:id")
    List<DmQuestionsModule> getDmQuestionsModulebyModuleId(int id);

    @Insert
    void insert(DmQuestionsModule... dmQuestionsModules);

    @Delete
    void delete(DmQuestionsModule dmQuestionsModule);

    @Query("DELETE FROM DmQuestionsModule")
    void deleteAll();
}
