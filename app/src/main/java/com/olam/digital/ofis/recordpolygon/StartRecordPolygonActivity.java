package com.olam.digital.ofis.recordpolygon;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.olam.digital.ofis.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Linu C. N. M1032987 on 10/18/2017.
 */

public class StartRecordPolygonActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_polygon);
        List<LocationBeanList>  aLocationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getAllLocations();
        String s = getPlotsJsonFromLocationBeanList(aLocationBeanList);
        Log.v("PLOTS", s);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Title");
        alert.setMessage("Message");
        // Create TextView
        final TextView input = new TextView(this);
        ScrollView scroller = new ScrollView(this);
        scroller.addView(input);
        alert.setView(scroller);
        input.setText(s);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                input.setText("hi");
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    String getPlotsJsonFromLocationBeanList(List<LocationBeanList>  aLocationBeanList) {
        PlotsList lPlotList = new PlotsList();
        List<location> lplotsList = new ArrayList<>();
        List<plots> lposition_dataList = new ArrayList<>();
        location lplots = new location();
        for (int i = 0; i < aLocationBeanList.size(); i++) {
            List<plot> lplotList = new ArrayList<>();
            plots lposition_data = new plots();
            for (plot bean : aLocationBeanList.get(i).getLocationBean()) {
                lplotList.add(bean);
            }
            lposition_data.setPlot(lplotList);
            lposition_dataList.add(lposition_data);
        }
        lplots.setPlots(lposition_dataList);
        lplotsList.add(lplots);
        String json = lPlotList == null ? null : new Gson().toJson(lPlotList);
        return json;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
