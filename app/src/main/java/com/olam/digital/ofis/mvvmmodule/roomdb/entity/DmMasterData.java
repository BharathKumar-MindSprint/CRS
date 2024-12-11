package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmMasterData")
public class DmMasterData {
    
    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    private int uid;
    
    @ColumnInfo(name = "id")
    private Integer id;
    
    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "value")
    private String value;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
