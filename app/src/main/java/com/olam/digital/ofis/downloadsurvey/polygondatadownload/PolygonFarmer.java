package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.os.Bundle;

import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.R;

public class PolygonFarmer extends BaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}