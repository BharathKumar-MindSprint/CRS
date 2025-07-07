package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SupplierModuleResponse {
    @SerializedName("supplierModules")
    private ArrayList<SupplierSurveyResponse> supplierSurveyModules;

    public SupplierModuleResponse(ArrayList<SupplierSurveyResponse> supplierSurveyModules) {
        this.supplierSurveyModules = supplierSurveyModules;
    }

    public ArrayList<SupplierSurveyResponse> getSupplierSurveyModules() {
        return supplierSurveyModules;
    }

    public void setSupplierSurveyModules(ArrayList<SupplierSurveyResponse> supplierSurveyModules) {
        this.supplierSurveyModules = supplierSurveyModules;
    }
}
