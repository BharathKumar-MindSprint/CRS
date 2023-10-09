package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FollowUpResultTable")
public class FollowUpResultTable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "totalSatisfactory")
    public int totalSatisfactory;

    @ColumnInfo(name = "resultValue")
    public String resultValue;

    @ColumnInfo(name = "totalActionPlan")
    public int totalActionPlan;

    @ColumnInfo(name = "totalBudget")
    public String totalBudget;

    @ColumnInfo(name = "totalComplaint")
    public int totalComplaint;

    @ColumnInfo(name = "resultColour")
    public String resultColour;

    @ColumnInfo(name = "resultType")
    public String resultType;

    @ColumnInfo(name = "totalNonComplaint")
    public int totalNonComplaint;

    @ColumnInfo(name = "resultRemarks")
    public String resultRemarks;
}
