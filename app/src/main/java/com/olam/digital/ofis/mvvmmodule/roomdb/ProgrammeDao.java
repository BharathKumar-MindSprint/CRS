package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Programme;

import java.util.List;

@Dao
public interface ProgrammeDao {
    @Query("SELECT * FROM Programme")
    List<Programme> getProgramme();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Programme... Programme);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Programme> programmeList);

    @Query("SELECT * FROM Programme WHERE id = :id")
    List<Programme> getProgrammeById(int id);

    @Query("SELECT name FROM Programme WHERE id = :id")
    String getProgrammeNameById(int id);
    @Delete
    void delete(Programme programme);

    @Query("DELETE FROM Programme")
    void deleteAll();
}
