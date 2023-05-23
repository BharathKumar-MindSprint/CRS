package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.ActionPlanProofFragment;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.FollowUpActionPlan;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.FollowUpSurvey;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.SurveyFragment;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment.SurveyResultsFragment;

public class FollowUpPagerAdapter extends FragmentStateAdapter {

    private Context myContext;
    // int totalTabs;
    private String[] titles = new String[] {"Follow Up Survey" , "Follow Up Action"};

    public FollowUpPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            case 0:
                return new FollowUpSurvey();

            case 1:
                return new FollowUpActionPlan();

            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
