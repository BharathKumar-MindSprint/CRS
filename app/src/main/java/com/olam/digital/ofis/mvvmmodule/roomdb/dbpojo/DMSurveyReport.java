package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DMSurveyReport")
public class DMSurveyReport {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "totalComplaint")
    private int totalComplaint;

    @ColumnInfo(name = "totalSatisfactory")
    private int totalSatisfactory;

    @ColumnInfo(name = "totalNonComplaint")
    private int totalNonComplaint;

    @ColumnInfo(name = "totalActionPlan")
    private int totalActionPlan;

    @ColumnInfo(name = "totalBudget")
    private double totalBudget;

    @ColumnInfo(name = "resultType")
    private String resultType;

    @ColumnInfo(name = "resultValue")
    private double resultValue;

    @ColumnInfo(name = "resultRemarks")
    private String resultRemarks;

    @ColumnInfo(name = "farmerId")
    private int farmerId;

    // Constructors, getters, and setters

    public DMSurveyReport(int totalComplaint, int totalSatisfactory, int totalNonComplaint, int totalActionPlan,
                          double totalBudget, String resultType, double resultValue, String resultRemarks, int farmerId) {
        this.totalComplaint = totalComplaint;
        this.totalSatisfactory = totalSatisfactory;
        this.totalNonComplaint = totalNonComplaint;
        this.totalActionPlan = totalActionPlan;
        this.totalBudget = totalBudget;
        this.resultType = resultType;
        this.resultValue = resultValue;
        this.resultRemarks = resultRemarks;
        this.farmerId = farmerId;
    }

    // Getters and setters for the properties
    // ...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotalComplaint() {
        return totalComplaint;
    }

    public void setTotalComplaint(int totalComplaint) {
        this.totalComplaint = totalComplaint;
    }

    public int getTotalSatisfactory() {
        return totalSatisfactory;
    }

    public void setTotalSatisfactory(int totalSatisfactory) {
        this.totalSatisfactory = totalSatisfactory;
    }

    public int getTotalNonComplaint() {
        return totalNonComplaint;
    }

    public void setTotalNonComplaint(int totalNonComplaint) {
        this.totalNonComplaint = totalNonComplaint;
    }

    public int getTotalActionPlan() {
        return totalActionPlan;
    }

    public void setTotalActionPlan(int totalActionPlan) {
        this.totalActionPlan = totalActionPlan;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public double getResultValue() {
        return resultValue;
    }

    public void setResultValue(double resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultRemarks() {
        return resultRemarks;
    }

    public void setResultRemarks(String resultRemarks) {
        this.resultRemarks = resultRemarks;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }
}
