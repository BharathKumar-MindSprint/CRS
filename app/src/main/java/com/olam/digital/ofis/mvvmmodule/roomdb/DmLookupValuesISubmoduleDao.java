package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;

import java.util.List;

@Dao
public interface DmLookupValuesISubmoduleDao {
    @Query("SELECT * FROM DmLookupValuesISubmodule")
    List<DmLookupValuesISubmodule> getDmLookupValuesISubmodule();

    @Insert
    void insert(DmLookupValuesISubmodule... dmLookupValuesISubmodules);

    @Delete
    void delete(DmLookupValuesISubmodule dmLookupValuesISubmodules);
}
