package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;

public class DiagnosticModuleActivity extends BaseActivity {
   /* LinearLayout ll_survey_tab,ll_actionplan_proof_tab,ll_survey_results_tab;
    LinearLayout ll_servey_next,ll_servey_back,ll_servey_back_actionplan,ll_servey_next_actionplan,ll_servey_back_servey_results,ll_servey_next_servey_results;
    TextView tv_survey,tv_actionplan_proof,tv_survey_results;
    RelativeLayout rl_survey,rl_actionplan_proof,rl_survey_results;*/

    public TabLayout tabLayout;
    ViewPager2 viewPager;
    private String[] titles = new String[] {"Survey" , "Action plan and proof","Survey Results"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout=findViewById(R.id.tabLayout);
        viewPager=findViewById(R.id.viewPager);

       /* tabLayout.addTab(tabLayout.newTab().setText("Survey"));
        tabLayout.addTab(tabLayout.newTab().setText("Action plan and proof"));
        tabLayout.addTab(tabLayout.newTab().setText("Survey Results"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);*/

        //FragmentActivity fm = getFragmentManager();
        PagerAdapter sa = new PagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout,viewPager,((tab, position) ->{
            tab.setText(titles[position]);
            //tab.view.setClickable(false);

        } )).attach();


       //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

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

       /* ll_survey_tab = findViewById(R.id.ll_survey_tab);
        ll_actionplan_proof_tab = findViewById(R.id.ll_actionplan_proof_tab);
        ll_survey_results_tab = findViewById(R.id.ll_survey_results_tab);

        ll_servey_next = findViewById(R.id.ll_servey_next);
        ll_servey_back = findViewById(R.id.ll_servey_back);


        ll_servey_back_actionplan = findViewById(R.id.ll_servey_back_actionplan);
        ll_servey_next_actionplan = findViewById(R.id.ll_servey_next_actionplan);


        ll_servey_back_servey_results = findViewById(R.id.ll_servey_back_servey_results);
        ll_servey_next_servey_results = findViewById(R.id.ll_servey_next_servey_results);

        rl_survey = findViewById(R.id.rl_survey);
        rl_actionplan_proof = findViewById(R.id.rl_actionplan_proof);
        rl_survey_results = findViewById(R.id.rl_survey_results);


        tv_survey = findViewById(R.id.tv_survey);
        tv_actionplan_proof = findViewById(R.id.tv_actionplan_proof);
        tv_survey_results = findViewById(R.id.tv_survey_results);

        ll_servey_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_actionplan_proof_tab.setBackgroundColor(getResources().getColor(R.color.ofis_farmer_screen_actions_background));
                rl_actionplan_proof.setVisibility(View.VISIBLE);
                rl_survey.setVisibility(View.GONE);
                ll_survey_tab.setBackgroundColor(getResources().getColor(R.color.dull_white));
                tv_survey.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_actionplan_proof.setTextColor(getResources().getColor(R.color.white));
            }
        });

        ll_servey_next_actionplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_survey_results_tab.setBackgroundColor(getResources().getColor(R.color.ofis_farmer_screen_actions_background));
                rl_survey_results.setVisibility(View.VISIBLE);
                rl_actionplan_proof.setVisibility(View.GONE);
                rl_survey.setVisibility(View.GONE);
                ll_survey_tab.setBackgroundColor(getResources().getColor(R.color.dull_white));
                ll_actionplan_proof_tab.setBackgroundColor(getResources().getColor(R.color.dull_white));
                tv_survey.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_actionplan_proof.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_survey_results.setTextColor(getResources().getColor(R.color.white));

            }
        });*/


    }
   /* public void navigateFragment(int position){
        viewPager.setCurrentItem(position, true);

    }*/

    public void goLastTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab -1;
        if (nextTab >= viewPager.getAdapter().getItemCount()){
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);

    }

    public void goNextTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab +1;
        if (nextTab >= viewPager.getAdapter().getItemCount()){
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);
    }

}