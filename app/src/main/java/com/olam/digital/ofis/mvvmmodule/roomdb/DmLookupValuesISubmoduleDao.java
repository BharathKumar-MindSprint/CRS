package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;

import java.util.List;

@Dao
public interface DmLookupValuesISubmoduleDao {
    @Query("SELECT * FROM DmLookupValuesISubmodule")
    List<DmLookupValuesISubmodule> getDmLookupValuesISubmodule();

    @Query("SELECT * FROM DmLookupValuesISubmodule WHERE idQuestionSubModule =:id")
    List<DmLookupValuesISubmodule> getDmAnswersSubModuleByQuestionId(int id);

    @Insert
    void insert(DmLookupValuesISubmodule... dmLookupValuesISubmodules);

    @Delete
    void delete(DmLookupValuesISubmodule dmLookupValuesISubmodules);

    @Query("DELETE FROM DmLookupValuesISubmodule")
    void deleteAll();
}
