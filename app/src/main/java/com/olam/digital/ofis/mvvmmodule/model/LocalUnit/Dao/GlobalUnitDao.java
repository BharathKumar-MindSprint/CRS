package com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.GlobalUnit;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.LocalUnit;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface GlobalUnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GlobalUnit... globalUnits);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(GlobalUnit globalUnits);

    @Delete
    void delete(GlobalUnit globalUnit);

    @Query("DELETE FROM GlobalUnit")
    void deleteAll();

    @Query("SELECT globalAbbreviation FROM GlobalUnit WHERE globalUnitId=:globalUnitId")
    List<String> getGlobalAbbreviation(int globalUnitId);

}
