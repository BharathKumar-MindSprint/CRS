package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ConditionalLogic;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ConditionalType;

import java.util.List;

@Dao
public interface ConditionalLogicDao {

    @Query("SELECT * FROM conditional_logic WHERE questionId = :questionsId")
    List<ConditionalLogic> getConditionsPropertiesByQuestionId(int questionsId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConditionalLogic> conditionalLogic);

    @Query("DELETE FROM conditional_logic")
    void deleteAll();
}
