package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Country;

import java.util.List;

@Dao
public interface ICountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Country> country);

    @Query("DELETE FROM Country")
    void deleteAll();
}
