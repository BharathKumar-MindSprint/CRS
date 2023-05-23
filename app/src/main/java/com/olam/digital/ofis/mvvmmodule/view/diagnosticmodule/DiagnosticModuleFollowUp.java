package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp.FollowUpPagerAdapter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;

public class DiagnosticModuleFollowUp extends BaseActivity {


    public TabLayout tabLayout;
    ViewPager2 viewPager;
    private boolean swipeEnabled = true;

    private String[] titles = new String[]{"Follow Up Survey" , "Follow Up Action"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module_follow_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tabLayoutFollowUp);
        viewPager = findViewById(R.id.viewPagerFollowUp);

        FollowUpPagerAdapter sa = new FollowUpPagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
            tab.setText(titles[position]);
//            tab.view.setClickable(false);

        })).attach();



        // swipe disable
        swipeEnabled = !swipeEnabled;
        viewPager.setUserInputEnabled(swipeEnabled);
    }
}