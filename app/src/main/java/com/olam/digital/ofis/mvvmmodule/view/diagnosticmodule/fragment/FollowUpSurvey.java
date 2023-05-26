package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.olam.digital.ofis.R;


public class FollowUpSurvey extends Fragment {

    ImageView iv_complaint_dropdown_follow_up_survey , iv_non_complaint_dropdown_follow_up_survey , iv_satisfactory_dropdown_follow_up_survey;
    LinearLayout ll_complaint_follow_up_survey , ll_non_complaint_follow_up_survey, ll_satisfactory_follow_up_survey;

    public FollowUpSurvey() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_follow_up_survey, container, false);

        iv_complaint_dropdown_follow_up_survey = view.findViewById(R.id.iv_complaint_dropdown_follow_up_survey);
        iv_non_complaint_dropdown_follow_up_survey = view.findViewById(R.id.iv_non_complaint_dropdown_follow_up_survey);
        iv_satisfactory_dropdown_follow_up_survey = view.findViewById(R.id.iv_satisfactory_dropdown_follow_up_survey);
        ll_complaint_follow_up_survey = view.findViewById(R.id.ll_complaint_follow_up_survey);
        ll_non_complaint_follow_up_survey = view.findViewById(R.id.ll_non_complaint_follow_up_survey);
        ll_satisfactory_follow_up_survey = view.findViewById(R.id.ll_satisfactory_follow_up_survey);


        iv_complaint_dropdown_follow_up_survey.setTag("0");
        iv_complaint_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_complaint_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_complaint_dropdown_follow_up_survey.setTag("1");
                    iv_complaint_dropdown_follow_up_survey.setRotation(180);
                    ll_complaint_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_complaint_dropdown_follow_up_survey.setTag("0");
                    iv_complaint_dropdown_follow_up_survey.setRotation(0);
                    ll_complaint_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });

        iv_non_complaint_dropdown_follow_up_survey.setTag("0");
        iv_non_complaint_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_non_complaint_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_non_complaint_dropdown_follow_up_survey.setTag("1");
                    iv_non_complaint_dropdown_follow_up_survey.setRotation(180);
                    ll_non_complaint_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_non_complaint_dropdown_follow_up_survey.setTag("0");
                    iv_non_complaint_dropdown_follow_up_survey.setRotation(0);
                    ll_non_complaint_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });

        iv_satisfactory_dropdown_follow_up_survey.setTag("0");
        iv_satisfactory_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_satisfactory_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_satisfactory_dropdown_follow_up_survey.setTag("1");
                    iv_satisfactory_dropdown_follow_up_survey.setRotation(180);
                    ll_satisfactory_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_satisfactory_dropdown_follow_up_survey.setTag("0");
                    iv_satisfactory_dropdown_follow_up_survey.setRotation(0);
                    ll_satisfactory_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });



        return view;
    }
}