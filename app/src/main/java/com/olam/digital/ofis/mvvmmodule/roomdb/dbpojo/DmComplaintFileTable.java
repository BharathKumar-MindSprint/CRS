package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmComplaintFileTable")
public class DmComplaintFileTable {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "file_name")
    private String file_name;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] fileData;

    public long getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }



}
