package com.olam.digital.ofis;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.olam.digital.ofis.model.Attendee;
import com.olam.digital.ofis.model.AttendeeRelations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<AttendeeRelations> attendeeRelationsList=null;
    private List<String> relationShipList=new ArrayList<>();
    private String[] relationShips=null;
    private boolean signatureAllowed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register_additional_trainee);
        initWidget();
        getIntentDetails();

        // get Relationships
        String locale=Utilities.getLocaleSuffix(res);
        attendeeRelationsList=dl.getTrainingRelations(this, locale);
        relationShipList.clear();
        relationShipList.add(getResources().getString(R.string.select_relationship_with_farmer));
        for (AttendeeRelations attendeeRelations : attendeeRelationsList){
            relationShipList.add(attendeeRelations.getName());
        }

        setInitialData();
        fillTraineeRelationship();
        if(parentGroup.getChildCount()==0){
            inflateAttendeeLayout();
        }
        if (!editAttendee) {
            btnAddAttendee.setVisibility(View.GONE);
            btnSaveAttendees.setText("Back");
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
                    etAttendeeName.setFilters(new InputFilter[]{new EmojiExcludeFilter()});
                    EditText etAttendeeAge = (EditText) child.findViewById(R.id.etTraineeAge);
                    Spinner spinnerRelationship = (Spinner) child.findViewById(R.id.spinnerRelationship);
                    LinearLayout signatureRelationAttendee = (LinearLayout) child.findViewById(R.id.signatureRelationAttendee);
                    ImageButton attendeeRelationSignatureDisplay = (ImageButton) child.findViewById(R.id.attendeeRelationSignatureDisplay);

                    String relationShip = (String) spinnerRelationship.getSelectedItem();


                    Spinner spinnerGender = (Spinner) child.findViewById(R.id.spinnerGender);
                    String gender = (String) spinnerGender.getSelectedItem();

                    String signatureName = null;
                    Bitmap signatureAttendee = null;
                    if (signatureAllowed && signatureRelationAttendee.getTag()!=null) {
                        signatureName = signatureRelationAttendee.getTag().toString();
                    }

                    if (etAttendeeName.getText().toString().trim().isEmpty()
                            || etAttendeeAge.getText().toString().trim().isEmpty()
                            || spinnerRelationship.getSelectedItemId() == 0
                            || (signatureAllowed && signatureName == null)) {
                        allAvailable = false;
                    }

                    Attendee attendee = new Attendee();
                    attendee.setTraineeName(etAttendeeName.getText().toString().trim());
                    attendee.setTraineeAge(etAttendeeAge.getText().toString().trim());
                    attendee.setTraineeGender(gender);
                    attendee.setTraineeGenderId(String.valueOf(spinnerGender.getSelectedItemId()));
                    attendee.setTraineeRelationship(relationShip);
                    attendee.setTraineeRelationshipId(String.valueOf(spinnerRelationship.getSelectedItemId()));
                    if (signatureAllowed && signatureName!=null) {
                        attendee.setTraineeRelationSignature(signatureName);
                        Drawable drawable = attendeeRelationSignatureDisplay.getDrawable();
                        Bitmap signBitmap = ((BitmapDrawable) drawable).getBitmap();
                        saveSignatureImage(signBitmap, signatureName);
                    }
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

    public void saveSignatureImage(Bitmap bitmap, String signatureName) {
        File directory = new File(getFilesDir(), "signatures");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File imageFile = new File(directory, signatureName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class EmojiExcludeFilter implements InputFilter {
        private String blockCharacterSet = "~#^|$%&*!'\"";
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL ||
                        (source != null && blockCharacterSet.contains(("" + source)))) {
                    return "";
                }
            }
            return null;
        }
    }

    private void inflateAttendeeLayout(){
        View childAttendeeView = inflater.inflate(R.layout.layout_additional_trainee, containerLayout, false);
        ImageView btnDeleteAttendee = childAttendeeView.findViewById(R.id.btndeleteAttendee);

        Spinner spinnerRelationship=childAttendeeView.findViewById(R.id.spinnerRelationship);

        EditText etAttendeeName = (EditText) childAttendeeView.findViewById(R.id.etTraineeName);
        etAttendeeName.setFilters(new InputFilter[]{new EmojiExcludeFilter()});

        LinearLayout traineeSignatureContainer = (LinearLayout) childAttendeeView.findViewById(R.id.traineeSignatureContainer);
        LinearLayout signatureRelationAttendee = (LinearLayout) childAttendeeView.findViewById(R.id.signatureRelationAttendee);
        ImageButton attendeeRelationSignatureDisplay = (ImageButton) childAttendeeView.findViewById(R.id.attendeeRelationSignatureDisplay);

        if (signatureAllowed) {
            traineeSignatureContainer.setVisibility(View.VISIBLE);
        } else {
            traineeSignatureContainer.setVisibility(View.GONE);
        }

        signatureRelationAttendee.setOnClickListener(v -> {
            String attendeeName = etAttendeeName.getText().toString();
            if (!attendeeName.trim().equalsIgnoreCase("")) {
                signTrainingAttendee(attendeeName, signatureRelationAttendee , attendeeRelationSignatureDisplay);
            } else {
                Toast.makeText(RegisterAdditionalTrainee.this, "Please enter the name first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Old
      //  String[] relationships = getResources().getStringArray(R.array.Trainee_relationships);

        //New
        String[] relationships = relationShipList.toArray(new String[relationShipList.size()]);

        ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,relationships);
        relationshipAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerRelationship.setAdapter(relationshipAdapter);

        Spinner spinnerGender=childAttendeeView.findViewById(R.id.spinnerGender);
        String[] gender = getResources().getStringArray(R.array.genders);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,gender);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
        // Disabling scroll option - OFIS-1172
//        ViewTreeObserver vto = scrollView.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                scrollView.scrollTo(0, 0);
//            }
//        });
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
        signatureAllowed = bundle.getBoolean(Utilities.ATTENDEE_SIGNATURE);
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
                    String attendeeRelationSignature = traineeRelationshipCursor.getString(traineeRelationshipCursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATION_SIGNATURE));

                    View child = inflater.inflate(R.layout.layout_additional_trainee, containerLayout, false);

                    ImageView btnDeleteAttendee = (ImageView) child.findViewById(R.id.btndeleteAttendee);
                    btnDeleteAttendee.setOnClickListener(view1 -> {
                        parentGroup.removeView(child);
                    });

                    TextView  lblAttendee=(TextView) child.findViewById(R.id.lblAttendee);
                    lblAttendee.setText(getResources().getString(R.string.attendee)+" "+count);

                    EditText etAttendeeName=(EditText) child.findViewById(R.id.etTraineeName);
                    etAttendeeName.setFilters(new InputFilter[]{new EmojiExcludeFilter()});
                    etAttendeeName.setText(nameItem);

                    EditText etAttendeeAge=(EditText) child.findViewById(R.id.etTraineeAge);
                    etAttendeeAge.setText(AgeItem);

                    Spinner spinnerRelationship=(Spinner) child.findViewById(R.id.spinnerRelationship);

                    //Old
                   // String[] relationships = getResources().getStringArray(R.array.Trainee_relationships);

                    //New
                    String[] relationships = relationShipList.toArray(new String[relationShipList.size()]);

                    //frank
                    ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,relationships);
                    relationshipAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerRelationship.setAdapter(relationshipAdapter);
                    spinnerRelationship.setSelection(Integer.parseInt(relationshipItem));

                    Spinner  spinnerGender=(Spinner) child.findViewById(R.id.spinnerGender);
                    String[] gender = getResources().getStringArray(R.array.genders);
                    ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,gender);
                    genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerGender.setAdapter(genderAdapter);
                    spinnerGender.setSelection(Integer.parseInt(genderItem));

                    LinearLayout traineeSignatureContainer = (LinearLayout) child.findViewById(R.id.traineeSignatureContainer);
                    LinearLayout signatureRelationAttendee = (LinearLayout) child.findViewById(R.id.signatureRelationAttendee);
                    ImageButton attendeeRelationSignatureDisplay = (ImageButton) child.findViewById(R.id.attendeeRelationSignatureDisplay);

                    if (signatureAllowed) {
                        int index = parentGroup.getChildCount();
                        traineeSignatureContainer.setVisibility(View.VISIBLE);
                        if (attendeeRelationSignature != null) {
                            File directory = new File(getFilesDir(), "signatures");
                            File imageFile = new File(directory, attendeeRelationSignature);
                            if (imageFile.exists()) {
                                Bitmap signatureBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                attendeeRelationSignatureDisplay.setImageBitmap(signatureBitmap);
                                signatureRelationAttendee.setTag(attendeeRelationSignature);

                            }
                        }
                        signatureRelationAttendee.setOnClickListener(v -> {
                            if (editAttendee) {
                                String attendeeName = etAttendeeName.getText().toString();
                                if (!attendeeName.trim().equalsIgnoreCase("")) {
                                    signTrainingAttendee(attendeeName, signatureRelationAttendee, attendeeRelationSignatureDisplay);
                                } else {
                                    Toast.makeText(RegisterAdditionalTrainee.this, getResources().getString(R.string.enter_attendee_relation_name), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        traineeSignatureContainer.setVisibility(View.GONE);
                    }

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


    public void signTrainingAttendee(String attendeeName, LinearLayout signatureRelationAttendee, ImageButton attendeeRelationSignatureDisplay) {
        final boolean[] isSigned = {false};
        final Dialog signatureDialog = new Dialog(RegisterAdditionalTrainee.this, android.R.style.Theme_Material_Dialog_Alert);
        signatureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        SignaturePad mSignaturePad = signatureDialog.findViewById(R.id.signature_pad);

        Button bt_clear = (Button)signatureDialog.findViewById(R.id.imgv_clear_signature);
        Button bt_confirm = (Button)signatureDialog.findViewById(R.id.btn_add_signature);
        TextView tv_attendeeName = (TextView) signatureDialog.findViewById(R.id.tv_attendeeName);
        tv_attendeeName.setVisibility(View.VISIBLE);
        tv_attendeeName.setText(attendeeName);

        bt_confirm.setEnabled(true);
        bt_clear.setEnabled(true);

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignaturePad.clear();
            }
        });

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
            }

            @Override
            public void onSigned() {
                isSigned[0] = true;
            }

            @Override
            public void onClear() {
                isSigned[0] = false;
            }
        });

        signatureDialog.findViewById(R.id.btn_cancel_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureDialog.dismiss();
            }
        });

        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isSigned[0]) {
                        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                        String signatureName = farmerId + "_" + System.currentTimeMillis() + ".png";
                        attendeeRelationSignatureDisplay.setImageBitmap(signatureBitmap);
                        signatureRelationAttendee.setTag(signatureName);
                        signatureDialog.dismiss();

                    } else {
                        Toast.makeText(RegisterAdditionalTrainee.this, "No signature available...", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        signatureDialog.show();
    }

    public static void removeAndReindexSignature(HashMap<Integer, Pair<String, Bitmap>> map, int keyToRemove) {
        if (map.containsKey(keyToRemove)) {
            map.remove(keyToRemove);
            HashMap<Integer, Pair<String, Bitmap>> tempMap = new HashMap<>();

            for (Map.Entry<Integer, Pair<String, Bitmap>> entry : map.entrySet()) {
                int newKey = entry.getKey() > keyToRemove ? entry.getKey() - 1 : entry.getKey();
                tempMap.put(newKey, entry.getValue());
            }

            map.clear();
            map.putAll(tempMap);
        } else {
            HashMap<Integer, Pair<String, Bitmap>> tempMap = new HashMap<>();

            for (Map.Entry<Integer, Pair<String, Bitmap>> entry : map.entrySet()) {
                int newKey = entry.getKey() > keyToRemove ? entry.getKey() - 1 : entry.getKey();
                tempMap.put(newKey, entry.getValue());
            }

            map.clear();
            map.putAll(tempMap);
        }
    }
}
