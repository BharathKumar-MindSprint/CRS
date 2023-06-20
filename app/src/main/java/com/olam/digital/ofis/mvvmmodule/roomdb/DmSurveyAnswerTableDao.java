package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("DELETE FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId")
    void deleteByQuestionId(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND isCaptureProof = :isCaptureProof")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id, String farmerId, boolean isCaptureProof);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable>  getDataByModuleId(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable> getDataByQuestionId(String id, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET actionplanId=:actionplanId WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateActionPlanId(String actionplanId, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET actionPlanText=:actionPlanText WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateActionPlanText(String actionPlanText, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET amount=:amount WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateAmount(String amount, String questionId, String farmerId);

    @Query("DELETE FROM DmSurveyAnswerTable")
    void deleteAll();

    @Update
    void updateEntity(DmSurveyAnswerTable entity);

}
