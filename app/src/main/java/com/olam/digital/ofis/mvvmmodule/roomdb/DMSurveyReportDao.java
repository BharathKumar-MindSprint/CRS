package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DMSurveyReport;

@Dao
public interface DMSurveyReportDao {

    @Insert
    void insert(DMSurveyReport surveyReport);

    @Query("SELECT * FROM DMSurveyReport WHERE farmerId = :farmerId AND moduleId = :moduleId AND plotId = :plotId")
    DMSurveyReport getFinalDMSurveyReports(long farmerId, int moduleId, int plotId);

    @Query("DELETE FROM DMSurveyReport WHERE moduleId = :moduleId AND farmerId = :farmerId AND plotId = :plotId")
    void deleteAlreadyStoredData(int moduleId, int farmerId, int plotId);

    @Query("UPDATE DMSurveyReport SET farmerId = :farmerId WHERE farmerId = :tempFarmerId")
    void updateFarmerId(int tempFarmerId, int farmerId);

    @Query("DELETE FROM DMSurveyReport")
    void deleteAll();

    @Query("DELETE FROM DMSurveyReport WHERE moduleId= :moduleId and farmerId= :farmerId AND plotId = :plotId")
    void delete(int moduleId, long farmerId, int plotId);

    @Query("UPDATE DMSurveyReport SET followUpCount = :followUpCount, isFollowUpCompleted = :isFollowUpCompleted ,firstFollowupDate =:firstFollowUpDate WHERE farmerId = :farmerId AND moduleId = :moduleId AND plotId = :plotId")
    void updateFollowUpCount(long farmerId, int moduleId, int plotId, int followUpCount, String isFollowUpCompleted,String firstFollowUpDate);
}

