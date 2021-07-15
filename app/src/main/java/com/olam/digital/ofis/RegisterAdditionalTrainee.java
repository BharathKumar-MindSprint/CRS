package com.olam.digital.ofis;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.model.Attendee;

import java.util.ArrayList;

public class RegisterAdditionalTrainee extends BaseActivity {

    // Widgets
    LinearLayout containerLayout;
    ImageButton btnAddAttendee;
    TextView tvFarmerName, tvAddAttendeeDesc;
    ViewGroup parentGroup;
    LayoutInflater inflater;
    Button btnSaveAttendees;
    ScrollView scrollView;

    //Variables
    int trainingRecordId;
    long farmerId;
    boolean editAttendee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register_additional_trainee);
        initWidget();
        getIntentDetails();
        setInitialData();
        fillTraineeRelationship();
        if(parentGroup.getChildCount()==0){
            inflateAttendeeLayout();
        }
        if (!editAttendee) {
            btnAddAttendee.setVisibility(View.GONE);
            btnSaveAttendees.setText(getResources().getString(R.string.back));
            tvAddAttendeeDesc.setText(getResources().getString(R.string.viewAttendee));
        }

        btnAddAttendee.setOnClickListener(view -> {
            {
                if (parentGroup.getChildCount()<5) {
                    btnAddAttendee.setEnabled(true);
                    btnAddAttendee.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_active));
                    inflateAttendeeLayout();
                }else{
                    btnAddAttendee.setEnabled(false);
                    btnAddAttendee.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_disabled));
                }
            }
        });

        btnSaveAttendees.setOnClickListener(view -> {
            if (!editAttendee){
                  finish();
            }else {
                int childCount = parentGroup.getChildCount();
                ArrayList<Attendee> attendeeList = new ArrayList<Attendee>();
                boolean allAvailable=true;
                for (int index = 0; index < childCount; index++) {
                    View child = parentGroup.getChildAt(index);
                    EditText etAttendeeName = (EditText) child.findViewById(R.id.etTraineeName);
                    EditText etAttendeeAge = (EditText) child.findViewById(R.id.etTraineeAge);
                    Spinner spinnerRelationship = (Spinner) child.findViewById(R.id.spinnerRelationship);
                    String relationShip = (String) spinnerRelationship.getSelectedItem();
                    Spinner spinnerGender = (Spinner) child.findViewById(R.id.spinnerGender);
                    String gender = (String) spinnerGender.getSelectedItem();

                    if (etAttendeeName.getText().toString().trim().equalsIgnoreCase("")
                            || etAttendeeAge.getText().toString().trim().equalsIgnoreCase("")){
                         allAvailable=false;
                    }

                    Attendee attendee = new Attendee();
                    attendee.setTraineeName(etAttendeeName.getText().toString().trim());
                    attendee.setTraineeAge(etAttendeeAge.getText().toString().trim());
                    attendee.setTraineeGender(gender);
                    attendee.setTraineeGenderId(String.valueOf(spinnerGender.getSelectedItemId()));
                    attendee.setTraineeRelationship(relationShip);
                    attendee.setTraineeRelationshipId(String.valueOf(spinnerRelationship.getSelectedItemId()));
                    attendeeList.add(attendee);
                }
                if (!allAvailable){
                    Toast.makeText(RegisterAdditionalTrainee.this,getResources().getString(R.string.mandatory_msg),Toast.LENGTH_LONG).show();
                }else {
                    // Add/Update the transaction Id
                    dl.addOrUpdateTrainees(context, farmerId, trainingRecordId, attendeeList);
                    finish();
                }
            }
        });
    }

    private void inflateAttendeeLayout(){
        View childAttendeeView = inflater.inflate(R.layout.layout_additional_trainee, containerLayout, false);
        ImageView btnDeleteAttendee = childAttendeeView.findViewById(R.id.btndeleteAttendee);

        Spinner spinnerRelationship=childAttendeeView.findViewById(R.id.spinnerRelationship);
        String[] relationships = getResources().getStringArray(R.array.Trainee_relationships);
        ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,relationships);
        spinnerRelationship.setAdapter(relationshipAdapter);

        Spinner spinnerGender=childAttendeeView.findViewById(R.id.spinnerGender);
        String[] gender = getResources().getStringArray(R.array.genders);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,gender);
        spinnerGender.setAdapter(genderAdapter);

        TextView  lblAttendee=childAttendeeView.findViewById(R.id.lblAttendee);
        int countAttendee= parentGroup.getChildCount()+1;
        lblAttendee.setText(getResources().getString(R.string.attendee)+" "+countAttendee);

        btnDeleteAttendee.setOnClickListener(view1 -> {
            parentGroup.removeView(childAttendeeView);
            if (parentGroup.getChildCount()<5){
                btnAddAttendee.setEnabled(true);
                btnAddAttendee.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_active));
            }
        });
        parentGroup.addView(childAttendeeView);
        ViewTreeObserver vto = scrollView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                scrollView.scrollTo(0, 0);
            }
        });
    }

    private void setInitialData() {
       Cursor farmerCursor= dl.getFarmerById(farmerId);
       String name = null;
        if (farmerCursor.getCount() > 0) {
            farmerCursor.moveToFirst();
            name = farmerCursor.getString(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
        }
        tvFarmerName.setText(name);

    }

    private void getIntentDetails() {
        //Get the bundle
        Bundle bundle = getIntent().getExtras();
        trainingRecordId = Integer.parseInt(bundle.getString("traineeRecordId"));
        farmerId=Long.valueOf(bundle.getString("farmerId"));
        editAttendee=bundle.getBoolean("isEdit");
    }

    private void fillTraineeRelationship() {
        Cursor traineeRelationshipCursor=dl.getTraineeRelationship(trainingRecordId, farmerId);
        int count =1;
        if (traineeRelationshipCursor.getCount()>0){
            if(traineeRelationshipCursor != null && traineeRelationshipCursor.moveToFirst()) {
                do {
                    String nameItem = traineeRelationshipCursor.getString(traineeRelationshipCursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_NAME));
                    String AgeItem = traineeRelationshipCursor.getString(traineeRelationshipCursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_AGE));
                    String relationshipItem = traineeRelationshipCursor.getString(traineeRelationshipCursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP_ID));
                    String genderItem = traineeRelationshipCursor.getString(traineeRelationshipCursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER_ID));

                    View child = inflater.inflate(R.layout.layout_additional_trainee, containerLayout, false);

                    ImageView btnDeleteAttendee = (ImageView) child.findViewById(R.id.btndeleteAttendee);
                    btnDeleteAttendee.setOnClickListener(view1 -> {
                        parentGroup.removeView(child);
                    });

                    TextView  lblAttendee=(TextView) child.findViewById(R.id.lblAttendee);
                    lblAttendee.setText(getResources().getString(R.string.attendee)+" "+count);

                    EditText etAttendeeName=(EditText) child.findViewById(R.id.etTraineeName);
                    etAttendeeName.setText(nameItem);

                    EditText etAttendeeAge=(EditText) child.findViewById(R.id.etTraineeAge);
                    etAttendeeAge.setText(AgeItem);

                    Spinner spinnerRelationship=(Spinner) child.findViewById(R.id.spinnerRelationship);
                    String[] relationships = getResources().getStringArray(R.array.Trainee_relationships);
                    ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,relationships);
                    spinnerRelationship.setAdapter(relationshipAdapter);
                    spinnerRelationship.setSelection(Integer.parseInt(relationshipItem));

                    Spinner  spinnerGender=(Spinner) child.findViewById(R.id.spinnerGender);
                    String[] gender = getResources().getStringArray(R.array.genders);
                    ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,gender);
                    spinnerGender.setAdapter(genderAdapter);
                    spinnerGender.setSelection(Integer.parseInt(genderItem));
                    if (!editAttendee) {
                        btnDeleteAttendee.setVisibility(View.GONE);
                    }

                    parentGroup.addView(child);
                    count++;

                } while (traineeRelationshipCursor.moveToNext());
            }
        }
    }

    private void initWidget() {
       containerLayout=findViewById(R.id.bottomSectionContainer);
       btnAddAttendee=findViewById(R.id.btnAddAttendee);
       btnSaveAttendees=findViewById(R.id.btnSaveAttendees);
       tvAddAttendeeDesc=findViewById(R.id.addAttendeeDesc);
       tvFarmerName=findViewById(R.id.tvFarmerName);
       scrollView=findViewById(R.id.scrollView);
       inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       parentGroup = (ViewGroup)containerLayout;
    }
}
