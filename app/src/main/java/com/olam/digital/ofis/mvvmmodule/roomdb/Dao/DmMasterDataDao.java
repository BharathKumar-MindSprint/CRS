package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmMasterDataModel;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMasterData;

@Dao
public interface DmMasterDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DmMasterData... dmMasterData);

    @Query("SELECT id FROM DmMasterData WHERE value = :value")
    int getDiagnosticPlotTypeId(String value);

    @Query("DELETE FROM DmMasterData")
    void deleteAll();
}
