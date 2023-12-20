package com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.LocalUnit;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface LocalUnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalUnit... localUnits);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<LocalUnit> localUnits);

    @Delete
    void delete(LocalUnit localUnit);

    @Query("DELETE FROM LocalUnit")
    void deleteAll();

    @Query("SELECT conversionFactor FROM LocalUnit WHERE localUnitId =:localUnitId")
    double getConversionFactorById(int localUnitId);

//    @Query("SELECT localUnitAbbreviation FROM LocalUnit where globalUnitFk =:globalId")
//    List<String> getValueByGlobalId(int globalId);

    @Query("SELECT localUnitId, localUnitAbbreviation FROM LocalUnit WHERE globalUnitFk = :targetGlobalUnitFk ")
    List<UnitMapping> getValueByGlobalId(int targetGlobalUnitFk);

    class UnitMapping {
        public int localUnitId;
        public String localUnitAbbreviation;
    }

}
