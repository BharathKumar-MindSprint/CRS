package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Places")
public class Places {
    @PrimaryKey
    public int id;
    public int district_id;
    public String type;
    public String name;
    public int subdistrictId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDistrict_id() {
        return district_id;
    }

    public void setDistrict_id(int district_id) {
        this.district_id = district_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubdistrictId() {
        return subdistrictId;
    }

    public void setSubdistrictId(int subdistrictId) {
        this.subdistrictId = subdistrictId;
    }
}
