package com.olam.digital.ofis.childlabour;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class AddNewChildActivity extends BaseActivity implements  View.OnClickListener{

    private EditText fullnameEditText;
    private Button maleButton, femaleButton;
    private Spinner ageSpinner;
    private Button addNewChildButton;
    private String gender;
    private String age;
    private String farmerName;
    private ArrayList<String> ageList;
    DataLayer dataLayer;
    String farmerId;
    String action;
    String childId;
    ChildrenDTO childrenDTO;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        dataLayer = new DataLayer(getResources(), this);
        initComponents();
        initEvents();
        setAgeSpinner();
        getBundleValues();
        if(farmerId!=null){
            farmerName=new ChildLabourLogic().getFarmerName(this,farmerId);
        }
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age=ageList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*if(getIntent().getStringExtra("action").equalsIgnoreCase("edit")){
            initData();
        }*/


    }

    private void initEvents() {
        femaleButton.setOnClickListener(this);
        maleButton.setOnClickListener(this);
        addNewChildButton.setOnClickListener(this);
        maleButton.performClick();
    }

    private void initComponents() {
        fullnameEditText=findViewById(R.id.fullnameEditText);
        maleButton=findViewById(R.id.maleButton);
        femaleButton=findViewById(R.id.femaleButton);
        ageSpinner=findViewById(R.id.ageSpinner);
        addNewChildButton=findViewById(R.id.addNewChildButton);
        TextView titlename = findViewById(R.id.title_name);
        TextView titleage = findViewById(R.id.title_age);
        TextView titlegender = findViewById(R.id.title_gender);
        titlename.setText(Html.fromHtml(titlename.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
        titleage.setText(Html.fromHtml(titleage.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
        titlegender.setText(Html.fromHtml(titlegender.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onClick(View v) {
         if (v.getId() == R.id.addNewChildButton){
             addNewChild();
         }else if (v.getId() == R.id.maleButton) {
             gender = getResources().getString(R.string.gender_male);
             femaleButton.setBackgroundColor(getResources().getColor(R.color.gender_selected));
             maleButton.setBackgroundResource(R.drawable.gender_male_selected);

             femaleButton.setTextColor(getResources().getColor(R.color.gender_selected_text));
             maleButton.setTextColor(getResources().getColor(android.R.color.black));

         } else if (v.getId() == R.id.femaleButton) {
             gender = getResources().getString(R.string.gender_female);

             femaleButton.setBackgroundResource(R.drawable.gender_female_selected);
             femaleButton.setTextColor(getResources().getColor(android.R.color.black));

             maleButton.setBackgroundColor(getResources().getColor(R.color.gender_selected));
             maleButton.setTextColor(getResources().getColor(R.color.gender_selected_text));

         }
    }

    private void setAgeSpinner() {
         ageList = new ArrayList<String>();
        for (int i = 0; i <= 17; i++) {
            ageList.add(Integer.toString(i));
        }
        adapter = new ArrayAdapter<String>(this, R.layout.rowview_age_spinner, ageList);
        adapter.setDropDownViewResource(R.layout.rowview_age_spinner);

        ageSpinner.setAdapter(adapter);
    }

    private void addNewChild() {
        String name=fullnameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(AddNewChildActivity.this,getResources().getString(R.string.fill_child_name),Toast.LENGTH_LONG).show();
            return;
        }
        String age = ageSpinner.getSelectedItem().toString();
        if(getIntent().getStringExtra("action").equalsIgnoreCase("new")){
            submitChild(name);
        }else {
            UpdateChild(name);
        }
    }

    private void UpdateChild(String name) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, name);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(age));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, gender);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 0);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_ID, childId);

       /* ContentValues updatecontentValues=new ContentValues();
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, childrenDTO.getFarmerId());
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, childrenDTO.getName());
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(childrenDTO.getAge()));
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, childrenDTO.getGender());
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, childrenDTO.getAgeOnYear());
        updatecontentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT, childrenDTO.getCreatedAt());*/
        long newChildId= new ChildLabourLogic().updateChildDetails(this,contentValues,childrenDTO.getId(),farmerName);
        if (newChildId != -1){
            Toast.makeText(this,getResources().getString(R.string.child_register_successfully),Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void submitChild(String name) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, name);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(age));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, gender);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 0);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT, timestamp.toString());

       long newChildId= new ChildLabourLogic().insertChildDetails(this,contentValues,farmerName);
       if (newChildId != -1){
           Toast.makeText(this,getResources().getString(R.string.child_register_successfully),Toast.LENGTH_LONG).show();
           finish();
       }
    }

    public void getBundleValues() {
        try{
            Bundle bundle = this.getIntent().getExtras();
            action=bundle.getString("action");
                if (action.equalsIgnoreCase("new")){
                    addNewChildButton.setText(getResources().getString(R.string.add_child));
                    farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
                }else if(action.equalsIgnoreCase("edit")){
                    addNewChildButton.setText(getResources().getString(R.string.update_child));
                    farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
                    childrenDTO= (ChildrenDTO) bundle.getSerializable("children");
                    setValue(childrenDTO);
                }else if(action.equalsIgnoreCase("sync_edit")){
                    addNewChildButton.setText(getResources().getString(R.string.update_child));
                    farmerId = getIntent().getStringExtra(ChildLabourUtils.CHILD_FARMER_ID);
                    childId = getIntent().getStringExtra(ChildLabourUtils.CHILD_ID);
                    childrenDTO= new ChildLabourLogic().getChildrenDTO(this,childId);
                    setValue(childrenDTO);


                }
        }catch (Exception e){
                e.printStackTrace();
        }

    }

    private void setValue(ChildrenDTO childrenDTO) {
        fullnameEditText.setText(childrenDTO.getName());

        if(childrenDTO.getGender().equalsIgnoreCase(ChildLabourUtils.MALE)){
           maleButton.performClick();
        }else if(childrenDTO.getGender().equalsIgnoreCase(ChildLabourUtils.FEMALE)){
           femaleButton.performClick();
        }
        childId = childrenDTO.getId();
        if (childrenDTO.getAge() != null) {
            int spinnerPosition = adapter.getPosition(childrenDTO.getAge());
            ageSpinner.setSelection(spinnerPosition);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

}
