package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.olam.digital.ofis.AddFarmerActivity;
import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.coffeesurveyconstraints.Constraint;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.DmQuestionsModuleDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;


public class SurveyResultsFragment extends Fragment {
    LinearLayout ll_servey_next_servey_results, ll_servey_back_servey_results;
    ConstraintLayout survey_result_main;

    LinearLayout newView;
    ImageView traffic_indicator_green, traffic_indicator_orange, traffic_indicator_red;

    int total = 50;
    OFISRoomDB db;
    EditText editTextText, editTextText3, editTextText4, editTextText5,editTextText6;

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
        survey_result_main = view.findViewById(R.id.survey_result_main);
        editTextText = view.findViewById(R.id.editTextText);
        editTextText3 = view.findViewById(R.id.editTextText3);
        editTextText4 = view.findViewById(R.id.editTextText4);
        editTextText5 = view.findViewById(R.id.editTextText5);
        editTextText6 = view.findViewById(R.id.editTextText6);

        ll_servey_next_servey_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFarmerActivity.class);
                startActivity(intent);
            }
        });

        newView = view.findViewById(R.id.survey_result_indicator);
        traffic_indicator_green = view.findViewById(R.id.indicator_green);
        traffic_indicator_orange = view.findViewById(R.id.indicator_orange);
        traffic_indicator_red = view.findViewById(R.id.indicator_red);

        db = OFISRoomDB.getDbInstance(getActivity());



        editTextText.setText(Constants.complaintSize);
        editTextText3.setText(Constants.noncomplaintSize);
        editTextText4.setText(Constants.satisfactorySize);

        editTextText5.setText(String.valueOf(Integer.parseInt(Constants.complaintSize) + Integer.parseInt(Constants.satisfactorySize) + Integer.parseInt(Constants.noncomplaintSize)));

        int complaint = Integer.parseInt(Constants.complaintSize);
        int nonComplaint = Integer.parseInt(Constants.noncomplaintSize);
        int satisfactory = Integer.parseInt(Constants.satisfactorySize);

        DmQuestionsModule isCritical = new DmQuestionsModule();
        Boolean val = isCritical.isCriticalQuest;


        if (val && nonComplaint >= 1 && satisfactory >= 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));
                traffic_indicator_green.setVisibility(View.GONE);
                traffic_indicator_orange.setVisibility(View.GONE);
                traffic_indicator_red.setVisibility(View.VISIBLE);
            }
        }else{

            if (nonComplaint>complaint && nonComplaint>satisfactory){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));
                    traffic_indicator_green.setVisibility(View.GONE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.VISIBLE);
                }
            } else if (satisfactory>complaint && satisfactory>nonComplaint){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.orange_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.ambur_survey_result));
                    traffic_indicator_green.setVisibility(View.GONE);
                    traffic_indicator_orange.setVisibility(View.VISIBLE);
                    traffic_indicator_red.setVisibility(View.GONE);
                }
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.green_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.green_survey_result));
                    traffic_indicator_green.setVisibility(View.VISIBLE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.GONE);
                }
            }

        }





        return view;
    }
}