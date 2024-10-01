package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DMSurveyReport;
import java.util.List;

@Dao
public interface DMSurveyReportDao {

    @Insert
    void insert(DMSurveyReport surveyReport);

    @Query("SELECT * FROM DMSurveyReport WHERE farmerId = :farmerId AND moduleId = :moduleId AND plotId = :plotId")
    DMSurveyReport getFinalDMSurveyReports(long farmerId, int moduleId, int plotId);

    @Query("DELETE FROM DMSurveyReport WHERE moduleId = :moduleId AND farmerId = :farmerId")
    void deleteAlreadyStoredData(int moduleId, int farmerId);

    @Query("UPDATE DMSurveyReport SET farmerId = :farmerId WHERE farmerId = :tempFarmerId")
    void updatefarmerId(int tempFarmerId, int farmerId);



}

