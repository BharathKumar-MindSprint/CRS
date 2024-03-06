package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.assigmenttype.AssignmentType;

import java.util.List;

@Dao
public interface AssignmentTypeDao {

    @Query("SELECT * FROM AssignmentType")
    List<AssignmentType> getAssignmentType();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssignmentType... assignmentTypes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AssignmentType> assignmentTypeList);

    @Delete
    void delete(AssignmentType assignmentType);

    @Query("DELETE FROM AssignmentType")
    void deleteAll();
}
