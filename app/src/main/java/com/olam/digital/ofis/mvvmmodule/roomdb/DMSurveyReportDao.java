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

    @Query("SELECT * FROM DMSurveyReport WHERE farmerId = :farmerId AND moduleId = :moduleId")
    DMSurveyReport getFinalDMSurveyReports(long farmerId, int moduleId);



    /*@Query("SELECT * FROM DMSurveyReport WHERE id = :reportId")
    DMSurveyReport getSurveyReportById(int reportId);*/


}

