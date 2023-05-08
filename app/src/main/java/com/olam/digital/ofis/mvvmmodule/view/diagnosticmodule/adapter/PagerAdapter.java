package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.ActionPlanProofFragment;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.SurveyFragment;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.SurveyResultsFragment;

public class PagerAdapter extends FragmentStateAdapter {
    private Context myContext;
   // int totalTabs;
    private String[] titles = new String[] {"Survey" , "Action plan and proof","Survey Results"};

    public PagerAdapter( @NonNull FragmentActivity fm){
        super(fm);
        //myContext = context;
        //this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

           case 0:
                SurveyFragment surveyFragment = new SurveyFragment();
                return surveyFragment;

            case 1:
                ActionPlanProofFragment actionPlanProofFragment = new ActionPlanProofFragment();
                return actionPlanProofFragment;
            case 2:
                SurveyResultsFragment surveyResultsFragment = new SurveyResultsFragment();
                return surveyResultsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
