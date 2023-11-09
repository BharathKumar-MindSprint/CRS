package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.CBU.ValidationOnRange;

import java.util.List;

@Dao
public interface ValidationOnRangeDao {

    @Query("SELECT * FROM ValidationOnRange")
    List<ValidationOnRange> getValidationOnRanges();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ValidationOnRange... validationOnRanges);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ValidationOnRange> validationOnRangeList);

    @Query("SELECT * FROM ValidationOnRange WHERE id = :id")
    List<ValidationOnRange> getValidationOnRangeById(int id);

    @Query("SELECT id FROM ValidationOnRange WHERE value =  \"Alert & Review\" ")
    int getValidationOnRangeId();

    @Delete
    void delete(ValidationOnRange validationOnRange);

    @Query("DELETE FROM ValidationOnRange")
    void deleteAll();
}
