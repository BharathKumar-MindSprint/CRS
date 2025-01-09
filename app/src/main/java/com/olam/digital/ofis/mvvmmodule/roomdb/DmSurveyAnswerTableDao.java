package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;

import java.util.List;


@Dao
public interface DmSurveyAnswerTableDao {
    @Query("SELECT * FROM DmSurveyAnswerTable")
    List<DmSurveyAnswerTable> getDmSurveyAnswerTable();

    @Insert
    void insert(DmSurveyAnswerTable... dmSurveyAnswerTables);

    @Delete
    void delete(DmSurveyAnswerTable dmSurveyAnswerTables);

    @Query("DELETE FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId AND plotId =:plotId")
    void deleteByQuestionId(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND plotId = :plotId")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id, String farmerId, int plotId);



    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND plotId = :plotId")
    List<DmSurveyAnswerTable> getDataByComplianceIdFilterByActionPlan(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND actionplanId")
    List<DmSurveyAnswerTable> getDataByComplianceIdFilterResolved(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND isCaptureProof = :isCaptureProof")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id, String farmerId, boolean isCaptureProof);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId AND plotId = :plotId")
    List<DmSurveyAnswerTable> getDataByModuleId(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId AND isSynced = :isSynced")
    List<DmSurveyAnswerTable> getDataByModuleId(String id, String farmerId, boolean isSynced);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId AND plotId =:plotId")
    List<DmSurveyAnswerTable> getDataByQuestionId(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId AND actionplanId = :actionplanId")
    List<DmSurveyAnswerTable> getDataByQuestionIdActionPlan(String id, String farmerId, String actionplanId);

    @Query("UPDATE DmSurveyAnswerTable SET resolved=:resolved WHERE questionID = :id AND farmerId =:farmerId")
    void updateResolvedStatus(String resolved, String id, String farmerId);


    @Query("UPDATE DmSurveyAnswerTable SET actionplanId=:actionplanId WHERE questionID = :questionId AND farmerId =:farmerId AND plotId = :plotId")
    void updateActionPlanId(String actionplanId, String questionId, String farmerId, int plotId);

    @Query("UPDATE DmSurveyAnswerTable SET actionplanId=:actionplanId WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateQuestionData(String actionplanId, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET actionPlanText=:actionPlanText WHERE questionID = :questionId AND farmerId =:farmerId AND plotId = :plotId")
    void updateActionPlanText(String actionPlanText, String questionId, String farmerId, int plotId);

    @Query("UPDATE DmSurveyAnswerTable SET amount=:amount WHERE questionID = :questionId AND farmerId =:farmerId AND plotId = :plotId")
    void updateAmount(String amount, String questionId, String farmerId, int plotId);

    @Query("UPDATE DmSurveyAnswerTable SET resolved=:status WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateResolveStatus(String status, String questionId, String farmerId);

    @Query("DELETE FROM DmSurveyAnswerTable")
    void deleteAll();

    @Query("DELETE FROM DmSurveyAnswerTable WHERE moduleId=:id and farmerId=:farmerID AND plotId = :plotId")
    void deleteAllDataForRecurrence(String id , String farmerID, int plotId);

    @Query("SELECT actionPlanText FROM DmSurveyAnswerTable WHERE questionID = :questionId AND farmerId =:farmerId AND plotId = :plotId")
    String getActionPlanText(String questionId, String farmerId, int plotId);


    @Update
    void updateEntity(DmSurveyAnswerTable entity);


    @Query("SELECT isCaptureProof FROM DmSurveyAnswerTable WHERE questionId = :id AND farmerId = :farmerId AND plotId = :plotId")
    int getIsCaptureProofValue(int id, String farmerId, int plotId);


    @Query("UPDATE DmSurveyAnswerTable SET isSynced=:status, submittedModuleId =:submittedmoduleId WHERE moduleId = :moduleId AND farmerId =:farmerId AND plotId = :plotId")
    void updateSyncStatus(String moduleId, String farmerId, boolean status, int submittedmoduleId, int plotId);

    @Query("SELECT isCaptureProof FROM DmSurveyAnswerTable WHERE questionId = :id AND farmerId = :farmerId")
    int getIsCaptureProofRequired(String id, String farmerId );

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId AND isSynced = :isSynced AND plotId = :plotId")
    List<DmSurveyAnswerTable> getDmPlotDataByModuleIdAndPlotId(String id, String farmerId, boolean isSynced, int plotId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE answerID =:id AND farmerId =:farmerId AND plotId =:plotId")
    List<DmSurveyAnswerTable> getDataByAnswerId(String id, String farmerId, int plotId);

    @Query("DELETE FROM DmSurveyAnswerTable WHERE answerID =:id AND farmerId =:farmerId AND plotId =:plotId")
    void deleteByAnswerId(String id, String farmerId, int plotId);

}
