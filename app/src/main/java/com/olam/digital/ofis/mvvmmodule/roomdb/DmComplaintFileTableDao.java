package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;

import java.util.List;


@Dao
public interface DmComplaintFileTableDao {

    @Query("SELECT * FROM DmComplaintFileTable")
    List<DmComplaintFileTable> getDmComplaintFileTable();

    @Query("SELECT * FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId")
    List<DmComplaintFileTable> getFilesById(int id, String farmerId);

   /* @Insert
    void insertAll(List<DmComplaintFileTable> dmComplaintFileTablesAll);*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DmComplaintFileTable... dmComplaintFileTables);

    @Query("SELECT last_insert_rowid()")
    long getLastInsertedId();

    @Delete
    void delete(DmComplaintFileTable dmComplaintFileTables);

    @Query("DELETE FROM DmComplaintFileTable WHERE file_name =:filename")
    void deleteByFileName(String filename);

    @Query("DELETE FROM DmComplaintFileTable WHERE uid =:id AND farmerId = :farmerId")
    void deleteById(long id, String farmerId);

    @Query("DELETE FROM DmComplaintFileTable")
    void deleteAll();

}
