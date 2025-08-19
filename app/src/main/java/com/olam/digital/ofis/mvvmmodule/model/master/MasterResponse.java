package com.olam.digital.ofis.mvvmmodule.model.master;

import com.google.gson.annotations.SerializedName;

public class MasterResponse {
    @SerializedName("outputdata")
    public MasterDataResponse masterDataResponse;
    public int resultcount;
    public String status;

    public MasterDataResponse getMasterDataResponse() {
        return masterDataResponse;
    }

    public void setMasterDataResponse(MasterDataResponse masterDataResponse) {
        this.masterDataResponse = masterDataResponse;
    }

    public int getResultcount() {
        return resultcount;
    }

    public void setResultcount(int resultcount) {
        this.resultcount = resultcount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
