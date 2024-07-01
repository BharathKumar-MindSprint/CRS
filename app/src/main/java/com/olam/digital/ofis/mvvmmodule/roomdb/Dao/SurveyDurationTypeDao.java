package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.qwrdata.SurveyDurationType;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.ValidationOnRange;

import java.util.List;

@Dao
public interface SurveyDurationTypeDao {

    @Query("SELECT * FROM SurveyDurationType")
    List<SurveyDurationType> getSurveyDurationTypes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SurveyDurationType... surveyDurationTypes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SurveyDurationType> surveyDurationTypeList);

    @Query("SELECT * FROM SurveyDurationType WHERE id = :id")
    List<SurveyDurationType> getSurveyDurationTypeById(int id);

    @Query("SELECT id FROM SurveyDurationType WHERE value =  \"Perpetual Survey\" ")
    int getSurveyDurationId();

    @Delete
    void delete(SurveyDurationType surveyDurationType);

    @Query("DELETE FROM SurveyDurationType")
    void deleteAll();

}
