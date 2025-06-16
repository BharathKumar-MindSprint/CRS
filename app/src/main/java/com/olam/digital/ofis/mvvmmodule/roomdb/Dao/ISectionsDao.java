package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Sections;

import java.util.List;

@Dao
public interface ISectionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Sections> sections);

    @Query("DELETE FROM Sections")
    void deleteAll();
}
