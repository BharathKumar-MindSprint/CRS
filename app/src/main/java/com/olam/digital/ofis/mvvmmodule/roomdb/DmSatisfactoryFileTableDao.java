package com.olam.digital.ofis.mvvmmodule.roomdb;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSatisfactoryFileTable;

import java.util.List;

@Dao
public interface DmSatisfactoryFileTableDao {
    @Query("SELECT * FROM DmSatisfactoryFileTable")
    List<DmSatisfactoryFileTable> getDmSatisfactoryFileTable();

    @Query("SELECT last_insert_rowid()")
    long getLastInsertedId();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DmSatisfactoryFileTable... dmSatisfactoryFileTables);

    @Delete
    void delete(DmSatisfactoryFileTable dmSatisfactoryFileTable);


    @Query("DELETE FROM DmSatisfactoryFileTable WHERE uid =:id")
    void deleteById(long id);
}
