package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRangeActionPlansItems;

import java.util.List;

@Dao
public interface DmRangeActionPlansItemsDao {
    @Query("SELECT * FROM DmRangeActionPlansItems")
    List<DmRangeActionPlansItems> getDmRangeActionPlansItems();

    @Insert
    void insert(DmRangeActionPlansItems... dmRangeActionPlansItems);

    @Delete
    void delete(DmRangeActionPlansItems dmRangeActionPlansItems);

    @Query("DELETE FROM DmRangeActionPlansItems")
    void deleteAll();
}
