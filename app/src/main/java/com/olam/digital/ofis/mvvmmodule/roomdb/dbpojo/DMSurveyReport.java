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
    private String resultValue;

    @ColumnInfo(name = "resultRemarks")
    private String resultRemarks;

    @ColumnInfo(name = "resultColour")
    private String resultColour;

    public String getResultColour() {
        return resultColour;
    }

    public void setResultColour(String resultColour) {
        this.resultColour = resultColour;
    }

    @ColumnInfo(name = "submissionDate")
    private String submissionDate;

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    @ColumnInfo(name = "farmerId")
    private long farmerId;

    @ColumnInfo(name = "moduleId")
    private int moduleId;

    @ColumnInfo(name = "farmerGroupId")
    private int farmerGroupId;

    @ColumnInfo(name = "currencyId")
    private int currencyId;

    @ColumnInfo(name = "overAllResult")
    private String overAllResult;

    @ColumnInfo(name = "plotId", defaultValue = "0")
    private Integer plotId;

    public String getOverAllResult() {
        return overAllResult;
    }

    public void setOverAllResult(String overAllResult) {
        this.overAllResult = overAllResult;
    }
// Constructors, getters, and setters

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getFarmerGroupId() {
        return farmerGroupId;
    }

    public void setFarmerGroupId(int farmerGroupId) {
        this.farmerGroupId = farmerGroupId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public DMSurveyReport(int totalComplaint, int totalSatisfactory, int totalNonComplaint, int totalActionPlan,
                          double totalBudget, String resultType, String resultValue, String resultRemarks ,long farmerId,
                          int moduleId,int farmerGroupId,int currencyId,String submissionDate, String resultColour,String overAllResult, int plotId) {
        this.totalComplaint = totalComplaint;
        this.totalSatisfactory = totalSatisfactory;
        this.totalNonComplaint = totalNonComplaint;
        this.totalActionPlan = totalActionPlan;
        this.totalBudget = totalBudget;
        this.resultType = resultType;
        this.resultValue = resultValue;
        this.resultRemarks = resultRemarks;
        this.farmerId = farmerId;
        this.moduleId = moduleId;
        this.farmerGroupId = farmerGroupId;
        this.currencyId = currencyId;
        this.submissionDate = submissionDate;
        this.resultColour = resultColour;
        this.overAllResult = overAllResult;
        this.plotId = plotId;
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

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultRemarks() {
        return resultRemarks;
    }

    public void setResultRemarks(String resultRemarks) {
        this.resultRemarks = resultRemarks;
    }

    public long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(long farmerId) {
        this.farmerId = farmerId;
    }

    public Integer getPlotId() {
        return plotId;
    }

    public void setPlotId(Integer plotId) {
        this.plotId = plotId;
    }

}
