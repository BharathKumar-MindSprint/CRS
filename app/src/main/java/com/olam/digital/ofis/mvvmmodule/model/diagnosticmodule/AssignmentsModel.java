package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AssignmentsModel {
    @SerializedName("assignmentType")
    public String assignmentType;

    @SerializedName("assignmentId")
    public List<Integer> assignmentId;


    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public List<Integer> getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(List<Integer> assignmentId) {
        this.assignmentId = assignmentId;
    }
}
