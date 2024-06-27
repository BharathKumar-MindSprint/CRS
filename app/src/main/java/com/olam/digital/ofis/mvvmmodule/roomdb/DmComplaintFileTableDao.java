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

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt")
    List<DmComplaintFileTable> getFilesById(int id, String farmerId, int attempt);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType FROM DmComplaintFileTable WHERE  isFileSynced = 0")
    List<DmComplaintFileTable> getUnSyncedFile();

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt")
    List<DmComplaintFileTable> getQuestionFilesById(int id, String farmerId, int attempt);

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

    @Query("DELETE FROM DmComplaintFileTable WHERE uid =:id AND farmerId = :farmerId AND attempt = :attempt")
    void deleteById(long id, String farmerId, int attempt);

    @Query("DELETE FROM DmComplaintFileTable")
    void deleteAll();

    @Query("DELETE FROM DmComplaintFileTable WHERE questionId=:questionID AND farmerId=:farmerID AND attempt = :attempt")
    void deleteRowsByFarmerAndQuestionID(String questionID , String farmerID , int attempt);

    @Query("UPDATE DmComplaintFileTable SET isFileSynced= :value WHERE uid = :uid")
    void updateSyncData(Long uid, boolean value);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType FROM DmComplaintFileTable WHERE questionId=:questionID AND farmerId=:farmerID")
    List<DmComplaintFileTable> getCaptureProofSize(String questionID , String farmerID );

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt AND surveyType=:surveyType")
    List<DmComplaintFileTable> getTotalFollowCount(int id, String farmerId, int attempt , String surveyType);

    @Query("SELECT filePath FROM DmComplaintFileTable WHERE uid =:id AND farmerId = :farmerId AND attempt = :attempt")
    String getFilePathToDelete(long id, String farmerId, int attempt);

}
