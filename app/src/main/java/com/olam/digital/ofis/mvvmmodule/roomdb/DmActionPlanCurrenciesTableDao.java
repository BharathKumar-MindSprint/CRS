package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmActionPlanCurrenciesTable;

import java.util.List;

@Dao
public interface DmActionPlanCurrenciesTableDao {

    @Insert
    void insert(DmActionPlanCurrenciesTable... dmActionPlanCurrenciesTables);

    @Delete
    void delete(DmActionPlanCurrenciesTable dmActionPlanCurrenciesTable);

    @Query("DELETE FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId AND plotId = :plotId")
    void deleteByModuleId(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId AND plotId = :plotId")
    List<DmActionPlanCurrenciesTable> getDataByModuleId(String id, String farmerId, int plotId);

    @Query("SELECT * FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId AND plotId = :plotId")
    List<DmActionPlanCurrenciesTable> getCurrencyByModuleId(String id , String farmerId, int plotId);

    @Query("UPDATE DmActionPlanCurrenciesTable SET farmerId = :farmerId WHERE farmerId = :tempFarmerId")
    void updateFarmerId(int tempFarmerId, int farmerId);

}
