package com.olam.digital.ofis.mvvmmodule.model.photocheckbox.Dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.PhotoCheckBoxData;

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


    @Query("SELECT id, questionId, lookupId, fileName FROM photocheckboxdata WHERE isDownload = :isDownloaded")
    Cursor getNoPhotoData(boolean isDownloaded);

    @Query("DELETE FROM photocheckboxdata where active=:active")
    void deleteDeactivatedPhoto(boolean active);

}

