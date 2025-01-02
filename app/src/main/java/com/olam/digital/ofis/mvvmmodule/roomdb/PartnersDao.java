package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Partners;

import java.util.List;

@Dao
public interface PartnersDao {
    @Query("SELECT * FROM Partners")
    List<Partners> getPartners();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Partners... partners);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Partners> partnersList);

    @Query("SELECT * FROM Partners WHERE id = :id")
    List<Partners> getPartnersById(int id);

    @Query("SELECT name FROM Partners WHERE id = :id")
    String getPartnerNameById(int id);
    @Delete
    void delete(Partners partners);

    @Query("DELETE FROM Partners")
    void deleteAll();
}
