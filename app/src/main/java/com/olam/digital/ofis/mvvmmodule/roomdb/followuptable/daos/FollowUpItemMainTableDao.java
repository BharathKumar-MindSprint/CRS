package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
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

    @Query("UPDATE DmSurveyAnswerTable SET isSynced=:status WHERE moduleId = :moduleId AND farmerId =:farmerId")
    void updateSyncStatus(String moduleId, String farmerId, boolean status);

    @Query("SELECT * FROM FollowUpItemMainTable WHERE (attempt, farmerId, moduleId) IN (\n" +
            "    SELECT MAX(attempt), farmerId, moduleId\n" +
            "    FROM FollowUpItemMainTable\n" +
            "    WHERE farmerId = :farmerId AND moduleId = :moduleId\n" +
            "    GROUP BY farmerId, moduleId\n" +
            ")")
    List<FollowUpItemMainTable> getFollowupByAttempts(long farmerId, int moduleId);
}
