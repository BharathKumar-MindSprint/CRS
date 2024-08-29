package com.olam.digital.ofis.mvvmmodule.model.photocheckbox;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "PhotoCheckBoxData")
public class PhotoCheckBoxData {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "questionId")
    public String questionId;

    @ColumnInfo(name = "lookupId")
    public String lookupId;

    @ColumnInfo(name = "fileName")
    public String fileName;

    @ColumnInfo(name = "isDownload", defaultValue = "0")
    public boolean isDownload;

    @ColumnInfo(name = "photoLocation")
    public String photoLocation ;

    @ColumnInfo(name = "active")
    public boolean active;

    @ColumnInfo(name = "moduleId")
    public String moduleId;
}
