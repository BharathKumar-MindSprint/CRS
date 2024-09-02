package com.olam.digital.ofis.mvvmmodule.model.photocheckbox.ResponseModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.PhotoCheckBoxData;

import java.util.ArrayList;

public class PhotoCheckBoxResponseModel {

    @SerializedName("syncDate")
    @Expose
    public String syncDate;

    @SerializedName("container")
    @Expose
    public String container;

    @SerializedName("photoCheckBoxList")
    @Expose
    public ArrayList<PhotoCheckBoxData> photoCheckBoxList;

    @SerializedName("moduleNotInUse")
    @Expose
    public ArrayList<Integer> moduleNotInUse;

    @SerializedName("count")
    @Expose
    public String count;

    public String getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(String syncDate) {
        this.syncDate = syncDate;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public ArrayList<PhotoCheckBoxData> getPhotoCheckBoxList() {
        return photoCheckBoxList;
    }

    public void setPhotoCheckBoxList(ArrayList<PhotoCheckBoxData> photoCheckBoxList) {
        this.photoCheckBoxList = photoCheckBoxList;
    }

    public ArrayList<Integer> getModuleNotInUse() {
        return moduleNotInUse;
    }

    public void setModuleNotInUse(ArrayList<Integer> moduleNotInUse) {
        this.moduleNotInUse = moduleNotInUse;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public PhotoCheckBoxResponseModel(String syncDate, String container, ArrayList<PhotoCheckBoxData> photoCheckBoxList, ArrayList<Integer> moduleNotInUse, String count) {
        this.syncDate = syncDate;
        this.container = container;
        this.photoCheckBoxList = photoCheckBoxList;
        this.moduleNotInUse = moduleNotInUse;
        this.count = count;
    }
}
