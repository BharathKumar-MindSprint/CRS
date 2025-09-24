package com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.GlobalUnit;

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

    @Query("SELECT globalAbbreviation FROM GlobalUnit WHERE globalUnitId =:globalUnitId")
    String getAbbByGlobalID(Integer globalUnitId);


    @Query("SELECT globalUnitId, globalAbbreviation FROM GlobalUnit WHERE globalUnitId = :targetGlobalUnitFk")
    List<GlobalUnitDao.UnitMapping> getGlobalId(int targetGlobalUnitFk);

    @Query("SELECT * FROM GlobalUnit")
    List<GlobalUnit> getGlobalUnits();

    class UnitMapping {
        public int globalUnitId;
        public String globalAbbreviation;
    }

}
