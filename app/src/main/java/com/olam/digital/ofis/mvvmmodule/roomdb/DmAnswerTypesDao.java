package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAnswerTypes;

import java.util.List;

@Dao
public interface DmAnswerTypesDao {

    @Query("SELECT * FROM DmAnswerTypes")
    List<DmAnswerTypes> getDmAnswerTypes();

    @Insert
    void insert(DmAnswerTypes... dmAnswerTypes);

    @Delete
    void delete(DmAnswerTypes dmAnswerTypes);

    @Query("DELETE FROM DmAnswerTypes")
    void deleteAll();

}
