package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import java.util.List;

@Dao
public interface DmActionPlanCurrenciesTableDao {
    @Query("SELECT * FROM DmActionPlanCurrenciesTable")
    List<DmActionPlanCurrenciesTable> getDmActionPlanCurrenciesTable();

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


}
