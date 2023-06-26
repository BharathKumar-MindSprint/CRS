package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModulesList;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;

import java.util.List;


public class DiagnosticModuleActivity extends BaseActivity  {

    public TabLayout tabLayout;
    ViewPager2 viewPager;
    TextView tv_subtitle;

    OFISRoomDB db;
    List<DiagnosticModulesList> dMmoduleList;
    String enumerator_id;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tv_subtitle = findViewById(R.id.tv_subtitle);

        enumerator_id = Utilities.getFromSharedPrefs(context);
        Constants.ENUMERATOR_ID = Utilities.getFromSharedPrefs(context);


        if(!Constants.MODULE_NAME.equalsIgnoreCase("0"))
        {
            tv_subtitle.setText(Constants.MODULE_NAME);
        }



        PagerAdapter sa = new PagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
            //tab.setText(titles[position]);
            tab.view.setClickable(false);

        })).attach();

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.tab_item);
                TextView tabTitle = tab.getCustomView().findViewById(R.id.tabTitle);
                tabTitle.setText(getTabTitle(i));

                // Set default colors for unselected tabs
                tabTitle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                if (i == 0) {
                    // Set the first tab as selected (white) by default
                    tabTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
                }

                // Add a listener to handle color changes when a tab is selected
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        TextView selectedTabTitle = tab.getCustomView().findViewById(R.id.tabTitle);
                        selectedTabTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        TextView unselectedTabTitle = tab.getCustomView().findViewById(R.id.tabTitle);
                        unselectedTabTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // Do nothing
                    }
                });
            }
        }


        // to make the tab swipe disable
        viewPager.setUserInputEnabled(false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

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

    private String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "SURVEY";
            case 1:
                return "ACTION PLAN AND PROOF";
            case 2:
                return "SURVEY RESULTS";
            default:
                return "";
        }
    }


}