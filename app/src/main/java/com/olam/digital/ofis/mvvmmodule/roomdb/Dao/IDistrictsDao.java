package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Districts;

import java.util.List;

@Dao
public interface IDistrictsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Districts> districtsList);

    @Query("DELETE FROM Districts")
    void deleteAll();
}
