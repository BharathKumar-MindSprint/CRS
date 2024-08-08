package com.olam.digital.ofis.mvvmmodule.model.photocheckbox.Dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.PhotoCheckBoxData;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PhotoCheckBoxDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhotoCheckBoxData... photoCheckBoxData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(PhotoCheckBoxData photoCheckBoxData);

    @Delete
    void delete(PhotoCheckBoxData photoCheckBoxData);

    @Query("DELETE FROM PhotoCheckBoxData")
    void deleteAll();

    @Query("UPDATE PhotoCheckBoxData SET photoLocation=:photoLocation , isDownload=:isDownload WHERE questionId=:questionId and lookupId=:lookupId and id=:id")
    void updatePhotoCheckboxUrl(String photoLocation, boolean isDownload, long questionId, long lookupId, long id);

    @Query("SELECT photoLocation FROM PhotoCheckBoxData WHERE lookupId=:lookupId and questionId=:questionId")
    String getPhotoLocation(String lookupId, int questionId);


    @Query("SELECT * FROM photocheckboxdata WHERE isDownload = :isDownloaded")
    List<PhotoCheckBoxData> getNoPhotoData(boolean isDownloaded);

    @Query("SELECT * FROM PhotoCheckBoxData WHERE active=:active")
    List<PhotoCheckBoxData> getInactivePhoto(boolean active);

    @Query("DELETE FROM photocheckboxdata where active=:active")
    void deleteDeactivatedPhoto(boolean active);

    @Query("SELECT * FROM PhotoCheckBoxData group by moduleId")
    List<PhotoCheckBoxData> getPhotoModuleId();

    @Query("DELETE FROM photocheckboxdata where moduleId=:moduleId")
    void deleteInActiveModulePhoto(int moduleId);

    @Query("SELECT * FROM PhotoCheckBoxData WHERE moduleId=:moduleId")
    List<PhotoCheckBoxData> getFileNameByModuleId(int moduleId);
}

