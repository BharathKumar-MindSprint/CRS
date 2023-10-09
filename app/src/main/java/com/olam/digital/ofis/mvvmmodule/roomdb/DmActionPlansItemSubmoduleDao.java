package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItemSubmodule;

import java.util.List;

@Dao
public interface DmActionPlansItemSubmoduleDao {

    @Query("SELECT * FROM DmActionPlansItemSubmodule")
    List<DmActionPlansItemSubmodule> getDmActionPlansItemSubmodule();

    @Insert
    void insert(DmActionPlansItemSubmodule... dmActionPlansItemSubmodules);

    @Delete
    void delete(DmActionPlansItemSubmodule dmActionPlansItemSubmodules);
    @Query("DELETE FROM DmActionPlansItemSubmodule")
    void deleteAll();
}
