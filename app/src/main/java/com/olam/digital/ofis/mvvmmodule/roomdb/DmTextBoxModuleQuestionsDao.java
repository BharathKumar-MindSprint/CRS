package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;

import java.util.List;

@Dao
public interface DmTextBoxModuleQuestionsDao {

    @Query("SELECT * FROM DmTextBoxModuleQuestions")
    DmTextBoxModuleQuestions getDmTextBoxModuleQuestions();

    @Insert
    void insert(DmTextBoxModuleQuestions... dmTextBoxModuleQuestions);

    @Delete
    void delete(DmTextBoxModuleQuestions dmTextBoxModuleQuestions);

    @Query("SELECT * FROM DmTextBoxModuleQuestions WHERE questionId=:id")
    List<DmTextBoxModuleQuestions> getDmTextBoxByQuestionId(int id);

    @Query("DELETE FROM DmTextBoxModuleQuestions")
    void deleteAll();
}
