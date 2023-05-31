package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import java.util.List;

@Dao
public interface DmActionPlansItemDao {
    @Query("SELECT * FROM DmActionPlansItem")
    List<DmActionPlansItem> getDmActionPlansItem();

    @Insert
    void insert(DmActionPlansItem... dmActionPlansItems);

    @Delete
    void delete(DmActionPlansItem dmActionPlansItem);

    @Query("DELETE FROM DmActionPlansItem")
    void deleteAll();
}
