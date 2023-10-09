package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "CapturedProofFollowupTable")
public class CapturedProofFollowupTable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "followupId")
    public int followupId;


    /*private List<String> captureProofList;*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

   /* public List<String> getCaptureProofList() {
        return captureProofList;
    }

    public void setCaptureProofList(List<String> captureProofList) {
        this.captureProofList = captureProofList;
    }*/

}
