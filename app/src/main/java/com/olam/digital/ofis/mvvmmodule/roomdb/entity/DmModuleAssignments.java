package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmModuleAssignments")
public class DmModuleAssignments {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public int uid;

    @ColumnInfo(name = "moduleId")
    public Integer moduleId;

    @ColumnInfo(name = "assignmentType")
    public String assignmentType;

    @ColumnInfo(name = "assignmentId")
    public Integer assignmentId;

    public DmModuleAssignments(int moduleId, String assignmentType, int assignmentId) {
        this.moduleId = moduleId;
        this.assignmentType = assignmentType;
        this.assignmentId = assignmentId;
    }
}
