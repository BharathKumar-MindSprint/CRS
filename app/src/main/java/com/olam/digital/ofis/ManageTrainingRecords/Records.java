package com.olam.digital.ofis.ManageTrainingRecords;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.Common.Utils.DateTimeUtils;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.TrainingRecords;
import com.olam.digital.ofis.Utilities;

public class Records extends BaseActivity {
private RelativeLayout Manage_Training_Records,Training_Records;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        Log.e("Records", "onCreate: called" );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Manage_Training_Records=findViewById(R.id.manage_training_btn);
        Training_Records=findViewById(R.id.training_records_btn);
        boolean isEnableAgroLearn = Utilities.getBoolFromSharedPrefs(context, Utilities.AGROLEARN_ENUMERATOR,false);
        if(isEnableAgroLearn) {
            Manage_Training_Records.setVisibility(View.VISIBLE);
        }else{
            Manage_Training_Records.setVisibility(View.GONE);
        }
        Manage_Training_Records.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ManagingTrainingActivity.class);
                context.startActivity(intent);
            }
        });

        Training_Records.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TrainingRecords.class);
                context.startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
       DateTimeUtils.checkTime(this, this);
        Log.e("Records", "onResume: called" );
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
       DateTimeUtils.checkTime(this, this);
        Log.e("Records", "onRestoreInstanceState: called" );
    }
}