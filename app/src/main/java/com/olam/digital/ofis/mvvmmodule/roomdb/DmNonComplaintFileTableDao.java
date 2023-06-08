package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmNonComplaintFileTable;

import java.util.List;

@Dao
public interface DmNonComplaintFileTableDao {
    @Query("SELECT * FROM DmNonComplaintFileTable")
    List<DmNonComplaintFileTable> getDmNonComplaintFileTable();

    @Query("SELECT last_insert_rowid()")
    long getLastInsertedId();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DmNonComplaintFileTable... dmNonComplaintFileTables);

    @Delete
    void delete(DmNonComplaintFileTable dmNonComplaintFileTable);


    @Query("DELETE FROM DmNonComplaintFileTable WHERE uid =:id")
    void deleteById(long id);
}
