package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.olam.digital.ofis.AddFarmerActivity;
import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;


public class SurveyResultsFragment extends Fragment {
    LinearLayout ll_servey_next_servey_results,ll_servey_back_servey_results;

    public SurveyResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_survey_results, container, false);

        ll_servey_next_servey_results = view.findViewById(R.id.ll_servey_next_servey_results);
        ll_servey_back_servey_results = view.findViewById(R.id.ll_servey_back_servey_results);

        ll_servey_next_servey_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFarmerActivity.class);
                startActivity(intent);
            }
        });




        return view;
    }
}