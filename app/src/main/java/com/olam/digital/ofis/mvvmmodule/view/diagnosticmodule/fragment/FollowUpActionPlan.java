package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.ViewFarmerActivity;


public class FollowUpActionPlan extends Fragment {

    ImageView iv_complaint_dropdown_follow_up_action , iv_non_complaint_dropdown_follow_up_action , iv_satisfactory_dropdown_follow_up_action;
    LinearLayout ll_complaint_follow_up_action , ll_non_complaint_follow_up_action, ll_satisfactory_follow_up_action;

    LinearLayout ll_servey_next_followup_actionplan_save;

    public FollowUpActionPlan() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_up_action_plan, container, false);

        iv_complaint_dropdown_follow_up_action = view.findViewById(R.id.iv_complaint_dropdown_follow_up_action);
        iv_non_complaint_dropdown_follow_up_action = view.findViewById(R.id.iv_non_complaint_dropdown_follow_up_action);
        iv_satisfactory_dropdown_follow_up_action = view.findViewById(R.id.iv_satisfactory_dropdown_follow_up_action);
        ll_complaint_follow_up_action = view.findViewById(R.id.ll_complaint_follow_up_action);
        ll_non_complaint_follow_up_action = view.findViewById(R.id.ll_non_complaint_follow_up_action);
        ll_satisfactory_follow_up_action = view.findViewById(R.id.ll_satisfactory_follow_up_action);

        ll_servey_next_followup_actionplan_save = view.findViewById(R.id.ll_servey_next_followup_actionplan_save);
        iv_complaint_dropdown_follow_up_action.setTag("0");
        iv_complaint_dropdown_follow_up_action.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_complaint_dropdown_follow_up_action.getTag().equals("0")) {
                    iv_complaint_dropdown_follow_up_action.setTag("1");
                    iv_complaint_dropdown_follow_up_action.setRotation(180);
                    ll_complaint_follow_up_action.setVisibility(View.VISIBLE);
                } else {
                    iv_complaint_dropdown_follow_up_action.setTag("0");
                    iv_complaint_dropdown_follow_up_action.setRotation(0);
                    ll_complaint_follow_up_action.setVisibility(View.GONE);
                }
            }
        });

        iv_non_complaint_dropdown_follow_up_action.setTag("0");
        iv_non_complaint_dropdown_follow_up_action.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_non_complaint_dropdown_follow_up_action.getTag().equals("0")) {
                    iv_non_complaint_dropdown_follow_up_action.setTag("1");
                    iv_non_complaint_dropdown_follow_up_action.setRotation(180);
                    ll_non_complaint_follow_up_action.setVisibility(View.VISIBLE);
                } else {
                    iv_non_complaint_dropdown_follow_up_action.setTag("0");
                    iv_non_complaint_dropdown_follow_up_action.setRotation(0);
                    ll_non_complaint_follow_up_action.setVisibility(View.GONE);
                }
            }
        });

        iv_satisfactory_dropdown_follow_up_action.setTag("0");
        iv_satisfactory_dropdown_follow_up_action.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_satisfactory_dropdown_follow_up_action.getTag().equals("0")) {
                    iv_satisfactory_dropdown_follow_up_action.setTag("1");
                    iv_satisfactory_dropdown_follow_up_action.setRotation(180);
                    ll_satisfactory_follow_up_action.setVisibility(View.VISIBLE);
                } else {
                    iv_satisfactory_dropdown_follow_up_action.setTag("0");
                    iv_satisfactory_dropdown_follow_up_action.setRotation(0);
                    ll_satisfactory_follow_up_action.setVisibility(View.GONE);
                }
            }
        });

        ll_servey_next_followup_actionplan_save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!ViewFarmerActivity.recyclerViewData.contains("Follow Up 1")) {
                    ViewFarmerActivity.recyclerViewData.add("Follow Up 1");

                    Intent intent = new Intent(getContext(),ViewFarmerActivity.class);
                    getContext().startActivity(intent);
                }else {
//                    Toast.makeText(getContext(), "Can't add more followup", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}