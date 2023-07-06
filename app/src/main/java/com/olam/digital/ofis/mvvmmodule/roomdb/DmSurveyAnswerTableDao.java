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



    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable> getDataByComplianceIdFilterByActionPlan(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND actionplanId")
    List<DmSurveyAnswerTable> getDataByComplianceIdFilterResolved(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE complinceTypeId =:id AND farmerId =:farmerId AND isCaptureProof = :isCaptureProof")
    List<DmSurveyAnswerTable> getDataByComplianceId(String id, String farmerId, boolean isCaptureProof);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable> getDataByModuleId(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE moduleId =:id AND farmerId =:farmerId AND isSynced = :isSynced")
    List<DmSurveyAnswerTable> getDataByModuleId(String id, String farmerId, boolean isSynced);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId")
    List<DmSurveyAnswerTable> getDataByQuestionId(String id, String farmerId);

    @Query("SELECT * FROM DmSurveyAnswerTable WHERE questionID =:id AND farmerId =:farmerId AND actionplanId = :actionplanId")
    List<DmSurveyAnswerTable> getDataByQuestionIdActionPlan(String id, String farmerId, String actionplanId);

    @Query("UPDATE DmSurveyAnswerTable SET resolved=:resolved WHERE questionID = :id AND farmerId =:farmerId")
    void updateResolvedStatus(String resolved, String id, String farmerId);


    @Query("UPDATE DmSurveyAnswerTable SET actionplanId=:actionplanId WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateActionPlanId(String actionplanId, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET actionplanId=:actionplanId WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateQuestionData(String actionplanId, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET actionPlanText=:actionPlanText WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateActionPlanText(String actionPlanText, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET amount=:amount WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateAmount(String amount, String questionId, String farmerId);

    @Query("UPDATE DmSurveyAnswerTable SET resolved=:status WHERE questionID = :questionId AND farmerId =:farmerId")
    void updateResolveStatus(String status, String questionId, String farmerId);

    @Query("DELETE FROM DmSurveyAnswerTable")
    void deleteAll();

    @Query("SELECT actionPlanText FROM DmSurveyAnswerTable WHERE questionID = :questionId AND farmerId =:farmerId")
    String getActionPlanText(String questionId, String farmerId);


    @Update
    void updateEntity(DmSurveyAnswerTable entity);

    /*@Query("SELECT idQuestionsMudule FROM DmLookupValuesItem WHERE idQuestionsMudule=:id AND isCaptureProof =:isCaptureProof")
    String getQuestionIdByProof (int id , int isCaptureProof);
*/
    /*@Query("SELECT isCaptureProof FROM DmLookupValuesItem WHERE id=:id AND isCaptureProof =:isCaptureProof")
    ArrayList<DmLookupValuesItem> getQuestionIdByProofBoolean (String id , int isCaptureProof);*/

    @Query("SELECT isCaptureProof FROM DmSurveyAnswerTable WHERE questionId = :id AND farmerId = :farmerId")
    int getIsCaptureProofValue(int id, String farmerId);


    @Query("UPDATE DmSurveyAnswerTable SET isSynced=:status, submittedModuleId =:submittedmoduleId WHERE moduleId = :moduleId AND farmerId =:farmerId")
    void updateSyncStatus(String moduleId, String farmerId, boolean status, int submittedmoduleId);

    @Query("SELECT isCaptureProof FROM DmSurveyAnswerTable WHERE questionId = :id AND farmerId = :farmerId")
    int getIsCaptureProofRequired(String id, String farmerId );

}
