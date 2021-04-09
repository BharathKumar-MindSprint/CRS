package com.olam.digital.ofis;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;

public class UpdateSubdistrict extends BaseActivity implements View.OnClickListener{

    Button updateSubdistrict;
    Long farmerId;
    private Spinner countrySpinner, regionSpinner, districtSpinner, placeSpinner, subdistrictSpinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_subdistrict);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        initEvents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){}


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
        updateSubdistrict=findViewById(R.id.updateSubdistrict);
        countrySpinner = this.findViewById(R.id.spinner_country);
        regionSpinner = this.findViewById(R.id.spinner_region);
        districtSpinner = this.findViewById(R.id.spinner_district);
        subdistrictSpinner = this.findViewById(R.id.spinner_subdistrict);
        placeSpinner = this.findViewById(R.id.spinner_place);

    }

    @Override
    public void onClick(View v) {

        Bundle childBundle=new Bundle();
        childBundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerId));

        if(v.getId() == R.id.updateSubdistrict) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }


}
