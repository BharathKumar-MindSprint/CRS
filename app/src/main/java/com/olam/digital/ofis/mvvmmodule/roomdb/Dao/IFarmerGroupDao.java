package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.FarmerGroup;

import java.util.List;

@Dao
public interface IFarmerGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FarmerGroup> farmerGroups);

    @Query("DELETE FROM FarmerGroup")
    void deleteAll();
}
