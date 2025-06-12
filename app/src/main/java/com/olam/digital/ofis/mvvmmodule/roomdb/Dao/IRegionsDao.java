package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Regions;

import java.util.List;

@Dao
public interface IRegionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Regions> regionsList);

    @Query("DELETE FROM Regions")
    void deleteAll();
}
