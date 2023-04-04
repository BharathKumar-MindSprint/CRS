package com.olam.digital.ofis.farmplandownload.model;

import com.google.gson.annotations.SerializedName;

public class DownloadUrlRequestBody {
    public String getRootobject() {
        return rootobject;
    }

    public void setRootobject(String rootobject) {
        this.rootobject = rootobject;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @SerializedName("rootobject")
    String rootobject;

    @SerializedName("filepath")
    String filepath;

    public DownloadUrlRequestBody(String rootobject, String filepath) {
        this.rootobject = rootobject;
        this.filepath = filepath;
    }
}
