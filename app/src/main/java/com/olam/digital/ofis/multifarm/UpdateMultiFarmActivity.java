package com.olam.digital.ofis.multifarm;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.xw.repo.BubbleSeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UpdateMultiFarmActivity extends BaseActivity {

    // Components
    private LinearLayout farmInformationLayout;
    private RadioGroup farmRadioGroup;
    private RadioButton newFarmRadioButton;
    private RadioButton existingFarmRadioButton;
    private EditText farmNameEdittext;
    private EditText farmCodeEdittext;
    private LinearLayout farmStakeholderLayout;
    private LinearLayout existingFarmLayout,newFarmLayout;
    private TextView existingFarmTV;
    private Button saveButton;

    // initialze variables
    private Long farmerID;
    private List<MultiFarmRoleDTO> multiFarmRoleDTOList= new ArrayList<MultiFarmRoleDTO>();
    private List<String> rolesList=new ArrayList<>();
    private String[] roles=null;
    private String blockCharacterSet = "~#^|$%&*!'\"";
    private LayoutInflater inflater;
    Context mContext=null;
    private String farmerName;

    private  boolean editMode=false;

    GPSListener gpsListener = null;
    private Location mCurrentLocation = null;
    private GPSTracker gpsTracker = null;
    private boolean isExistingFarm =false;

    private long editFarmerId; private long editFarmId;
    private boolean newFarmEditMode=false;
    MultiFarmDTO existingStakeHolder=null;
    private boolean editIsExistFarm=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_multifarm);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext=this;
        initWidgets();

        Intent intent = getIntent();

        try {
            editMode = intent.getBooleanExtra("editMode", false);
            if(editMode){
                editFarmerId = Long.valueOf(getIntent().getExtras().getString("farmerId"));
                editFarmId = Long.valueOf(getIntent().getExtras().getString("farmId"));
                farmerName=dl.getFarmerNamefromDb(editFarmerId);
            }else{
                farmerID=getIntent().getExtras().getLong("farmerId");
                farmerName=getIntent().getExtras().getString("olamfarmername");
            }
        }catch (Exception e){}



        gpsListener = location -> {
            if (location != null) {
                mCurrentLocation = location;
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener);
        gpsTracker.getDeviceLocation();

        multiFarmRoleDTOList=dl.getMultiFarmRoles(Utilities.getLocaleSuffix(res));
        rolesList.clear();
        for (MultiFarmRoleDTO multiFarmRoleDTO : multiFarmRoleDTOList){
            rolesList.add(multiFarmRoleDTO.getName());
        }
        bindMultiFarm();

        saveButton.setOnClickListener(v -> {
            if(!farmNameEdittext.getText().toString().trim().equalsIgnoreCase("") &&
               !farmCodeEdittext.getText().toString().trim().equalsIgnoreCase("")){
                if (farmStakeholderLayout.getChildCount()==0){
                    Toast.makeText(UpdateMultiFarmActivity.this,res.getString(R.string.stakeholder_validation),Toast.LENGTH_LONG).show();
                     return;
                }
            if (isAllRoleFarmerFilled()) {
                if (editMode){

                     long newId=dl.updateMultiFarmInfo(getAllMultiFarmList1(editFarmerId), editFarmId,isExistingFarm,editIsExistFarm);
                     ContentValues networkQueueValues = new ContentValues();
                     networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, editFarmerId);
                     networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, newId);
                     dl.updateNetworkQueueMultiFarmEntry(networkQueueValues,editFarmId,editFarmerId);

                }else{
                     long newId= dl.insertMultiFarmInfo(getAllMultiFarmList1(farmerID), farmerID,isExistingFarm);
                     dl.insertNetworkMultifarmUpdateQueueEntry(farmerID,farmerName,newId);
                }

            }else{
                Toast.makeText(UpdateMultiFarmActivity.this,res
                        .getString(R.string.fill_stakeholder_name_role),Toast.LENGTH_LONG).show();
                return;
            }
            }else{
                Toast.makeText(UpdateMultiFarmActivity.this,res
                        .getString(R.string.fill_stakeholder_farm_code_name),Toast.LENGTH_LONG).show();
                return;
            }
            finish();
        });
    }

    private void initWidgets() {
        farmInformationLayout = findViewById(R.id.farmInformationLayout);
        farmRadioGroup = findViewById(R.id.farmRadioGroup);
        newFarmRadioButton = findViewById(R.id.newFarmRadioButton);
        existingFarmRadioButton = findViewById(R.id.existingFarmRadioButton);
        // New
        newFarmLayout = findViewById(R.id.newFarmLayout);
        farmNameEdittext = findViewById(R.id.farmNameEdittext);
        farmCodeEdittext = findViewById(R.id.farmCodeEdittext);

        farmStakeholderLayout = findViewById(R.id.farmStakeholderLayout);
        saveButton = findViewById(R.id.saveButton);

    }

            private void bindMultiFarm() {
                inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                if (editMode) {

                    MultiFarmDTO existingStakeHolder = dl.getMultiFarmStakeHolderInfoByFarmId(String.valueOf(editFarmId));
                    JSONArray multifarmArray = existingStakeHolder.getStakeholdersInfo();
                    String[] roles = rolesList.toArray(new String[rolesList.size()]);
                    boolean position = true;

//                    if (existingStakeHolder.getFarmId() != null){
//                        if(!existingStakeHolder.getFarmId().equalsIgnoreCase("")){
//                            newFarmEditMode=false;
//                            existingFarmRadioButton.setChecked(true);
//                        }else{
//                            newFarmEditMode=true;
//                            newFarmRadioButton.setChecked(true);
//                        }
//                    }else{
//                        newFarmEditMode=true;
//                        newFarmRadioButton.setChecked(true);
//                    }

                    if (existingStakeHolder!=null) {
                        if (existingStakeHolder.isExistingFarm()) {
                            editIsExistFarm=true;
                            isExistingFarm = true;
                            existingFarmRadioButton.setChecked(true);
                        } else {
                            editIsExistFarm=false;
                            isExistingFarm = false;
                            newFarmRadioButton.setChecked(true);
                        }
                    }

                    if (multifarmArray!=null) {
                        if (multifarmArray.length() > 0) {
                            int length = multifarmArray.length();
                            for (int j = 0; j < length; j++) {
                                try {
                                    String farmname = multifarmArray.getJSONObject(j).get("farmerId").toString();
                                    String roleId = multifarmArray.getJSONObject(j).get("farmerRoleId").toString();
                                    farmCodeEdittext.setTag(editFarmId);

                                    // Update ID to avoid duplicate Register
                                    String id=null;
                                    if (multifarmArray.getJSONObject(j).has("id")) {
                                        id = (multifarmArray.getJSONObject(j).getString("id"));
                                    }

                                    LinearLayout farmStakeholderRow = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
                                    farmStakeholderRow.setTag(id);

                                    // Widgets
                                    TextView farmerNameEdittext = farmStakeholderRow.findViewById(R.id.farmFarmerTV);
                                    Spinner farmRoleSpinner = farmStakeholderRow.findViewById(R.id.farmerRoleSpinner);
                                    ImageView farmRoleDelete = farmStakeholderRow.findViewById(R.id.farmRoleDelete);

                                    // Spinner value setting part
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateMultiFarmActivity.this, R.layout.spinner_item, roles);
                                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                    farmRoleSpinner.setAdapter(adapter);


                                    //   String roleId= existingStakeHolder.getString(existingStakeHolder.getColumnIndex(OFISDBContract.FarmStakeHolderInformation.COLUMN_NAME_STAKE_HOLDER_ROLE_ID));
                                    farmRoleSpinner.setSelection(Integer.parseInt(roleId) - 1);

                                    // Set Tag in edit Mode
                                    farmRoleSpinner.setTag(roleId);

                                    String farmerId = multifarmArray.getJSONObject(j).get("farmerId").toString();

                                    String farmerNameFromDb = dl.getFarmerNamefromDb(Long.valueOf(farmerId));
                                    farmerNameEdittext.setText(farmerNameFromDb);

                                    farmerNameEdittext.setTag(farmerId);


                                    // Should cset the farmer Name - New field incorporate
                                    farmNameEdittext.setText(multifarmArray.getJSONObject(j).get("farmName").toString());
                                    farmCodeEdittext.setText(multifarmArray.getJSONObject(j).get("farmCode").toString());

                                    // disable the farm delete in first record
                                    if (position) {
                                      //  farmRoleDelete.setVisibility(View.GONE);
                                        position = false;
                                    }

                                    farmStakeholderLayout.addView(farmStakeholderRow);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                } else {

                    // Existing
                     existingStakeHolder=dl.getMultiFarmStakeHolderInfoByFarmerId(String.valueOf(farmerID));

                    if (existingStakeHolder!=null) {
                        if (existingStakeHolder.isExistingFarm()) {
                            isExistingFarm = true;
                        } else {
                            isExistingFarm = false;
                        }
                    }


                    JSONArray multifarmArray = existingStakeHolder.getStakeholdersInfo();
                    String[] roles = rolesList.toArray(new String[rolesList.size()]);
                    if(multifarmArray!=null) {
                        if (multifarmArray.length() > 0) {

                            int length = multifarmArray.length();
                            List<View> viewList = new ArrayList<View>();
                            for (int j = 0; j < length; j++) {
                                try {
                                    String roleId = multifarmArray.getJSONObject(j).get("farmerRoleId").toString();

                                    String id=null;
                                    if (multifarmArray.getJSONObject(j).has("id")) {
                                        id = (multifarmArray.getJSONObject(j).getString("id"));
                                    }

                                    LinearLayout farmStakeholderRow = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
                                    farmStakeholderRow.setTag(id);


                                    // Widgets
                                    TextView farmerNameEdittext = farmStakeholderRow.findViewById(R.id.farmFarmerTV);
                                    Spinner farmRoleSpinner = farmStakeholderRow.findViewById(R.id.farmerRoleSpinner);
                                    ImageView farmRoleDelete = farmStakeholderRow.findViewById(R.id.farmRoleDelete);

                                    // Spinner value setting part
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateMultiFarmActivity.this, R.layout.spinner_item, roles);
                                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                    farmRoleSpinner.setAdapter(adapter);


                                    //   String roleId= existingStakeHolder.getString(existingStakeHolder.getColumnIndex(OFISDBContract.FarmStakeHolderInformation.COLUMN_NAME_STAKE_HOLDER_ROLE_ID));
                                    farmRoleSpinner.setSelection(Integer.parseInt(roleId) - 1);

                                    // Set Tag in edit Mode
                                    farmRoleSpinner.setTag(roleId);
                                    String farmerId = multifarmArray.getJSONObject(j).get("farmerId").toString();

                                    String farmerNameFromDb = dl.getFarmerNamefromDb(Long.valueOf(farmerId));
                                    farmerNameEdittext.setText(farmerNameFromDb);
                                    farmerNameEdittext.setTag(farmerId);


                                    // Should cset the farmer Name - New field incorporate
                                    farmNameEdittext.setText(multifarmArray.getJSONObject(j).get("farmName").toString());
                                    farmCodeEdittext.setText(multifarmArray.getJSONObject(j).get("farmCode").toString());
                                    farmCodeEdittext.setTag(multifarmArray.getJSONObject(j).get("farmsId").toString());


                                    if (farmerId.equalsIgnoreCase(String.valueOf(farmerID))) {
                                      //  farmRoleDelete.setVisibility(View.GONE);
                                        bindFarmRoleDelete(farmStakeholderRow.findViewById(R.id.farmRoleDelete));
                                        farmStakeholderLayout.addView(farmStakeholderRow);
                                    } else {
                                        bindFarmRoleDelete(farmStakeholderRow.findViewById(R.id.farmRoleDelete));
                                        viewList.add(farmStakeholderRow);
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            for (View v : viewList) {
                                farmStakeholderLayout.addView(v);
                            }
//                            if (farmStakeholderLayout.getChildCount()>0){
//                                existFarmDefault=true;
//                            }
                        }


                    }else{

                        // Should allow the delinking . So disabling this scenerio

//                        LinearLayout farmRoleInclude = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
//                        farmStakeholderLayout.addView(farmRoleInclude);
//
//                        Spinner farmRoleSpinner = farmRoleInclude.findViewById(R.id.farmerRoleSpinner);
//                        bindFarmerRoles(farmRoleSpinner);
//
//                        TextView farmFarmerEditText = farmRoleInclude.findViewById(R.id.farmFarmerTV);
//                        farmFarmerEditText.setText(farmerName);
//                        farmFarmerEditText.setTag(farmerID);
//
//
//                        ImageView farmRoleDelete = farmRoleInclude.findViewById(R.id.farmRoleDelete);
//                        farmRoleDelete.setVisibility(View.GONE);
                    }
                }

                existingFarmRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        isExistingFarm=true;
                        List<MultiFarmDTO> existingFarmList = new ArrayList<>();
                        showPopupFarm(existingFarmList);
                        farmStakeholderLayout.removeAllViews();
                    }
                });


                newFarmRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        //1989
                        isExistingFarm =false;
                        farmNameEdittext.setEnabled(true);
                        farmNameEdittext.setText("");
                        farmCodeEdittext.setText("");
                        farmStakeholderLayout.removeAllViews();

//                        if (newFarmEditMode){
//                            SetUpEditedData();
//                        }
                    }
                });


                // Add FArmer
                LinearLayout addRoleFarmerLayout = findViewById(R.id.addFarmerRoleLayout);
                addRoleFarmerLayout.setOnClickListener(v -> {
                    LinearLayout farmRoleInclude = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);

//                    if (farmStakeholderLayout.getChildCount() == 0) {
//                        // Default Farmer
//                        //NotNeeded
//                        String defaultFarmerName="";
//                        if(editMode){
//                             defaultFarmerName = dl.getFarmerNamefromDb(editFarmerId).trim();
//                        }else{
//                             defaultFarmerName = dl.getFarmerNamefromDb(farmerID).trim();
//                        }
//                        if (!defaultFarmerName.equalsIgnoreCase("") && defaultFarmerName != null) {
//                            TextView farmerNameET = farmRoleInclude.findViewById(R.id.farmFarmerTV);
//                            farmerNameET.setText(defaultFarmerName);
//                            if (editMode){
//                                farmerNameET.setTag(String.valueOf(editFarmerId));
//                            }else{
//                                farmerNameET.setTag(String.valueOf(farmerID));
//                            }
//                        } else {
//                            // This use case will never get called
//                            Toast.makeText(UpdateMultiFarmActivity.this, "name is empty", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        bindFarmRoleDelete(farmRoleInclude.findViewById(R.id.farmRoleDelete));
//                    } else {
//                        bindFarmRoleDelete(farmRoleInclude.findViewById(R.id.farmRoleDelete));
//                    }

                    farmStakeholderLayout.addView(farmRoleInclude);
                    bindFarmerRoles(farmRoleInclude.findViewById(R.id.farmerRoleSpinner));
                    bindFarmers(farmRoleInclude.findViewById(R.id.farmFarmerTV));
                    bindFarmRoleDelete(farmRoleInclude.findViewById(R.id.farmRoleDelete));
                });

                farmNameEdittext.addTextChangedListener(new TextWatcher() {

                    public void afterTextChanged(Editable s) {

                        // you can call or do what you want with your EditText here
                        String farmCodeContent=farmNameEdittext.getText().toString().trim();
                        if (farmCodeContent.equalsIgnoreCase("")){
                            farmCodeEdittext.setText("");
                        }else{
                            farmCodeEdittext.setText(farmCodeContent+"-"+ String.format("%04d", new Random().nextInt(10000)));
                        }
                         farmCodeEdittext.setEnabled(false);
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                });

            }

    private void SetUpEditedData(){
        MultiFarmDTO existingStakeHolder = dl.getMultiFarmBySelfId(editFarmerId);
        JSONArray multifarmArray = existingStakeHolder.getStakeholdersInfo();
        String[] roles = rolesList.toArray(new String[rolesList.size()]);
        boolean position = true;

//        if (existingStakeHolder.getFarmId() != null){
//            newFarmEditMode=false;
//        }else{
//            newFarmEditMode=true;
//        }

        if (multifarmArray!=null) {
            if (multifarmArray.length() > 0) {
                int length = multifarmArray.length();
                for (int j = 0; j < length; j++) {
                    try {
                        String farmname = multifarmArray.getJSONObject(j).get("farmerId").toString();
                        String roleId = multifarmArray.getJSONObject(j).get("farmerRoleId").toString();
                        String id=null;
                        if (multifarmArray.getJSONObject(j).has("id")) {
                            id = (multifarmArray.getJSONObject(j).getString("id"));
                        }

                        LinearLayout farmStakeholderRow = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
                        farmStakeholderRow.setTag(id);


                        // Widgets
                        TextView farmerNameEdittext =farmStakeholderRow.findViewById(R.id.farmFarmerTV);
                        Spinner farmRoleSpinner = farmStakeholderRow.findViewById(R.id.farmerRoleSpinner);
                        ImageView farmRoleDelete = farmStakeholderRow.findViewById(R.id.farmRoleDelete);

                        // Spinner value setting part
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateMultiFarmActivity.this, R.layout.spinner_item, roles);
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        farmRoleSpinner.setAdapter(adapter);


                        //   String roleId= existingStakeHolder.getString(existingStakeHolder.getColumnIndex(OFISDBContract.FarmStakeHolderInformation.COLUMN_NAME_STAKE_HOLDER_ROLE_ID));
                        farmRoleSpinner.setSelection(Integer.parseInt(roleId) - 1);

                        // Set Tag in edit Mode
                        farmRoleSpinner.setTag(roleId);

                        String farmerId = multifarmArray.getJSONObject(j).get("farmerId").toString();

                        String farmerNameFromDb = dl.getFarmerNamefromDb(Long.valueOf(farmerId));
                        farmerNameEdittext.setText(farmerNameFromDb);

                        farmerNameEdittext.setTag(farmerId);


                        // Should cset the farmer Name - New field incorporate
                        farmNameEdittext.setText(multifarmArray.getJSONObject(j).get("farmName").toString());
                        farmCodeEdittext.setText(multifarmArray.getJSONObject(j).get("farmCode").toString());
                        farmCodeEdittext.setTag(multifarmArray.getJSONObject(j).get("farmsId").toString());

                        // disable the farm delete in first record
                        if (position) {
                            //  farmRoleDelete.setVisibility(View.GONE);
                            position = false;
                        }

                        farmStakeholderLayout.addView(farmStakeholderRow);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void bindFarmers(TextView farmFarmerEditText) {
                farmFarmerEditText.setOnClickListener(v -> {
                    List<MultifarmFarmerDTO> mfFarmerList = new ArrayList<>();
                    showPopupFarmers(mfFarmerList, farmFarmerEditText);
                });
            }

            private void bindExistingFarm(TextView existingFarmTV) {
                existingFarmTV.setOnClickListener(v -> {
                    List<MultiFarmDTO> existingFarmList = new ArrayList<>();
                    showPopupFarm(existingFarmList);
                });
            }

            private void bindFarmerRoles(Spinner roleSpinner) {

                String[] roles = rolesList.toArray(new String[rolesList.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateMultiFarmActivity.this, R.layout.spinner_item, roles);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                roleSpinner.setAdapter(adapter);

                roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        MultiFarmRoleDTO multiFarmRoleDTO = multiFarmRoleDTOList.get(position);
                        roleSpinner.setTag(multiFarmRoleDTO.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }

            private void bindFarmRoleDelete(ImageView trashbutton) {
                trashbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showConfirmDelink(v);
                    }
                });
            }

    void showConfirmDelink(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        TextView tvTitle= dialogView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.INVISIBLE);
      //  tvTitle.setText(R.string.gps_manager_msg);
        tvMsg.setText(R.string.delink_confirmation);
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();

        AlertDialog finalAlert = alert;
        btnNo.setOnClickListener(view -> finalAlert.dismiss());
        btnYes.setOnClickListener(view -> {
            finalAlert.dismiss();
            LinearLayout numberrow = (LinearLayout) v.getParent();
            ((ViewGroup) numberrow.getParent()).removeView(numberrow);
        });
        alert.show();
    }

    public void showPopupFarmers(List<MultifarmFarmerDTO> mfFarmerList, TextView farmerEditText) {

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.activity_multifarm_findfarmer, null);
                FindMultiFarmFarmerAdapter findMultiFarmFarmerAdapter;
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(findViewById(R.id.farmInformationLayout), Gravity.CENTER, 0, 0);
                dimBehind(popupWindow);

                final Dialog farmerleadDialog = new Dialog(UpdateMultiFarmActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                farmerleadDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                farmerleadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                farmerleadDialog.setContentView(R.layout.activity_multifarm_findfarmer);

                EditText searchfl = popupView.findViewById(R.id.findFLSearchBox);
                RecyclerView multiFarmFarmerRecyclerView = popupView.findViewById(R.id.multiFarmFarmerReyclerView);

                ArrayList<Long> tempFarmerList=getTempFarmerList();

                mfFarmerList = dl.getfarmersList(tempFarmerList);
                findMultiFarmFarmerAdapter = new FindMultiFarmFarmerAdapter(mfFarmerList, mContext);
                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                multiFarmFarmerRecyclerView.setLayoutManager(reLayoutManager);
                // Newly Added
                DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
                itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_seperator_divider));
                multiFarmFarmerRecyclerView.addItemDecoration(itemDecoration);

                multiFarmFarmerRecyclerView.setItemAnimator(new DefaultItemAnimator());
                ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
                multiFarmFarmerRecyclerView.setAdapter(findMultiFarmFarmerAdapter);
                multiFarmFarmerRecyclerView.scrollToPosition(findMultiFarmFarmerAdapter.getItemCount() - 1);
                searchfl.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                searchfl.setFilters(new InputFilter[]{flFilter});

                List<MultifarmFarmerDTO> finalMfFarmerList = mfFarmerList;
                searchfl.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence constraint, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable constraint) {
                        ArrayList<MultifarmFarmerDTO> filteredlist = new ArrayList<>();
                        for (MultifarmFarmerDTO multifarmFarmerDTO : finalMfFarmerList) {
                            if (multifarmFarmerDTO.getFarmerName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                filteredlist.add(multifarmFarmerDTO);
                            }
                        }
                        if (filteredlist.isEmpty()) {
                            filteredlist.clear();
                            Toast.makeText(UpdateMultiFarmActivity.this, res.getString(R.string.no_match_found), Toast.LENGTH_SHORT).show();
                            findMultiFarmFarmerAdapter.filterList(filteredlist);
                        } else {
                            findMultiFarmFarmerAdapter.filterList(filteredlist);
                        }
                    }
                });
                popupView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                findMultiFarmFarmerAdapter.setOnItemMultifarmFarmerListener(new OnItemMultifarmFarmerListener() {
                    @Override
                    public void onMultiFarmFarmerClick(View view, int position, MultifarmFarmerDTO multifarmFarmerDTO) {
                        farmerEditText.setText(multifarmFarmerDTO.getFarmerName());
                        farmerEditText.setTag(multifarmFarmerDTO.getFarmerId());
                        popupWindow.dismiss();
                    }
                });
            }



    // multifarm farm - progress
            public void showPopupFarm(List<MultiFarmDTO> existingFarmList) {

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.acivity_multifarm_farm, null);
                FindMultiFarmFarmAdapter findMultiFarmFarmAdapter = null;
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(findViewById(R.id.farmInformationLayout), Gravity.CENTER, 0, 0);
                dimBehind(popupWindow);

                final Dialog farmerleadDialog = new Dialog(UpdateMultiFarmActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                farmerleadDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                farmerleadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                farmerleadDialog.setContentView(R.layout.activity_multifarm_findfarmer);

                EditText searchfl = popupView.findViewById(R.id.existingFarmTV);
                RecyclerView multiFarmFarmReyclerView = popupView.findViewById(R.id.multiFarmFarmReyclerView);
                existingFarmList = dl.getfarmList();
                // Bubble Seekbar
                BubbleSeekBar bubbleSeekBar = popupView.findViewById(R.id.seekbarKM);
                List<MultiFarmDTO> finalExistingFarmList = existingFarmList;
                findMultiFarmFarmAdapter = new FindMultiFarmFarmAdapter(existingFarmList, mContext);
                FindMultiFarmFarmAdapter finalFindMultiFarmFarmAdapter = findMultiFarmFarmAdapter;

                Button searchFarmLocationBtn=popupView.findViewById(R.id.searchFarmLocationBtn);
                final int[] progressFinal = {0};
                searchFarmLocationBtn.setOnClickListener(v -> {
                    if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                        gpsTracker.getDeviceLocation();
                        return;
                    }
                    if (mCurrentLocation!=null) {
                        ArrayList<MultiFarmDTO> multiFarmSeekbarDTOList = new DistanceCalculation(UpdateMultiFarmActivity.this, finalExistingFarmList, progressFinal[0]).calculate(mCurrentLocation);
                        finalFindMultiFarmFarmAdapter.filterList(multiFarmSeekbarDTOList);
                    }else{
                        Toast.makeText(UpdateMultiFarmActivity.this, "Please enable the device Location",Toast.LENGTH_LONG).show();
                    }

                });

                bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                    }

                    @Override
                    public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                    }

                    @Override
                    public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

                        progressFinal[0] =progress;
                    }
                });


                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                multiFarmFarmReyclerView.setLayoutManager(reLayoutManager);
                // Newly Added
                DividerItemDecoration itemDecoration = new DividerItemDecoration(UpdateMultiFarmActivity.this, DividerItemDecoration.VERTICAL);
                itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_seperator_divider));
                multiFarmFarmReyclerView.addItemDecoration(itemDecoration);

                multiFarmFarmReyclerView.setItemAnimator(new DefaultItemAnimator());
                ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
                multiFarmFarmReyclerView.setAdapter(findMultiFarmFarmAdapter);
                multiFarmFarmReyclerView.scrollToPosition(findMultiFarmFarmAdapter.getItemCount() - 1);
                searchfl.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                searchfl.setFilters(new InputFilter[]{flFilter});

                List<MultiFarmDTO> finalMfFarmList = existingFarmList;
                FindMultiFarmFarmAdapter finalFindMultiFarmFarmAdapter1 = findMultiFarmFarmAdapter;
                searchfl.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence constraint, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable constraint) {
                        ArrayList<MultiFarmDTO> filteredlist = new ArrayList<>();
                        for (MultiFarmDTO multiFarmDTO : finalMfFarmList) {
                            if (multiFarmDTO.getFarmName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                filteredlist.add(multiFarmDTO);
                            }
                        }
                        if (filteredlist.isEmpty()) {
                            filteredlist.clear();
                            Toast.makeText(UpdateMultiFarmActivity.this, res.getString(R.string.no_match_found), Toast.LENGTH_SHORT).show();
                            finalFindMultiFarmFarmAdapter1.filterList(filteredlist);
                        } else {
                            finalFindMultiFarmFarmAdapter1.filterList(filteredlist);
                        }
                    }
                });
                popupView.findViewById(R.id.cancel_btn).setOnClickListener(v -> {
                    if (!editMode) {
                        newFarmRadioButton.setChecked(true);
                        farmNameEdittext.setText("");
                        farmCodeEdittext.setText("");
                        popupWindow.dismiss();
                    }
                });

                findMultiFarmFarmAdapter.setOnItemMultifarmFarmListener(new OnItemMultiFarmFarmListener() {
                    @Override
                    public void onMultiFarmFarmClick(View view, int position, MultiFarmDTO multiFarmDTO) {
                        farmNameEdittext.setText(multiFarmDTO.getFarmName());
                        farmCodeEdittext.setText(multiFarmDTO.getFarmCode());
                        farmCodeEdittext.setTag(multiFarmDTO.getFarmId());
                        farmNameEdittext.setEnabled(false);
                        farmCodeEdittext.setEnabled(false);
                        setupMultiFarmStakeholders(multiFarmDTO);
                        // farmerEditText.setTag(multifarmFarmerDTO.getFarmerId());
                        popupWindow.dismiss();
                    }
                });

            }

            private InputFilter flFilter = new InputFilter() {

                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                    if (source != null && blockCharacterSet.contains(("" + source))) {
                        return "";
                    }
                    for (int i = start; i < end; i++) {
                        int type = Character.getType(source.charAt(i));
                        if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                            return "";
                        }
                    }
                    return null;
                }
            };

            public void dimBehind(PopupWindow popupWindow) {
                View container;
                if (popupWindow.getBackground() == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        container = (View) popupWindow.getContentView().getParent();
                    } else {
                        container = popupWindow.getContentView();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        container = (View) popupWindow.getContentView().getParent().getParent();
                    } else {
                        container = (View) popupWindow.getContentView().getParent();
                    }
                }
                Context context = popupWindow.getContentView().getContext();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.7f;
                wm.updateViewLayout(container, p);
            }



    private void setupMultiFarmStakeholders(MultiFarmDTO multiFarmDTO) {
                // Collect Overall Data
        JSONArray multifarmArray=dl.getOverallStakeholdersByFarmId(multiFarmDTO.getFarmId());
       // JSONArray multifarmArray = multiFarmDTO.getStakeholdersInfo();
        String[] roles = rolesList.toArray(new String[rolesList.size()]);

        LinearLayout farmRoleInclude = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
        if (farmStakeholderLayout.getChildCount() == 0) {
            // Default Farmer
            //NotNeeded

            //--- Removed s

//            String defaultFarmerName="";
//            if(editMode){
//                defaultFarmerName = dl.getFarmerNamefromDb(editFarmerId).trim();
//            }else{
//                defaultFarmerName = dl.getFarmerNamefromDb(farmerID).trim();
//            }
//            if (!defaultFarmerName.equalsIgnoreCase("") && defaultFarmerName != null) {
//                TextView farmerNameET = farmRoleInclude.findViewById(R.id.farmFarmerTV);
//                farmerNameET.setText(defaultFarmerName);
//                if (editMode){
//                    farmerNameET.setTag(String.valueOf(editFarmerId));
//                }else{
//                    farmerNameET.setTag(String.valueOf(farmerID));
//                }
//
//            } else {
//                Toast.makeText(UpdateMultiFarmActivity.this, "name is empty", Toast.LENGTH_LONG).show();
//                return;
//            }
//            bindFarmerRoles(farmRoleInclude.findViewById(R.id.farmerRoleSpinner));
//            bindFarmRoleDelete(farmRoleInclude.findViewById(R.id.farmRoleDelete));
//
//            farmStakeholderLayout.addView(farmRoleInclude);

            //--- Removed

        }

        if(multifarmArray==null){
            return;
        }

        if (multifarmArray.length() > 0) {
            int length = multifarmArray.length();
            for (int j = 0; j < length; j++) {
                try {
                    String farmname = multifarmArray.getJSONObject(j).get("farmerId").toString();
                    String roleId = multifarmArray.getJSONObject(j).get("farmerRoleId").toString();
                    String id=null;
                    if (multifarmArray.getJSONObject(j).has("id")) {
                         id = (multifarmArray.getJSONObject(j).getString("id"));
                    }
                    LinearLayout farmStakeholderRow = (LinearLayout) inflater.inflate(R.layout.farm_role_entry, farmStakeholderLayout, false);
                    farmStakeholderRow.setTag(id);

                    // Widgets
                    TextView farmerNameEdittext = farmStakeholderRow.findViewById(R.id.farmFarmerTV);
                    Spinner farmRoleSpinner = farmStakeholderRow.findViewById(R.id.farmerRoleSpinner);
                    ImageView farmRoleDelete = farmStakeholderRow.findViewById(R.id.farmRoleDelete);

                    // Spinner value setting part
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateMultiFarmActivity.this, R.layout.spinner_item, roles);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    farmRoleSpinner.setAdapter(adapter);

                    bindFarmers(farmerNameEdittext);

                    String farmerPId = multifarmArray.getJSONObject(j).get("farmerId").toString();

                    // FarmerName Setting  evana
                    if (multifarmArray.getJSONObject(j).get("farmerId").toString().equalsIgnoreCase(String.valueOf(farmerID))) {
                        String defaultFarmerName = farmerName;
                        farmerNameEdittext.setText(defaultFarmerName);
                    } else {
                        String farmerNameFromDb = dl.getFarmerNamefromDb(Long.valueOf(farmerPId));
                        farmerNameEdittext.setText(farmerNameFromDb);
                    }

                    farmerNameEdittext.setTag(farmerPId);

                    // Should cset the farmer Name - New field incorporate
                    farmNameEdittext.setText(multifarmArray.getJSONObject(j).get("farmName").toString());
                    farmCodeEdittext.setText(multifarmArray.getJSONObject(j).get("farmCode").toString());
                    farmCodeEdittext.setTag(multifarmArray.getJSONObject(j).get("farmsId").toString());

                    // Allowing the Farmer and delete the farmer stakeholder
                    bindFarmRoleDelete(farmStakeholderRow.findViewById(R.id.farmRoleDelete));
                    bindFarmerRoles(farmStakeholderRow.findViewById(R.id.farmerRoleSpinner));
                    farmRoleSpinner.setSelection(Integer.parseInt(roleId) - 1);
                    // Set Tag in edit Mode
                    farmRoleSpinner.setTag(roleId);

                    farmStakeholderLayout.addView(farmStakeholderRow);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private MultiFarmDTO getAllMultiFarmList1(long farmerId) {

        int stakeholderCount = farmStakeholderLayout.getChildCount();

        String farmName = farmNameEdittext.getText().toString().trim();
        String farmCode = farmCodeEdittext.getText().toString().trim();
        Object farmIdObj=farmCodeEdittext.getTag();
        String farmIds=null;
        if (farmIdObj!=null){
            farmIds=farmIdObj.toString();
        }

        MultiFarmDTO multiFarmDTO = new MultiFarmDTO();
        multiFarmDTO.setFarmName(farmName);
        multiFarmDTO.setFarmCode(farmCode);

        multiFarmDTO.setFarmId(farmIds);

        JSONArray jsonArray = new JSONArray();

        for (int j = 0; j < stakeholderCount; j++) {
            LinearLayout stakeHolderRow = (LinearLayout) farmStakeholderLayout.getChildAt(j);
            JSONObject jsonObject = new JSONObject();


            TextView farmerNameET = stakeHolderRow.findViewById(R.id.farmFarmerTV);
            Spinner farmRoleSpinner = stakeHolderRow.findViewById(R.id.farmerRoleSpinner);
            String farmerStakeHolderId = farmerNameET.getTag().toString();
            String farmerNameFarm = String.valueOf(farmerNameET.getText().toString().trim());
            Object id=stakeHolderRow.getTag();
            String StakeholderId=null;
            if(id!=null){
                StakeholderId=id.toString();
            }

            try {
                jsonObject.put("farmCode", farmCode);
                jsonObject.put("farmName", farmName);
             //   if(isExistingFarm) {
                    jsonObject.put("id", StakeholderId);
             //   }

                if (farmerName.equalsIgnoreCase(farmerNameFarm)) {
                    jsonObject.put("primary", true);
                    jsonObject.put("farmerId", farmerId);
                } else {
                    jsonObject.put("primary", false);
                    jsonObject.put("farmerId", Long.valueOf(farmerStakeHolderId));
                }

                jsonObject.put("farmerRoleId", Long.valueOf(String.valueOf(farmRoleSpinner.getTag())));
//                if (isExistingFarm){
//                    jsonObject.put("farmsId",Long.valueOf(farmIds));
//                }else{
//                    jsonObject.put("farmsId",null);
//                }
                if (farmIds !=null) {
                    jsonObject.put("farmsId",Long.valueOf(farmIds));
                }
                jsonObject.put("enumeratorId",Long.valueOf(Utilities.getFromSharedPrefs(this)));
                jsonObject.put("farmerName",farmerNameFarm);

            } catch (Exception e) {
                e.printStackTrace();
            }

            jsonArray.put(jsonObject);

        }
        multiFarmDTO.setStakeholdersInfo(jsonArray);

        return multiFarmDTO;
    }



    private boolean isAllRoleFarmerFilled(){
        boolean fill=true;
        int stakeholderCount=farmStakeholderLayout.getChildCount();

        for (int j = 0; j < stakeholderCount; j++) {
            LinearLayout stakeHolderRow = (LinearLayout) farmStakeholderLayout.getChildAt(j);

            TextView farmerNameET = stakeHolderRow.findViewById(R.id.farmFarmerTV);
            Spinner farmRoleSpinner = stakeHolderRow.findViewById(R.id.farmerRoleSpinner);

            String farmerStakeHolderId = farmRoleSpinner.getTag().toString();
            String farmerName = String.valueOf(farmerNameET.getText()).replace(" ", "");

            if (farmerStakeHolderId == null || farmerName.equalsIgnoreCase("")){
                fill=false;
            }
        }
        return fill;
    }

    private ArrayList<Long> getTempFarmerList() {
        int stakeholderCount=farmStakeholderLayout.getChildCount();
        ArrayList<Long> tempList=new ArrayList<Long>();
        for (int j = 0; j < stakeholderCount; j++) {
            LinearLayout stakeHolderRow = (LinearLayout) farmStakeholderLayout.getChildAt(j);

            TextView farmerNameET = stakeHolderRow.findViewById(R.id.farmFarmerTV);
            try {
                tempList.add(Long.valueOf(farmerNameET.getTag().toString()));
            }catch ( Exception e){}
        }
        return tempList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
        }