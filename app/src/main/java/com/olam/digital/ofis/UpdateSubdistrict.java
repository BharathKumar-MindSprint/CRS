package com.olam.digital.ofis;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    EditText newPlaceName;
    int selectedDistrict, selectedPlace;
    boolean editmode = false;
    private TextView tv_country_name, tv_region_name, tv_district_name, tv_place_name;
    Spinner subdistrictSpinner, countrySpinner, regionSpinner, districtSpinner, placeSpinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_subdistrict);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        farmerId=getIntent().getExtras().getInt(ChildLabourUtils.CHILD_FARMER_ID);
        editmode=getIntent().getExtras().getBoolean("EDIT_MODE");
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
       /* tv_country_name=findViewById(R.id.tv_country_name);
        tv_region_name=findViewById(R.id.tv_region_name);
        tv_district_name=findViewById(R.id.tv_district_name);
        tv_place_name=findViewById(R.id.tv_place_name);*/
        countrySpinner = (Spinner) this.findViewById(R.id.spinner_country);
        regionSpinner = (Spinner) this.findViewById(R.id.spinner_region);
        districtSpinner = (Spinner) this.findViewById(R.id.spinner_district);
        subdistrictSpinner = (Spinner) this.findViewById(R.id.spinner_subdistrict);
        placeSpinner = (Spinner) this.findViewById(R.id.spinner_place);

        try {
            farmer = dl.getFarmerById(farmerId);
            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                int selectedCountry = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));
                int selectedRegion = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID)));
                selectedDistrict = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID)));
                selectedPlace = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
                try {
                    selectedSubDistrict = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID)));
                }catch (Exception e){
                    e.printStackTrace();
                }
                String countryName = dl.getCountryName(selectedCountry);
//                tv_country_name.setText(countryName);
                String regionName = dl.getRegionName(selectedRegion);
//                tv_region_name.setText(regionName);
                String districtName = dl.getDistrictName(selectedDistrict);
//                tv_district_name.setText(districtName);
                String placeName = dl.getPlaceName(selectedPlace);

                String subdistricName = dl.getPlaceName(selectedSubDistrict);
//                tv_place_name.setText(placeName);
                dl.createGeographyHierarchySpinners(this, context, null, selectedCountry, selectedRegion, selectedDistrict, selectedSubDistrict, selectedPlace, null, true, 3);
//                dl.loadSubdistrictSpinner(this, context, selectedDistrict, 0);
               /* countrySpinner.setEnabled(false);
                regionSpinner.setEnabled(false);
                districtSpinner.setEnabled(false);*/

            }
        }finally {
            farmer.close();
        }


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.updateSubdistrict) {

            newPlaceName = findViewById(R.id.newPlaceName);
            Spinner spinner_place = findViewById(R.id.spinner_place);
            long place_id = spinner_place.getSelectedItemId();
            if (place_id == 0) {

                Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
                return ;

            }
            String newPlaceNameValue = (place_id == -1) ? String.valueOf(newPlaceName.getText()) : "";
            if (place_id == -1) {
                if (newPlaceNameValue.length() == 0) {

                    Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
                    return ;
                } else {
                    if (dl.placeAlreadyExists(newPlaceNameValue, selectedDistrict)) {
                        Toast.makeText(context, res.getString(R.string.place_name_not_unique), Toast.LENGTH_LONG).show();


                        return ;
                    }
                }
            }
            selectedPlace = (int) placeSpinner.getSelectedItemId();
            if (place_id == -1 && newPlaceName.length() > 0) {
                place_id = dl.insertNewPlace(newPlaceNameValue, selectedDistrict);
                Log.d("AddPlace", String.valueOf(place_id));
            }

            selectedSubDistrict = (int) subdistrictSpinner.getSelectedItemId();

            if (selectedSubDistrict < 1) {
                Toast.makeText(context, res.getString(R.string.fill_subdistrict), Toast.LENGTH_LONG).show();
                return;
            }

            ContentValues contentValue=new ContentValues();
            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID,selectedSubDistrict);
            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISID,selectedSubDistrict);
//            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACEID,selectedPlace);
            contentValue.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID,selectedPlace);
            dl.updateSubdistrict(farmerId, contentValue, olamfarmername);
            Toast.makeText(this,getResources().getString(R.string.subdistrict_note), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }


}
