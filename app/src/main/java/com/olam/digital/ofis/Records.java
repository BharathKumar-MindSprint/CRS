package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class Records extends BaseActivity {
private RelativeLayout Manage_Training_Records,Training_Records;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Manage_Training_Records=findViewById(R.id.manage_training_btn);
        Training_Records=findViewById(R.id.training_records_btn);

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
}