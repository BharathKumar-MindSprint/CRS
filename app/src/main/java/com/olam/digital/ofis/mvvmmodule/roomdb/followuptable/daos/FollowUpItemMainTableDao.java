package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import java.util.List;

@Dao
public interface FollowUpItemMainTableDao {

    @Query("SELECT * FROM FollowUpItemMainTable")
    List<FollowUpItemMainTable> getFollowUpItemMainTable();

    @Insert
    void insert(FollowUpItemMainTable... followUpItemMainTables);

    @Query("DELETE FROM FollowUpItemMainTable")
    void deleteAll();

    @Query("DELETE FROM FollowUpItemMainTable WHERE questionId =:questionId AND farmerId =:farmerId AND attempt = :attempt")
    void deleteByQnAttempt(String questionId, String farmerId, int attempt);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE farmerId =:farmerId AND moduleId =:moduleId")
    List<FollowUpItemMainTable> getFollowupByFarmerId(long farmerId, int moduleId);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE questionId =:questionId AND farmerId =:farmerId AND attempt = :attempt")
    List<FollowUpItemMainTable> getFollowupByQuestionId1(String questionId, String farmerId, int attempt);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE questionId =:questionId AND farmerId =:farmerId AND attempt = :attempt")
    FollowUpItemMainTable getFollowupByQuestionId(String questionId, String farmerId, int attempt);

    @Query("UPDATE FollowUpItemMainTable SET actionPlanText=:actionPlanText WHERE questionID = :questionId AND farmerId =:farmerId AND attempt = :attempt")
    void updateActionPlanText(String actionPlanText, String questionId, String farmerId, int attempt);

    @Query("UPDATE FollowUpItemMainTable SET actionPlanId=:actionPlanId WHERE questionID = :questionId AND farmerId =:farmerId AND attempt = :attempt")
    void updateActionPlanId(String actionPlanId, String questionId, String farmerId, int attempt);

    @Query("UPDATE FollowUpItemMainTable SET isSync=:status WHERE moduleId = :moduleId AND farmerId =:farmerId")
    void updateSyncStatus(String moduleId, String farmerId, boolean status);

    @Query("SELECT DISTINCT (attempt) FROM FollowUpItemMainTable where farmerId =:farmerId and moduleId =:moduleId")
    List<Integer> getFollowupByAttempts(long farmerId, int moduleId);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE farmerId =:farmerId AND moduleId = :moduleId and isSync = :status")
    List<FollowUpItemMainTable> getFollowupUnSyncedItems(String farmerId, String moduleId, boolean status);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE farmerId =:farmerId AND moduleId = :moduleId AND isSync = :status AND status = :type AND attempt = :attempt")
    List<FollowUpItemMainTable> getFollowupUnResolvedItems(String farmerId, String moduleId, boolean status, String type, int attempt);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE questionID = :questionId AND farmerId =:farmerId AND status=:notResolved AND isSync = 0")
    List<FollowUpItemMainTable> getActionPlanTextFollowUp(String questionId, String farmerId , String notResolved);

    @Query("SELECT status FROM FollowUpItemMainTable WHERE questionId =:questionId AND farmerId =:farmerId AND attempt =:attempt")
    String getResolvedOrNOt(String questionId, String farmerId, int attempt);


    @Query("SELECT createdAt FROM FollowUpItemMainTable WHERE attempt =:attempt AND farmerId =:farmerID ORDER BY uid DESC LIMIT 1")
    String getFollowUpStartDate(String attempt , String farmerID);

    @Query("DELETE FROM FollowUpItemMainTable WHERE moduleId=:id and farmerId=:farmerID")
    void deleteAllDataForRecurrenceFollowUp(String id , String farmerID);

    @Query("SELECT questionId FROM FOLLOWUPITEMMAINTABLE WHERE moduleId=:id and attempt=:attempt and farmerId=:farmerID")
    List<Integer> getCountOfResolvedNotResolved(String id , String attempt , String farmerID );

    @Query("SELECT DISTINCT questionId from FOLLOWUPITEMMAINTABLE where moduleId=:moduleId AND farmerId=:farmerId AND status=:status AND isSync=1")
    List<Integer> getResolvedQuestionId(String moduleId, String farmerId, String status);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE questionId=:id AND farmerId=:farmerId AND attempt=:attempt AND isSync=1")
    List<FollowUpItemMainTable> getFollowUpDataByQuestionId(String id, String farmerId, int attempt);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE farmerId =:farmerId AND moduleId = :moduleId AND isSync = :status AND status = :type AND attempt = :attempt AND complianceId =:complianceId")
    List<FollowUpItemMainTable> getFollowupUnResolvedItems(String farmerId, String moduleId, boolean status, String type, int attempt, String complianceId);



}
