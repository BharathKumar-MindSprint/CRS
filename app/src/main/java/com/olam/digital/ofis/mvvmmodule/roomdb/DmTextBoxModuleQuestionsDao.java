package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;

@Dao
public interface DmTextBoxModuleQuestionsDao {

    @Query("SELECT * FROM DmTextBoxModuleQuestions")
    DmTextBoxModuleQuestions getDmTextBoxModuleQuestions();

    @Insert
    void insert(DmTextBoxModuleQuestions... dmTextBoxModuleQuestions);

    @Delete
    void delete(DmTextBoxModuleQuestions dmTextBoxModuleQuestions);

    @Query("DELETE FROM DmTextBoxModuleQuestions")
    void deleteAll();
}
