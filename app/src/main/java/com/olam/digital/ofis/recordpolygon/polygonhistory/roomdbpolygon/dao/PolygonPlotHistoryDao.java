package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao.LocalUnitDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PolygonPlotHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PolygonPlotHistory... polygonPlotHistoryDao);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PolygonPlotHistory polygonPlotHistoryDao);

    @Delete
    void delete(PolygonPlotHistory polygonPlotHistoryDao);

    @Query("DELETE FROM PolygonPlotHistory")
    void deleteAll();

    @Query("SELECT positionData FROM POLYGONPLOTHISTORY WHERE id=:id")
    String getPositionData(long id);


    @Query("SELECT centerPlot FROM POLYGONPLOTHISTORY WHERE id=:id")
    String getcenterPlotData(long id);

    @Query("SELECT id, plotName FROM PolygonPlotHistory")
    List<PolygonPlotHistoryDao.UnitMapping> getAllPlotCount();

    class UnitMapping {
        public int id;
        public String plotName;
    }


}
