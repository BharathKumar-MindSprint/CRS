package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "")
public class CourseAssignment {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name="courseId")
    public int course_id;

    @ColumnInfo(name="courseAssignmentId")
    public int courseAssignmentId;

    @ColumnInfo(name="courseAssignmentType")
    public String courseAssignmentType;


}
