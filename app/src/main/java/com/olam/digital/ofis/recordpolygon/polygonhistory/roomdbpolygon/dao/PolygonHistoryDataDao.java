package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.list.PolygonHistoryDataList;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PolygonHistoryDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PolygonHistoryData... polygonHistoryData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PolygonHistoryData polygonHistoryData);

    @Delete
    void delete(PolygonHistoryData globalUnit);

    @Query("DELETE FROM PolygonHistoryData")
    void deleteAll();

    @Query("SELECT primary_lat FROM PolygonHistoryData WHERE farmerId = :id")
    double getLattitude(long id);

    @Query("SELECT primary_lng FROM PolygonHistoryData WHERE farmerId = :id")
    double getLongitude(long id);


}
