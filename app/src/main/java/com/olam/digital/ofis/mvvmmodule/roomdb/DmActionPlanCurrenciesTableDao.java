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

    @Query("DELETE FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId")
    void deleteByModuleId(String id, String farmerId);

    @Query("SELECT * FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId")
    List<DmActionPlanCurrenciesTable> getDataByModuleId(String id, String farmerId);

    @Query("SELECT currencyId FROM DmActionPlanCurrenciesTable WHERE moduleId =:id AND farmerId =:farmerId")
    int getCurrencyByModuleId(String id , String farmerId);
}
