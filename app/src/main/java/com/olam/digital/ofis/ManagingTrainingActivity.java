package com.olam.digital.ofis;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.olam.digital.ofis.model.Assets;
import com.olam.digital.ofis.model.Course;
import com.olam.digital.ofis.model.DocumentList;
import com.olam.digital.ofis.model.Languages;
import com.olam.digital.ofis.model.Module;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagingTrainingActivity extends BaseActivity  implements AdapterView.OnItemSelectedListener {
    private Spinner courseSpinner,moduleSpinner,languageSpinner;
    private TextView Media_Text;
    private Button retry_btn;
    private RecyclerView recyclerView;
    private RelativeLayout Home_frame;
    private RelativeLayout Network_frame;
    private IntentFilter mIntentFilter;
    private ArrayAdapter<String> Language_Arrayadapter;
    private LinearLayout r1;
    private ProgressDialog progressDialog;
    private String Alert_msg="Oops!!! Something went wrong. Please contact the administrator.";
    private boolean CourseList_Flag=false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managing_training);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Loading .....");
        progressDialog.show();
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction("checkinternet");
        Intent serviceIntent= new Intent(this,MyService.class);
        startService(serviceIntent);
        if(isOnline(getApplicationContext())){
            if(CourseList_Flag==false){
                courseSpinnerMethod();
            }
            Home_frame.setVisibility(View.VISIBLE);
            Network_frame.setVisibility(View.INVISIBLE);
        }else {
            progressDialog.cancel();
            Network_frame.setVisibility(View.VISIBLE);
            Home_frame.setVisibility(View.INVISIBLE);
        }
        Media_Text.setVisibility(View.INVISIBLE);
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Home_frame.setVisibility(View.VISIBLE);
                    Network_frame.setVisibility(View.INVISIBLE);

                } else {
                    String msg="Please check the Internet Connection";
                    showAlert(msg);
                }
            }
        });
    }

    private void AssetsMethod( int CourseLanguageId) {
        Media_Text.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        ManageRecords AllAssets=RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<Assets> call=AllAssets.getAllAsset(CourseLanguageId);
        call.enqueue(new Callback<Assets>() {
            @Override
            public void onResponse(Call<Assets> call, Response<Assets> response) {
                if(response.code()==200){
                    if(response.body().getStatusCode()==200){
                        if(response.body().getData()!=null) {
                            List<Assets.data> data = response.body().getData();
                            if (data != null && data.size() > 0) {
                                String[] Asset_name = new String[data.size()];
                                DocList(data.get(0).getCourseAssetTypeId(),CourseLanguageId);
                                for (int i = 0; i < data.size(); i++) {
                                    Asset_name[i] = data.get(i).getAssetType();
                                    Button btn = new Button(ManagingTrainingActivity.this);
                                    LinearLayout.LayoutParams mar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    r1.removeAllViews();
                                    recyclerView.setVisibility(View.INVISIBLE);
                                    r1.addView(btn);
                                    btn.setText(Asset_name[i]);
                                    btn.setWidth(500);
                                    btn.setHeight(170);
                                    if(i==0){
                                          btn.setTextColor(getResources().getColor(R.color.white));
                                          btn.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
                                    }else{
                                        btn.setBackgroundColor(getResources().getColor(R.color.disabled_gray));
                                    }
                                    r1.setVisibility(View.VISIBLE);
                                    int finalI1 = i;
                                    progressDialog.cancel();
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                              btn.setTextColor(getResources().getColor(R.color.white));
                                              btn.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
                                            DocList(data.get(finalI1).getCourseAssetTypeId(),CourseLanguageId);

                                        }
                                    });
                                }
                            }
                        }
                    }
                }
                else {
                    progressDialog.cancel();
                    showAlert(Alert_msg);
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
            if(intent.getAction().equals("checkinternet")){
                if(intent.getStringExtra("online_status").equals("true")){
                    if(CourseList_Flag==false){
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
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
    private void init() {
        courseSpinner=(Spinner)findViewById(R.id.spinner_course);
        moduleSpinner=(Spinner)findViewById(R.id.spinner_module);
        languageSpinner=(Spinner)findViewById(R.id.spinner_language);
        Media_Text=(TextView)findViewById(R.id.media_text);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        Home_frame=(RelativeLayout)findViewById(R.id.home_layout);
        Network_frame=(RelativeLayout)findViewById(R.id.check_network);
        retry_btn=(Button)findViewById(R.id.retry_btn);
        r1 = (LinearLayout) findViewById(R.id.TabviewLayout);
    }

    private void DocList(int coursesAssetTypeId,int coursesLanguageId) {
        recyclerView.setVisibility(View.INVISIBLE);
        ManageRecords AllDocumentList=RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DocumentList> call=AllDocumentList.getAllDocuments(coursesAssetTypeId,coursesLanguageId);
        call.enqueue(new Callback<DocumentList>() {
            @Override
            public void onResponse(Call<DocumentList> call, Response<DocumentList> response) {
               if(response.code()==200){
                   if(response.body()!=null){
                       if(response.body().getData()!=null){
                           recyclerView.setVisibility(View.VISIBLE);
                           List<DocumentList.data> data=response.body().getData();
                           RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
                           DocumentListAdapter adapter = new DocumentListAdapter(data);
                           recyclerView.setHasFixedSize(true);
                           recyclerView.setLayoutManager(new LinearLayoutManager(ManagingTrainingActivity.this));
                           recyclerView.setAdapter(adapter);
                       }else{
                           Toast.makeText(ManagingTrainingActivity.this, "Error in getData", Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       Toast.makeText(ManagingTrainingActivity.this, "No data Found", Toast.LENGTH_SHORT).show();
                   }
               }else{
                   showAlert(Alert_msg);
               }
            }
            @Override
            public void onFailure(Call<DocumentList> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, "Error in Document API call"+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                        lang_arr.add("Select Language");
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
                showAlert(Alert_msg);
            }
            }

            @Override
            public void onFailure(Call<Languages> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, "Error in Language API call"+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                            mod_arr.add("Select Module");
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
                                    recyclerView.setVisibility(View.INVISIBLE);
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
                        showAlert(Alert_msg);
                    }
                }


            @Override
            public void onFailure(Call<Module> call, Throwable t) {
                Toast.makeText(ManagingTrainingActivity.this, "Error in Module API call"+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void courseSpinnerMethod() {
        ManageRecords AllCourse=RetrofitClient.getRetrofit().create(ManageRecords.class);
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
                         arr.add("Select Course");
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
                                     Language_Arrayadapter.notifyDataSetChanged();
                                     r1.setVisibility(View.INVISIBLE);
                                     recyclerView.setVisibility(View.INVISIBLE);
                                     Media_Text.setVisibility(View.INVISIBLE);
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
                 showAlert(Alert_msg);
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
        registerReceiver(MyReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(MyReceiver,mIntentFilter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}





