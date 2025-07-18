package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "conditional_type")
public class ConditionalType {

    @PrimaryKey
    @NonNull
    private Integer id;

    @SerializedName("value")
    private String value;

    @SerializedName("active")
    private String active;

    public ConditionalType(@NonNull Integer id, String value, String active) {
        this.id = id;
        this.value = value;
        this.active = active;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
