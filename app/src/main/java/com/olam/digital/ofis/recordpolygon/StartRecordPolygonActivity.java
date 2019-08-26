package com.olam.digital.ofis.recordpolygon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Linu C. N. M1032987 on 10/18/2017.
 */

public class StartRecordPolygonActivity extends Activity {

    private static final String TAG = StartRecordPolygonActivity.class.getSimpleName();

    private static final String FARMER_ID = "FARMER_ID";
    private static final String FARM_ID = "FARM_ID";
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String FARMER_NAME = "FARMER_NAME";

    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_polygon);
        res = getResources();
        SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmID("41526");
        List<LocationBeanList>  aLocationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getAllLocations();
        String s = getPlotsJsonFromLocationBeanList(aLocationBeanList);
        Log.v("PLOTS", s);
        saveToSyncDatabase(s);
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


    void saveToSyncDatabase(String plotsJson) {
        DataLayer dl = new DataLayer(getResources(), StartRecordPolygonActivity.this);
        if (plotsJson != null) {
            plotsJson = Base64.encodeToString(plotsJson.getBytes(), Base64.NO_WRAP);
            int updated = dl.updateFarmPolygon(48592, plotsJson, "VI-1358-CO38", "BUI MANH HUNG");
        }
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
        lPlotList.setLocation(lplotsList);
        String json = lPlotList == null ? null : new Gson().toJson(lPlotList);
        return json;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        final EditText edTxtNumberOfPlots = new EditText(this);
//        edTxtNumberOfPlots.setInputType(InputType.TYPE_CLASS_NUMBER);
//        edTxtNumberOfPlots.setHint(res.getString(R.string.record_polygon_input_hint));
//        edTxtNumberOfPlots.setText("6");
//        new AlertDialog.Builder(this)
//                .setTitle(res.getString(R.string.record_polygon_input_title))
//                .setMessage(res.getString(R.string.record_polygon_input_messege))
//                .setView(edTxtNumberOfPlots)
//                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        long farmerId = 234L;
//                        long farmId = 234L;
//                        String lPlotCountString = edTxtNumberOfPlots.getText().toString();
//                        int lPlotCount = 0;
//                        if (!TextUtils.isEmpty(lPlotCountString)) {
//                            lPlotCount = Integer.parseInt(lPlotCountString);
//                        }
//                        Intent recordPolygonIntent = new Intent(StartRecordPolygonActivity.this, RecordPolygonActivity.class);
//                        recordPolygonIntent.putExtra(FARMER_ID, farmerId);
//                        recordPolygonIntent.putExtra(FARM_ID, farmId);
//                        recordPolygonIntent.putExtra(OLAM_FARMER_ID, "CI-0006-BA44-0000");
//                        recordPolygonIntent.putExtra(FARMER_NAME, "COULIABLY DRISSA");
//                        recordPolygonIntent.putExtra(PLOT_COUNT, lPlotCount);
//                        recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
//                        startActivity(recordPolygonIntent);
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                }).show();
    }
}
