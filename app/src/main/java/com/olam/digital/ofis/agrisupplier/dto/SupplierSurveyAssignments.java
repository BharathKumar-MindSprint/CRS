package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SupplierSurveyAssignments {

    @SerializedName("assignmentType")
    private int assignmentType;
    @SerializedName("assignmentValue")
    private List<Integer> assignmentValue;
    @SerializedName("isCustomRecurrence")
    private boolean isCustomRecurrence;
    @SerializedName("recurrenceFrequency")
    private int recurrenceFrequency;
    @SerializedName("recurrenceType")
    private String recurrenceType;
    @SerializedName("startDate")
    private String startDate;

    public SupplierSurveyAssignments(int assignmentType, List<Integer> assignmentValue, boolean isCustomRecurrence, int recurrenceFrequency, String recurrenceType, String startDate) {
        this.assignmentType = assignmentType;
        this.assignmentValue = assignmentValue;
        this.isCustomRecurrence = isCustomRecurrence;
        this.recurrenceFrequency = recurrenceFrequency;
        this.recurrenceType = recurrenceType;
        this.startDate = startDate;
    }

    public int getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(int assignmentType) {
        this.assignmentType = assignmentType;
    }

    public List<Integer> getAssignmentValue() {
        return assignmentValue;
    }

    public void setAssignmentValue(List<Integer> assignmentValue) {
        this.assignmentValue = assignmentValue;
    }

    public boolean isCustomRecurrence() {
        return isCustomRecurrence;
    }

    public void setCustomRecurrence(boolean customRecurrence) {
        isCustomRecurrence = customRecurrence;
    }

    public int getRecurrenceFrequency() {
        return recurrenceFrequency;
    }

    public void setRecurrenceFrequency(int recurrenceFrequency) {
        this.recurrenceFrequency = recurrenceFrequency;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
