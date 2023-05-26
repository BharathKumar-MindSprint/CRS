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

    @Query("SELECT * FROM DmQuestionsSubModule WHERE sectionId =:id")
    List<DmQuestionsSubModule> getDmQuestionsSubModulebySectionId(int id);

    @Insert
    void insert(DmQuestionsSubModule... dmQuestionsSubModules);

    @Delete
    void delete(DmQuestionsSubModule dmQuestionsSubModules);

    @Query("DELETE FROM DmQuestionsSubModule")
    void deleteAll();

    @Query("DELETE FROM DmQuestionsSubModule WHERE sectionId =:id")
    void deletebySectionId(int id);
}
