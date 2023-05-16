package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;

import java.util.ArrayList;
import java.util.List;

public class ResponseModel {

    @SerializedName("diagnosticModules")
    @Expose
    ArrayList<DiagnosticModulesList> diagnosticModules;

    public ResponseModel(ArrayList<DiagnosticModulesList> diagnosticModules) {
        this.diagnosticModules = diagnosticModules;
    }

    public ArrayList<DiagnosticModulesList> getDiagnosticModules() {
        return diagnosticModules;
    }

    public void setDiagnosticModules(ArrayList<DiagnosticModulesList> diagnosticModules) {
        this.diagnosticModules = diagnosticModules;
    }


}
