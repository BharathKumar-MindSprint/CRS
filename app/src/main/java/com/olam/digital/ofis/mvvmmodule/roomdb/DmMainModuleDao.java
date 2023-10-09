package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;

import java.util.List;

@Dao
public interface DmMainModuleDao {

    @Query("SELECT * FROM DmMainModule")
    List<DmMainModule> getDMmodule();

    @Insert
    void insert(DmMainModule... dmModule);

    @Delete
    void delete(DmMainModule dmModule);

    @Query("DELETE FROM DmMainModule")
    void deleteAll();

    @Query("SELECT assessmentType FROM DmMainModule WHERE id=:moduleId")
    String getAssessmentType(int moduleId);

    @Query("SELECT displayLogic FROM DmMainModule WHERE id=:moduleId")
    String getDisplayLogic(int moduleId);

    @Query("SELECT maxScore FROM DmMainModule WHERE id=:moduleId")
    int getMaxScore(int moduleId);

    @Query("SELECT startDate FROM DmMainModule WHERE id=:moduleId")
    String getStartData(int moduleId);
    @Query("SELECT followUpDuration FROM DmMainModule WHERE id=:moduleId")
    Integer getFollowUpDuration(String moduleId);

    @Query("SELECT followUpAttempts FROM DmMainModule WHERE id=:moduleId")
    Integer getFollowUpAttempts(String moduleId);

}
