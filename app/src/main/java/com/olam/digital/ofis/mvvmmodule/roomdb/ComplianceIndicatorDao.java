package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;

import java.util.List;

@Dao
public interface ComplianceIndicatorDao {

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

    @Query("SELECT EXISTS (SELECT 1 FROM ComplianceIndicator WHERE moduleDraftId = :moduleId AND isActionPlanRequired = 1 LIMIT 1)")
    boolean hasActionPlan(String moduleId);

    @Query("SELECT NOT EXISTS (SELECT 1 FROM ComplianceIndicator WHERE moduleDraftId = :moduleId AND isActionPlanRequired = 1 LIMIT 1)")
    boolean hasNoActionPlan(String moduleId);




}
