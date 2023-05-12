package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.Response;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseItem;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;
import com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule.DiagnosticModuleRepository;
import com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule.DiagnosticViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;


public class DiagnosticModuleActivity extends BaseActivity {
    private DiagnosticModuleRepository diagnosticModuleRepository;
    private DiagnosticViewModel diagnosticViewModel;

    public TabLayout tabLayout;
    ViewPager2 viewPager;
    private ProgressDialog progressDialog;
    private String[] titles = new String[]{"Survey", "Action plan and proof", "Survey Results"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));
        progressDialog.show();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        PagerAdapter sa = new PagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
            tab.setText(titles[position]);
            //tab.view.setClickable(false);

        })).attach();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

       //
        diagnosticModuleRepository=new DiagnosticModuleRepository(getApplication());

        diagnosticViewModel= ViewModelProviders.of(this).get(DiagnosticViewModel.class);
        getAllDiagnosticModule();

    }

    public void goLastTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab - 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);

    }

    public void goNextTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab + 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);
    }


    private void getAllDiagnosticModule() {
        Toast.makeText(this, "ResponseMethod", Toast.LENGTH_SHORT).show();

        diagnosticViewModel.responseLiveData().observe(this, responseLiveData -> {
            if (responseLiveData != null) {
                Toast.makeText(this, "ResponseMethodINN", Toast.LENGTH_SHORT).show();
                progressDialog.hide();

            }
        });

    }

}