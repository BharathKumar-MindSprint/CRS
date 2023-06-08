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

    @Delete
    void delete(ComplianceIndicator complianceIndicators);

    @Query("SELECT * FROM ComplianceIndicator WHERE moduleDraftId =:id")
    List<ComplianceIndicator> getComplianceByModuleId(String id);

}
