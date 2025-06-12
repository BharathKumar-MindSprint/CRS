package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sections")
public class Sections {
    @PrimaryKey
    public int id;
    public int farmergroup_id;
    public String code;
    public String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFarmergroup_id() {
        return farmergroup_id;
    }

    public void setFarmergroup_id(int farmergroup_id) {
        this.farmergroup_id = farmergroup_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
