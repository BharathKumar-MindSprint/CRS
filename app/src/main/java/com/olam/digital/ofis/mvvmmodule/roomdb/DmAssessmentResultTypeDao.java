package com.olam.digital.ofis.mvvmmodule.roomdb;

import static android.icu.text.MessagePattern.ArgType.SELECT;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AssessmentResultTypeItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAssessmentResultType;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;

import java.util.List;

@Dao
public interface DmAssessmentResultTypeDao {

    @Insert
    void insert(DmAssessmentResultType... dmAssessmentResultTypes);

    @Delete
    void delete(DmAssessmentResultType dmAssessmentResultType);

    @Query("SELECT minRange FROM AssessmentResultType WHERE moduleId=:moduleId")
    int getMinRange(int moduleId);

    @Query("SELECT maxRange FROM AssessmentResultType WHERE moduleId=:moduleId")
    int getMaxRange(int moduleId);

    @Query("SELECT * FROM AssessmentResultType WHERE moduleId=:moduleId")
    List<AssessmentResultTypeItems> getAllModuleId(int moduleId);



    @Query("SELECT remarks FROM AssessmentResultType WHERE moduleId=:moduleId AND minRange=:minRange AND maxRange=:maxRange")
    String getRemarks(int moduleId , int minRange , int maxRange);




}
