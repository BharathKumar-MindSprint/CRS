package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrency;

import java.util.List;

@Dao
public interface DmCurrencyDao {
    @Query("SELECT * FROM DmCurrency")
    List<DmCurrency> getDMCurrency();

    @Query("SELECT * FROM DmCurrency WHERE moduleDraftId =:id")
    List<DmCurrency> getDMCurrencyByModuleId(String id);

    @Query("SELECT currencyId FROM DmCurrency WHERE moduleDraftId = :moduleDraftId AND isDefault=:value")
    int isCurrencyDefault(int moduleDraftId , int value);


    @Insert
    void insert(DmCurrency... dmCurrency);

    @Delete
    void delete(DmCurrency dmCurrency);

    @Query("DELETE FROM DmCurrency")
    void deleteAll();
}
