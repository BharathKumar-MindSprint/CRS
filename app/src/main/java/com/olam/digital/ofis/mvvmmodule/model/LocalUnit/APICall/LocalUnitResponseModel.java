package com.olam.digital.ofis.mvvmmodule.model.LocalUnit.APICall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.ListItems.OutputData;

public class LocalUnitResponseModel {

    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("outputData")
    @Expose
    public OutputData outputData;

    @SerializedName("resultcount")
    @Expose
    public String resultcount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OutputData getOutputData() {
        return outputData;
    }

    public void setOutputData(OutputData outputData) {
        this.outputData = outputData;
    }

    public String getResultcount() {
        return resultcount;
    }

    public void setResultcount(String resultcount) {
        this.resultcount = resultcount;
    }

    public LocalUnitResponseModel(String status, OutputData outputData, String resultcount) {
        this.status = status;
        this.outputData = outputData;
        this.resultcount = resultcount;
    }
}
