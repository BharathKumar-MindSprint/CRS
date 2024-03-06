package com.olam.digital.ofis.mvvmmodule.model.assigmenttype;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AssignmentType")
public class AssignmentType {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "active")
    public boolean active;

    @ColumnInfo(name = "created_at")
    public String created_at;

    @ColumnInfo(name = "updated_at")
    public String updated_at;

    @ColumnInfo(name = "app_id")
    public String app_id;
}
