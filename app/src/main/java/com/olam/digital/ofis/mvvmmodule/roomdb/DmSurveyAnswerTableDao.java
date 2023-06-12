package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import java.util.List;


@Dao
public interface DmSurveyAnswerTableDao {
    @Query("SELECT * FROM DmSurveyAnswerTable")
    List<DmSurveyAnswerTable> getDmSurveyAnswerTable();

    @Insert
    void insert(DmSurveyAnswerTable... dmSurveyAnswerTables);

    @Delete
    void delete(DmSurveyAnswerTable dmSurveyAnswerTables);

    @Query("DELETE FROM DmSurveyAnswerTable WHERE questionID =:id")
    void deleteByQuestionId(String id);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id")
    List<DmSurveyAnswerTable> getDataByModuleId(String id);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id")
    List<DmSurveyAnswerTable> getDataByQuestionId(String id);

    @Query("DELETE FROM DmSurveyAnswerTable")
    void deleteAll();

}
