package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.olam.digital.ofis.AddFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;


public class ActionPlanProofFragment extends Fragment {
    LinearLayout ll_servey_back_actionplan,ll_servey_next_actionplan;

    public ActionPlanProofFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_action_plan_froof, container, false);

        ll_servey_back_actionplan = view.findViewById(R.id.ll_servey_back_actionplan);
        ll_servey_next_actionplan = view.findViewById(R.id.ll_servey_next_actionplan);



        return view;
    }
}