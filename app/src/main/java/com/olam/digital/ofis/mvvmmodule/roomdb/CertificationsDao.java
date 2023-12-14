package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Certifications;

import java.util.List;

@Dao
public interface CertificationsDao {
    @Query("SELECT * FROM Certifications")
    List<Certifications> getCertifications();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Certifications... certifications);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Certifications> certificationsList);

    @Query("SELECT * FROM Certifications WHERE certificationId = :id")
    List<Certifications> getCertificationsById(int id);

    @Delete
    void delete(Certifications certifications);

    @Query("DELETE FROM Certifications")
    void deleteAll();
}
