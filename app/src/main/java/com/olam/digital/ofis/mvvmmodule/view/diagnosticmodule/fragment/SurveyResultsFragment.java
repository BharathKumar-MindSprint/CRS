package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

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
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;


public class SurveyResultsFragment extends Fragment {
    LinearLayout ll_servey_next_servey_results, ll_servey_back_servey_results;

    LinearLayout newView;
    ImageView traffic_indicator;

    int total = 50;
    OFISRoomDB db;
    EditText editTextText, editTextText3, editTextText4;

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
        editTextText = view.findViewById(R.id.editTextText);
        editTextText3 = view.findViewById(R.id.editTextText3);
        editTextText4 = view.findViewById(R.id.editTextText4);

        ll_servey_next_servey_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFarmerActivity.class);
                startActivity(intent);
            }
        });

        newView = view.findViewById(R.id.survey_result_indicator);
        traffic_indicator = view.findViewById(R.id.traffic_indicator);

        db = OFISRoomDB.getDbInstance(getActivity());

        if (total < 40) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.orange_view_survey));
                traffic_indicator.setVisibility(View.GONE);

            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.green_view_survey));
                traffic_indicator.setVisibility(View.VISIBLE);
            }
        }

        editTextText.setText(Constants.complaintSize);
        editTextText3.setText(Constants.noncomplaintSize);
        editTextText4.setText(Constants.satisfactorySize);


        return view;
    }
}