package com.olam.digital.ofis.farmplandownload;

import android.view.View;

import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;

import java.util.List;

public interface ItemClickListener {
    void onClick( String fileUrl, String filename);
}
