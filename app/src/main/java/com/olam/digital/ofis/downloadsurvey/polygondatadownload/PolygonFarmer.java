package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.R;

public class PolygonFarmer extends BaseListActivity {

    private EditText findFarmerSearchBox;
    private ListView listFarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findFarmerSearchBox = (EditText) findViewById(R.id.findFarmerSearchBoxPolygon);
        listFarmer = (ListView) findViewById(R.id.listFarmer);

    }
}