package com.olam.digital.ofis;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;

public class UpdateSubdistrict extends BaseActivity implements View.OnClickListener{

    Button updateSubdistrict;
    int farmerId, selectedSubDistrict;
    String olamfarmername;
    private TextView tv_country_name, tv_region_name, tv_district_name, tv_place_name;
    private Spinner subdistrictSpinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_subdistrict);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        farmerId=getIntent().getExtras().getInt(ChildLabourUtils.CHILD_FARMER_ID);
        olamfarmername=getIntent().getExtras().getString("olamfarmername");
        initComponents();
        initEvents();

    }



    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_VIEW_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_REGISTER_CHILD_ACTIVITY, ScreenNameConstants.SCREEN_REGISTER_CHILD);

    }

    private void initEvents() {
        updateSubdistrict.setOnClickListener(this);
    }

    private void initComponents() {
        Cursor farmer = null;
        updateSubdistrict=findViewById(R.id.updateSubdistrict);
        tv_country_name=findViewById(R.id.tv_country_name);
        tv_region_name=findViewById(R.id.tv_region_name);
        tv_district_name=findViewById(R.id.tv_district_name);
        tv_place_name=findViewById(R.id.tv_place_name);

        subdistrictSpinner = this.findViewById(R.id.spinner_subdistrict);
        try {
            farmer = dl.getFarmerById(farmerId);
            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                int selectedCountry = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));
                int selectedRegion = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID)));
                int selectedDistrict = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID)));
                int selectedPlace = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
                String countryName = dl.getCountryName(selectedCountry);
                tv_country_name.setText(countryName);
                String regionName = dl.getRegionName(selectedRegion);
                tv_region_name.setText(regionName);
                String districtName = dl.getDistrictName(selectedDistrict);
                tv_district_name.setText(districtName);
                String placeName = dl.getPlaceName(selectedPlace);
                tv_place_name.setText(placeName);

                dl.loadSubdistrictSpinner(this, context, selectedDistrict, 0);
            }
        }finally {
            farmer.close();
        }


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.updateSubdistrict) {
            selectedSubDistrict = (int) subdistrictSpinner.getSelectedItemId();
            Toast.makeText(this,getResources().getString(R.string.subdistrict_note), Toast.LENGTH_LONG).show();
            ContentValues contentValue=new ContentValues();
            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID,selectedSubDistrict);
            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISID,selectedSubDistrict);
            dl.updateSubdistrict(farmerId, contentValue, olamfarmername);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }


}
