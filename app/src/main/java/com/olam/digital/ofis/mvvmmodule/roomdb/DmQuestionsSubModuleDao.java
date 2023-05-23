package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;

import java.util.List;

@Dao
public interface DmQuestionsSubModuleDao {
    @Query("SELECT * FROM DmQuestionsSubModule")
    List<DmQuestionsSubModule> getDmQuestionsSubModule();

    @Insert
    void insert(DmQuestionsSubModule... dmQuestionsSubModules);

    @Delete
    void delete(DmQuestionsSubModule dmQuestionsSubModules);
}
