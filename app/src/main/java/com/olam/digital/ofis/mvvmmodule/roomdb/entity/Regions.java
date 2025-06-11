package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Regions")
public class Regions {
    @PrimaryKey
    public int id;
    public int country_id;
    public String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCountry_id() {
        return country_id;
    }

    public void setCountry_id(int country_id) {
        this.country_id = country_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
