package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmComplaintFileTable;

import java.util.List;


@Dao
public interface DmComplaintFileTableDao {

    @Query("SELECT * FROM DmComplaintFileTable")
    List<DmComplaintFileTable> getDmComplaintFileTable();

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType, fileSize, plotId FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt AND plotId = :plotId")
    List<DmComplaintFileTable> getFilesById(int id, String farmerId, int attempt, int plotId);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType, fileSize, plotId FROM DmComplaintFileTable WHERE  farmerId = :farmerId AND questionId =:questionId AND isFileSynced = 0 AND plotId = :plotId")
    List<DmComplaintFileTable> getUnSyncedFile(String farmerId, String questionId, int plotId);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType, fileSize, plotId FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt AND plotId = :plotId")
    List<DmComplaintFileTable> getQuestionFilesById(int id, String farmerId, int attempt, int plotId);

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

    @Query("DELETE FROM DmComplaintFileTable WHERE uid =:id AND farmerId = :farmerId AND attempt = :attempt AND plotId = :plotId")
    void deleteById(long id, String farmerId, int attempt, int plotId);

    @Query("DELETE FROM DmComplaintFileTable")
    void deleteAll();

    @Query("DELETE FROM DmComplaintFileTable WHERE questionId=:questionID AND farmerId=:farmerID AND attempt = :attempt AND plotId =:plotId")
    void deleteRowsByFarmerAndQuestionID(String questionID , String farmerID , int attempt, int plotId);

    @Query("UPDATE DmComplaintFileTable SET isFileSynced= :value WHERE uid = :uid")
    void updateSyncData(Long uid, boolean value);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType, fileSize, plotId FROM DmComplaintFileTable WHERE questionId=:questionID AND farmerId=:farmerID AND plotId =:plotId")
    List<DmComplaintFileTable> getCaptureProofSize(String questionID , String farmerID, int plotId);

    @Query("SELECT uid, file_name, questionId, farmerId, attempt, filePath, isFileSynced, ofisFileName, surveyType, fileSize, plotId FROM DmComplaintFileTable WHERE questionId = :id AND farmerId = :farmerId AND attempt = :attempt AND surveyType=:surveyType")
    List<DmComplaintFileTable> getTotalFollowCount(int id, String farmerId, int attempt , String surveyType);

    @Query("SELECT filePath FROM DmComplaintFileTable WHERE uid =:id AND farmerId = :farmerId AND attempt = :attempt")
    String getFilePathToDelete(long id, String farmerId, int attempt);

    @Query("SELECT SUM(fileSize) FROM DmComplaintFileTable WHERE farmerId = :farmerId AND questionId = :id AND surveyType=:surveyType AND plotId = :plotId AND isFileSynced = 0")
    int getTotalUnSyncFileSizeByQuestionId(String id, String farmerId, String surveyType, int plotId);

    @Query("SELECT COUNT(uid) FROM DmComplaintFileTable WHERE questionId IN (:questionIds) AND farmerId = :farmerId AND plotId = :plotId AND isFileSynced = 0")
    int getUnSyncedFiles(List<Integer> questionIds, long farmerId, int plotId);

    @Query("UPDATE DmComplaintFileTable SET farmerId = :farmerId WHERE farmerId = :tempFarmerId")
    void updateFarmerId(int tempFarmerId, int farmerId);


}
