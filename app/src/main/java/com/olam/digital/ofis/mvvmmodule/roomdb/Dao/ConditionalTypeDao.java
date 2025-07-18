package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ConditionalType;

import java.util.List;

@Dao
public interface ConditionalTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConditionalType> conditionalTypes);

    @Query("SELECT value FROM conditional_type WHERE id = :id")
    String getConditionType(int id);

    @Query("DELETE FROM conditional_type")
    void deleteAll();
}
