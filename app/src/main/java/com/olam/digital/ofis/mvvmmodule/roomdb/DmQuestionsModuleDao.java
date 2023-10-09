package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;

import java.util.List;

@Dao
public interface DmQuestionsModuleDao {
    @Query("SELECT * FROM DmQuestionsModule")
    List<DmQuestionsModule> getDmQuestionsModule();

    @Query("SELECT * FROM DmQuestionsModule WHERE moduleId=:id AND sectionId=0")
    List<DmQuestionsModule> getDmQuestionsModulebyModuleId(int id);

    @Query("SELECT * FROM DmQuestionsModule WHERE sectionId =:id")
    List<DmQuestionsModule> getDmQuestionsSubModulebySectionId(int id);

    @Query("SELECT * FROM DmQuestionsModule WHERE id=:id")
    List<DmQuestionsModule> getDmQuestionType(int id);



    @Insert
    void insert(DmQuestionsModule... dmQuestionsModules);

    @Delete
    void delete(DmQuestionsModule dmQuestionsModule);

    @Query("DELETE FROM DmQuestionsModule")
    void deleteAll();

    @Query("SELECT * FROM DmQuestionsModule WHERE moduleId=:id AND mandate=1")
    List<DmQuestionsModule> getDmQuestionsModuleId(int id);

    @Query("SELECT isCriticalQuest FROM DmQuestionsModule WHERE moduleId=:moduleID AND id=:questionID")
    int getIsCriticalQuestForColor(String moduleID,int questionID);

    @Query("SELECT question FROM DmQuestionsModule WHERE id=:questionID")
    String getQuestionById(int questionID);
}
