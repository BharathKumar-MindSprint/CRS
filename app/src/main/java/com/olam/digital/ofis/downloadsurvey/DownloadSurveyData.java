package com.olam.digital.ofis.downloadsurvey;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.NetworkLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.SettingsActivity;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AccessTokenTask;
import com.olam.digital.ofis.authentication.OnAccessTokenInterface;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.downloadsurvey.adapter.DownloadedModuleAdapter;
import com.olam.digital.ofis.downloadsurvey.adapter.FilterModuleRecyclerAdapter;
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemDownloadedSurveyClickListener;

import java.util.ArrayList;


public class DownloadSurveyData extends BaseActivity implements OnAccessTokenInterface {

    private Context context;
    String moduleName, moduleId;
    Button downloadData;
    private static TextView recordCount;
    private Spinner farmerGroupSpinner, sectionSpinner;
    private long selectedFarmerGroup = 0, selectedSection = 0;
    AnswerRequestDTO requestDTO;
    private static Context staticContext;
    private static RecyclerView moduleListRecyclerView;
    private static DownloadedModuleAdapter downloadedModuleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadsurvey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        setUpSearchFunctionality();
    }


    private void init() {
        context = this;
        staticContext = this;
        downloadData = (Button) findViewById(R.id.downloadData);
        recordCount = (TextView) findViewById(R.id.recordCount);
        farmerGroupSpinner = (Spinner) findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (Spinner) findViewById(R.id.spinner_section);
        moduleListRecyclerView = (RecyclerView) findViewById(R.id.downloadedList);
        dl = new DataLayer(res, this);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            ModuleDTO moduleDTO = (ModuleDTO) bundle.getSerializable("module");
            moduleId = moduleDTO.getModule_id();
            moduleName = moduleDTO.getName();
        }

        downloadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFarmerGroup == 0){
                    Toast.makeText(DownloadSurveyData.this, getResources().getString(R.string.fill_farmergroup_name),Toast.LENGTH_LONG).show();
                    return;
                } else if (selectedSection == 0){
                    Toast.makeText(DownloadSurveyData.this, getResources().getString(R.string.fill_section),Toast.LENGTH_LONG).show();
                    return;
                }

                requestDTO = new AnswerRequestDTO();
                requestDTO.setModule_id(moduleId);
                requestDTO.setModule_name(moduleName);
                requestDTO.setFarmerGroupId(String.valueOf(selectedFarmerGroup));
                requestDTO.setSectionId(String.valueOf(selectedSection));

                checkAuthenticate();
            }
        });
    }

    private void setUpSearchFunctionality() {
        Cursor farmerGroups = dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        dl.bindDataDrivenSpinner(farmerGroupSpinner, farmerGroups, context, R.string.spinner_farmergroup_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

        farmerGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor sections = dl.getSpinnerData(OFISDBContract.SectionsEntry.TABLE_NAME, true, OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID, ((int) id), true, OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME);
                dl.bindDataDrivenSpinner(sectionSpinner, sections, context, R.string.spinner_section_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

                selectedFarmerGroup = id;
                selectedSection = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = id;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        try {
            ArrayList<AnswerRequestDTO> moduleList = dl.getDownloadedSurveyList();
            loadData(dl, moduleList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkAuthenticate(){
        NetworkLayer nl = new NetworkLayer(context, res);
        if (nl.isPhoneOnline()) {
            AccessTokenTask accessTokenTask = new AccessTokenTask(context, getAuthRequestDTO(), DownloadSurveyData.this);
            accessTokenTask.execute();
        }else{
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private AuthRequestDTO getAuthRequestDTO(){
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(Utilities.getFromSharedPrefs(context));
        authRequestDTO.setPasscode(Utilities.getPasscodeFromSharedPrefs(DownloadSurveyData.this));
        return authRequestDTO;
    }


    @Override
    public void onTokenCompleted(String status) {
        if(! status.equalsIgnoreCase("false")){
            NetworkLayer nl = new NetworkLayer(context, getResources());
            nl.DownloadSurveyData(requestDTO);
            //noinspection UnusedAssignment
            nl = null;
        }else{
            Toast.makeText(DownloadSurveyData.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
        }
    }

    public static void refreshDownloadedSurveyList(AnswerRequestDTO answerRequestDTO){
//        Toast.makeText( staticContext,"Download completed",Toast.LENGTH_LONG).show();
        new surveyTask().execute(answerRequestDTO);


    }

    private static void dialogLogout(DataLayer dl, AnswerRequestDTO moduleDTO, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadSurveyData.staticContext);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(staticContext);
        View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText("Are you sure want to delete this downloaded record?");
        Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                dl.deleteDownloadedSurveys(moduleDTO.getFarmerGroupId(), moduleDTO.getSectionId());
//                downloadedModuleAdapter.resetItem(dl.getDownloadedSurveyList());
//                downloadedModuleAdapter.notifyItemChanged(position);
                if(dl.getDownloadedSurveyList().size()==0){
                    recordCount.setVisibility(View.GONE);
                }
                Toast.makeText(staticContext, "Record Deleted", Toast.LENGTH_LONG).show();
                loadData(dl,dl.getDownloadedSurveyList());
            }
        });

        alert.show();
    }

    private static class surveyTask extends AsyncTask<AnswerRequestDTO, Integer, String> {
        ArrayList<AnswerRequestDTO> moduleList;
        DataLayer dl = new DataLayer(staticContext.getResources(), staticContext);

        @Override
        protected String doInBackground(AnswerRequestDTO... answerRequestDTOS) {
            AnswerRequestDTO answerRequestDTO = answerRequestDTOS[0];

            dl.addDownloadedSurveyList(answerRequestDTO);
            moduleList = dl.getDownloadedSurveyList();

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadData(dl,moduleList);

        }


    }

    public static void loadData(DataLayer dl, ArrayList<AnswerRequestDTO> moduleList) {
        if(moduleList.size() > 0) {
            recordCount.setText("Total records Downloaded: " +moduleList.size());
            downloadedModuleAdapter = new DownloadedModuleAdapter(moduleList, staticContext);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(staticContext);
            moduleListRecyclerView.setLayoutManager(reLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(staticContext, DividerItemDecoration.VERTICAL);
            itemDecoration.setDrawable(staticContext.getResources().getDrawable(R.drawable.list_seperator_divider));
            moduleListRecyclerView.addItemDecoration(itemDecoration);
            moduleListRecyclerView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
            moduleListRecyclerView.setAdapter(downloadedModuleAdapter);
            moduleListRecyclerView.scrollToPosition(downloadedModuleAdapter.getItemCount() - 1);
            downloadedModuleAdapter.setOnItemClickListener(new OnItemDownloadedSurveyClickListener() {
                @Override
                public void onModuleClick(View view, int position, AnswerRequestDTO moduleDTO) {
                    dialogLogout(dl,moduleDTO,position);
                    /*dl.deleteDownloadedSurveys(moduleDTO.getFarmerGroupId(), moduleDTO.getSectionId());
                    downloadedModuleAdapter.resetItem(dl.getDownloadedSurveyList());
                    downloadedModuleAdapter.notifyItemChanged(position);*/
                }
            });
        }
    }
}
