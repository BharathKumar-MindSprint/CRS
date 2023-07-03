package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import java.util.List;

@Dao
public interface ComplianceIndicatorDao {

    @Query("SELECT * FROM ComplianceIndicator")
    List<ComplianceIndicator> getComplianceIndicator();

    @Insert
    void insert(ComplianceIndicator... complianceIndicators);


    @Query("SELECT * FROM ComplianceIndicator WHERE moduleDraftId =:id")
    List<ComplianceIndicator> getComplianceByModuleId(String id);

    @Query("SELECT * FROM ComplianceIndicator WHERE id =:id")
    List<ComplianceIndicator> getComplianceNameById(String id);

    @Query("SELECT compliance FROM ComplianceIndicator WHERE id =:id")
    String getComplianceNameBycomplianceId(String id);

    @Query("DELETE FROM ComplianceIndicator")
    void deleteAll();

    @Query("SELECT score FROM ComplianceIndicator WHERE id =:id AND compliance=:complianceText")
    int getComplianceScore(String id , String complianceText);

    @Query("SELECT isActionPlanRequired FROM ComplianceIndicator WHERE id=:complianceID")
    boolean getIsActionPlanRequired(String complianceID);



}
