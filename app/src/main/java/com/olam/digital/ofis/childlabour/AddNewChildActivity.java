package com.olam.digital.ofis.childlabour;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;

import java.util.ArrayList;
import java.util.Calendar;

public class AddNewChildActivity extends AppCompatActivity implements  View.OnClickListener{

    private EditText fullnameEditText;
    private AppCompatButton maleButton, femaleButton;
    private AppCompatSpinner ageSpinner;
    private AppCompatButton addNewChildButton;
    private String gender;
    private String age;
    private ArrayList<String> ageList;
    DataLayer dataLayer;
    String farmerId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);
        dataLayer = new DataLayer(getResources(), this);
        initComponents();
        initEvents();
        setAgeSpinner();
        getBundleValues();
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age=ageList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initEvents() {
        femaleButton.setOnClickListener(this);
        maleButton.setOnClickListener(this);
        addNewChildButton.setOnClickListener(this);
    }

    private void initComponents() {
        fullnameEditText=findViewById(R.id.fullnameEditText);
        maleButton=findViewById(R.id.maleButton);
        femaleButton=findViewById(R.id.femaleButton);
        ageSpinner=findViewById(R.id.ageSpinner);
        addNewChildButton=findViewById(R.id.addNewChildButton);
    }

    @Override
    public void onClick(View v) {
         if (v.getId() == R.id.addNewChildButton){
             addNewChild();
         }else if(v.getId() == R.id.maleButton){
              gender=ChildLabourUtils.MALE;
        }else if(v.getId() == R.id.femaleButton){
             gender=ChildLabourUtils.FEMALE;
        }
    }

    private void setAgeSpinner() {
         ageList = new ArrayList<String>();
        for (int i = 0; i <= 17; i++) {
            ageList.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ageList);
        ageSpinner.setAdapter(adapter);
    }

    private void addNewChild() {
        String name=fullnameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(AddNewChildActivity.this,getResources().getString(R.string.fill_name),Toast.LENGTH_LONG).show();
            return;
        }
        submitChild(name);
    }


    private void submitChild(String name) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, name);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(age));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, gender);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 0);

       long newChildId= new ChildLabourLogic().insertChildDetails(this,contentValues);
       if (newChildId != -1){
           Toast.makeText(this,getResources().getString(R.string.child_register_successfully),Toast.LENGTH_LONG).show();
           finish();
       }
    }

    public void getBundleValues() {
        Bundle bundle = getIntent().getExtras();
        farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
    }

}
