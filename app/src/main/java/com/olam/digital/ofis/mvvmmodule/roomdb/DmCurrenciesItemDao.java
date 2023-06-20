package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionTypes;

import java.util.List;

@Dao
public interface DmCurrenciesItemDao {
    @Query("SELECT * FROM DmCurrenciesItem")
    List<DmCurrenciesItem> getDmCurrenciesItem();

    @Insert
    void insert(DmCurrenciesItem... dmCurrenciesItems);
    @Query("SELECT * FROM DmCurrenciesItem WHERE currencyId = :id")
    List<DmCurrenciesItem> getCurrencyItemById(int id);

    @Query("SELECT code FROM DmCurrenciesItem WHERE currencyId = :id")
    String getCurrencyCodeItemById(int id);

    @Delete
    void delete(DmCurrenciesItem dmCurrenciesItems);

    @Query("DELETE FROM DmCurrenciesItem")
    void deleteAll();
}
