package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp.FollowUpPagerAdapter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;

public class DiagnosticModuleFollowUp extends BaseActivity {


    public TabLayout tabLayout;
    ViewPager2 viewPager;
    private boolean swipeEnabled = true;
    TextView followup_subtitle;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module_follow_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tabLayoutFollowUp);
        viewPager = findViewById(R.id.viewPagerFollowUp);
        followup_subtitle = findViewById(R.id.followup_subtitle);
        if(!Constants.MODULE_NAME.equalsIgnoreCase("0")) {
            followup_subtitle.setText(Constants.MODULE_NAME);
        }

        FollowUpPagerAdapter sa = new FollowUpPagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
//            tab.setText(titles[position]);
            tab.view.setClickable(false);

        })).attach();



        // swipe disable
        viewPager.setUserInputEnabled(false);

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
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "SURVEY";
            case 1:
                return "Action plan and proof";
            default:
                return "";
        }
    }


    public void goLastTabFollowUp(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab - 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);

    }

    public void goNextTabFollowUp(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab + 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);
    }


}