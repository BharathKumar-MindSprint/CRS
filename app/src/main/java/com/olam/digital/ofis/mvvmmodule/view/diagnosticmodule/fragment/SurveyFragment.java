package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;

import java.util.ArrayList;
import java.util.List;


public class SurveyFragment extends Fragment {
    LinearLayout ll_servey_next,ll_servey_back, dm_questionsSection;
    OFISRoomDB db;


    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_survey, container, false);

        ll_servey_next = view.findViewById(R.id.ll_servey_next);
        ll_servey_back = view.findViewById(R.id.ll_servey_back);
        dm_questionsSection = view.findViewById(R.id.dm_questionsSection);

         db = OFISRoomDB.getDbInstance(getActivity());
        List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModule();
       /* displayItems(dmQuestionsModuleList);
        dmQuestionsModuleList.clear();*/

        List<DmSectionsItem> dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsItem();
        displaySectionItems(dmSectionsItemList);

        return view;
    }

    private void displaySectionItems(List<DmSectionsItem> dmSectionsItemList){

        for (DmSectionsItem dmSectionsItem : dmSectionsItemList) {
            TextView textView = new TextView(getActivity());
            textView.setText(dmSectionsItem.name);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(15);
            textView.setTypeface(null, Typeface.BOLD);
            dm_questionsSection.addView(textView);

        }

    }

    private void displayItems(List<DmQuestionsModule> dmQuestionsModuleList) {
        for (DmQuestionsModule dmQuestionsModule : dmQuestionsModuleList) {
            TextView textView = new TextView(getActivity());
            textView.setText(dmQuestionsModule.question);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(15);
            //textView.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            dm_questionsSection.addView(textView);
        }
    }
}


