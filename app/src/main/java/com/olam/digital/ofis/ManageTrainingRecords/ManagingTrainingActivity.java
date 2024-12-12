package com.olam.digital.ofis.ManageTrainingRecords;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.ManageTrainingRecords.adapter.CustomViewPager;
import com.olam.digital.ofis.ManageTrainingRecords.adapter.ViewpageAdapter;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.ManageTrainingRecords.model.Assets;
import com.olam.digital.ofis.ManageTrainingRecords.model.Course;
import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;
import com.olam.digital.ofis.ManageTrainingRecords.model.Languages;
import com.olam.digital.ofis.ManageTrainingRecords.model.Module;
import com.olam.digital.ofis.authentication.AuthenticationHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagingTrainingActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {
    private Spinner courseSpinner,moduleSpinner,languageSpinner;
    private Button retry_btn;
    private RelativeLayout Home_frame;
    private RelativeLayout Network_frame;
    private IntentFilter mIntentFilter;
    private ArrayAdapter<String> Language_Arrayadapter;
    private LinearLayout r1;
    private RelativeLayout tab;
    private ProgressDialog progressDialog;
    private boolean CourseList_Flag=false;
    TabLayout tabLayout;
    private CustomViewPager viewpager;
    String[] Asset_name;
    List<DocumentList.data> data;
    ViewpageAdapter adapter;
    boolean noDataFound = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managing_training);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));
        progressDialog.show();
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Home_frame.setVisibility(View.VISIBLE);
                    Network_frame.setVisibility(View.INVISIBLE);

                } else {
                    showAlert(getString(R.string.Please_check_the_Internet_Connection));
                }
            }
        });
    }
    private void Spinnerhint(){
        //course hint
        ArrayList<String> course_arr = new ArrayList<>();
        course_arr.add(getString(R.string.selecting_course));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManagingTrainingActivity.this,R.layout.spinner_item, course_arr);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        courseSpinner.setAdapter(spinnerArrayAdapter);
        //module hint
        ArrayList<String> mod_arr = new ArrayList<>();
        mod_arr.add(getString(R.string.select_module));
        ArrayAdapter<String> moduleArrayAdapter = new ArrayAdapter<String>(ManagingTrainingActivity.this, R.layout.spinner_item, mod_arr);
        moduleArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        moduleSpinner.setAdapter(moduleArrayAdapter);
        //language hint
        ArrayList<String> language_arr = new ArrayList<>();
        language_arr.add(getString(R.string.select_language));
        ArrayAdapter<String> languageArrayadapter = new ArrayAdapter<String>(ManagingTrainingActivity.this, R.layout.spinner_item, language_arr);
        languageArrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        languageSpinner.setAdapter(languageArrayadapter);
    }
    private void AssetsMethod( int CourseLanguageId) {
        tab.setVisibility(View.INVISIBLE);
        viewpager=findViewById(R.id.viewpager);
        ManageRecords AllAssets= RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<Assets> call=AllAssets.getAllAsset(CourseLanguageId);
        call.enqueue(new Callback<Assets>() {
            @Override
            public void onResponse(Call<Assets> call, Response<Assets> response) {
                if(response.code()==200){
                    if(response.body().getStatusCode()==200) {
                        if (response.body().getData() != null) {
                            List<Assets.data> data = response.body().getData();
                            if (data != null && data.size() > 0) {
                                Asset_name = new String[data.size()];
                                tabLayout.removeAllTabs();
                                progressDialog.cancel();
                                r1.setVisibility(View.VISIBLE);
                                tab.setVisibility(View.VISIBLE);
                                for (int i = 0; i < data.size(); i++) {
                                    Asset_name[i] = data.get(i).getAssetType();
                                    DocList(data.get(i).getCourseAssetTypeId(), CourseLanguageId);
                                }
                            }
                        }
                    }
                }
                else {
                    progressDialog.cancel();
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }

            }

            @Override
            public void onFailure(Call<Assets> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, "Error in Assets API call"+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public BroadcastReceiver MyReceiver =new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                if(isOnline(context)){
                    if(CourseList_Flag==false){
                        Spinnerhint();
                        courseSpinnerMethod();
                    }
                    Home_frame.setVisibility(View.VISIBLE);
                    Network_frame.setVisibility(View.INVISIBLE);
                }else{
                    progressDialog.cancel();
                    Network_frame.setVisibility(View.VISIBLE);
                    Home_frame.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    public boolean isOnline(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo ni=cm.getActiveNetworkInfo();
        if(ni !=null && ni.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(R.string.Alert_txt);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
            if (noDataFound){
                finish();
                onBackPressed();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
    private void init() {
        courseSpinner=(Spinner)findViewById(R.id.spinner_course);
        moduleSpinner=(Spinner)findViewById(R.id.spinner_module);
        languageSpinner=(Spinner)findViewById(R.id.spinner_language);
        Home_frame=(RelativeLayout)findViewById(R.id.home_layout);
        Network_frame=(RelativeLayout)findViewById(R.id.check_network);
        retry_btn=(Button)findViewById(R.id.retry_btn);
        r1 = (LinearLayout) findViewById(R.id.TabviewLayout);
        tab=(RelativeLayout) findViewById(R.id.ListRelativeLayout);
        tabLayout=findViewById(R.id.tablayout);
    }

    private void DocList(int coursesAssetTypeId,int coursesLanguageId) {
        tab.setVisibility(View.INVISIBLE);
        ManageRecords AllDocumentList=RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DocumentList> call=AllDocumentList.getAllDocuments(coursesAssetTypeId,coursesLanguageId);
        call.enqueue(new Callback<DocumentList>() {
            @Override
            public void onResponse(Call<DocumentList> call, Response<DocumentList> response) {
                if(response.code()==200){
                    if(response.body()!=null){
                        if(response.body().getData()!=null){
                            tab.setVisibility(View.VISIBLE);
                            data=response.body().getData();
                            tabLayout.setupWithViewPager(viewpager);
                            String[] name = {"Document","Media"};
                            adapter=new ViewpageAdapter(getSupportFragmentManager(),data,name);
                            viewpager.setAdapter(adapter);
                            viewpager.setPagingEnabled(false);
                            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                            tabLayout.setTabTextColors(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                            tabLayout.setTabTextColors(getResources().getColor(R.color.black),getResources().getColor(R.color.white));
                            new TabLayout.TabLayoutOnPageChangeListener(tabLayout);
                            tabLayout.setOnTabSelectedListener(ManagingTrainingActivity.this);
                            LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
                            tabStrip.getChildAt(1).setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            });
                        }else{
                            Toast.makeText(ManagingTrainingActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ManagingTrainingActivity.this, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }
            }
            @Override
            public void onFailure(Call<DocumentList> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void languageSpinnerMethod(int courseTrainingModuleId) {
        ManageRecords AllLanguages=RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<Languages> call=AllLanguages.getAllLanguages(courseTrainingModuleId);
        call.enqueue(new Callback<Languages>() {
            @Override
            public void onResponse(Call<Languages> call, Response<Languages> response) {
                if(response.code()==200){
                    if(response.body()!=null){
                        progressDialog.cancel();
                        List<Languages.data> LanguageList = response.body().getData();

                        if (LanguageList != null && LanguageList.size() > 0) {
                            String[] Languages = new String[LanguageList.size()];
                            ArrayList<String> lang_arr =new ArrayList<>();
                            lang_arr.add(getString(R.string.select_language));
                            for (int i = 0; i < LanguageList.size(); i++) {
                                lang_arr.add(LanguageList.get(i).getLanguageName());
                                Languages[i] = LanguageList.get(i).getLanguageName();
                            }
                            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = languageSpinner.getSelectedItem().toString();
                                    int CourseLanguageId ;
                                    for (int j = 0; j < LanguageList.size(); j++) {
                                        Languages[j] = LanguageList.get(j).getLanguageName();

                                        if (Languages[j].equals(item)) {
                                            CourseLanguageId = LanguageList.get(j).getCourseLanguageId();
                                            progressDialog.show();
                                            AssetsMethod(CourseLanguageId);
                                        }
                                    }
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            Language_Arrayadapter = new ArrayAdapter<String>(ManagingTrainingActivity.this,R.layout.spinner_item, lang_arr);
                            Language_Arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            languageSpinner.setAdapter(Language_Arrayadapter);

                        }

                    }
                }else{
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }
            }

            @Override
            public void onFailure(Call<Languages> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moduleSpinnerMethod(int Id) {
        ManageRecords AllModules=RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<Module> call=AllModules.getAllModules(Id);
        call.enqueue(new Callback<Module>() {
            @Override
            public void onResponse(Call<Module> call, Response<Module> response) {
                if (response.code()==200) {
                    if (response.body() != null) {
                        progressDialog.cancel();
                        List<Module.data> ModuleList = response.body().getData();
                        if (ModuleList != null && ModuleList.size() > 0) {
                            String[] Modules = new String[ModuleList.size()];
                            ArrayList<String> mod_arr = new ArrayList<>();
                            mod_arr.add(getString(R.string.select_module));
                            for (int i = 0; i < ModuleList.size(); i++) {
                                mod_arr.add(ModuleList.get(i).getModuleName());
                                Modules[i] = ModuleList.get(i).getModuleName();
                            }
                            moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = moduleSpinner.getSelectedItem().toString();
                                    int courseTrainingModuleId;
                                    r1.setVisibility(View.INVISIBLE);
                                    tab.setVisibility(View.INVISIBLE);
                                    for (int j = 0; j < ModuleList.size(); j++) {
                                        Modules[j] = ModuleList.get(j).getModuleName();
                                        if (Modules[j] == item) {
                                            courseTrainingModuleId = ModuleList.get(j).getCourseTrainingModuleId();
                                            progressDialog.show();
                                            languageSpinnerMethod(courseTrainingModuleId);
                                        }
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManagingTrainingActivity.this, R.layout.spinner_item, mod_arr);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            moduleSpinner.setAdapter(spinnerArrayAdapter);
                        }
                    }
                } else {
                    progressDialog.cancel();
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }
            }
            @Override
            public void onFailure(Call<Module> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void courseSpinnerMethod() {
        ManageRecords AllCourse=RetrofitClient.getRetrofit().create(ManageRecords.class);
        String EnumeratorID=new AuthenticationHelper(context).getuserName();
        Call<Course> call=AllCourse.getAllCourses();
        call.enqueue(new Callback<Course>() {
            @Override
            public void onResponse(Call<Course> call, Response<Course> response) {
                if(response.code()==200){
                    if(response.body()!=null){
                        progressDialog.cancel();
                        CourseList_Flag=true;
                        List<Course.data> CourseList = response.body().getData();

                        if (CourseList != null && CourseList.size() > 0) {
                            ArrayList<String> arr =new ArrayList<>();
                            String[] Course = new String[CourseList.size()];
                            arr.add(getString(R.string.selecting_course));
                            for ( int i = 0; i < CourseList.size(); i++) {
                                arr.add(CourseList.get(i).getCourseName());
                                Course[i] = CourseList.get(i).getCourseName();
                            }
                            courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String item = courseSpinner.getSelectedItem().toString();
                                    int position=courseSpinner.getSelectedItemPosition();
                                    int Id;
                                    if(Language_Arrayadapter!=null&&position!=0){
                                        Language_Arrayadapter.clear();
                                        //language hint
                                        ArrayList<String> language_arr = new ArrayList<>();
                                        language_arr.add(getString(R.string.select_language));
                                        ArrayAdapter<String> languageArrayadapter = new ArrayAdapter<String>(ManagingTrainingActivity.this, R.layout.spinner_item, language_arr);
                                        languageArrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        Language_Arrayadapter.notifyDataSetChanged();
                                        languageSpinner.setAdapter(languageArrayadapter);
                                        r1.setVisibility(View.INVISIBLE);
                                        tab.setVisibility(View.INVISIBLE);
                                    }
                                    for (int j = 0; j < CourseList.size(); j++) {
                                        Course[j] = CourseList.get(j).getCourseName();

                                        if(Course[j].equals(item)){
                                            Id = CourseList.get(j).getCourseId();
                                            progressDialog.show();
                                            moduleSpinnerMethod(Id);
                                        }
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManagingTrainingActivity.this,R.layout.spinner_item, arr);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            courseSpinner.setAdapter(spinnerArrayAdapter);

                        }
                    }else {
                        CourseList_Flag=false;
                    }
                }else{
                    CourseList_Flag=false;
                    progressDialog.cancel();
                    noDataFound = true;
                    showAlert(getString(R.string.no_data_found));
                }
            }
            @Override
            public void onFailure(Call<Course> call, Throwable t) {
                CourseList_Flag=true;
                progressDialog.cancel();
                Toast.makeText(ManagingTrainingActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(MyReceiver,mIntentFilter,RECEIVER_EXPORTED);
        } else {
            registerReceiver(MyReceiver,mIntentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(MyReceiver,mIntentFilter,RECEIVER_EXPORTED);
        } else {
            registerReceiver(MyReceiver,mIntentFilter);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewpager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}




