package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;

import java.util.ArrayList;


public class SurveyFragment extends Fragment {
    LinearLayout ll_servey_next,ll_servey_back;


    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_survey, container, false);

        ll_servey_next = view.findViewById(R.id.ll_servey_next);
//        ll_servey_back = view.findViewById(R.id.ll_servey_back);




        return view;
    }


}