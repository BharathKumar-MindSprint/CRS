package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SurveyFragment extends Fragment {
    LinearLayout ll_servey_next,ll_servey_back, dm_questionsSection,dm_Section;
    OFISRoomDB db;
    public LayoutInflater inflater;
    Resources resources;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_survey, container, false);

        ll_servey_next = view.findViewById(R.id.ll_servey_next);
        ll_servey_back = view.findViewById(R.id.ll_servey_back);
        dm_questionsSection = view.findViewById(R.id.dm_questionsSection);
        dm_Section = view.findViewById(R.id.dm_Section);

         db = OFISRoomDB.getDbInstance(getActivity());
//        List<DmQuestionsSubModule> dmQuestionsModuleList = db.dmQuestionsSubModuleDao().getDmQuestionsSubModule();
//        displayItems(dmQuestionsModuleList);
//        dmQuestionsModuleList.clear();

        List<DmSectionsItem> dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsItem();
        displayItems(dmSectionsItemList);

        return view;
    }

    private void displayQuestionItems(int section ,List<DmQuestionsSubModule> dmQuestionsSubModuleList){

        for (int j=0;j<dmQuestionsSubModuleList.size();j++ ) {

            DmQuestionsSubModule dmQuestionsSubModule = dmQuestionsSubModuleList.get(j);
            TextView textView = new TextView(getActivity());
            textView.setText(section+"."+(j+1)+"."+dmQuestionsSubModule.question);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(15);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);

        }

    }

    private void displayAnswerItems(String type,int section ,List<DmLookupValuesISubmodule> dmLookupValuesISubmoduleList,LinearLayout parentLayout,final Resources res){

        if(type=="dropdown")
        {
            final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);

            resources = res;


//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
            final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_item, res.getStringArray(R.array.plotvariants));
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            Cursor spinnervalues;

//            // get display config
//            try {
//                JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
//                final String columnName = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;
//
//                if (displayConfig.has("lookuptable")) {
//                    if(OFISDBContract.countryLookUpValuesEntry.TABLE_NAME.trim().toLowerCase().equals(displayConfig.getString("lookuptable").trim().toLowerCase())){
//                        spinnervalues = dl.getSpinnerData(OFISDBContract.countryLookUpValuesEntry.TABLE_NAME, false, null, null, true, "CountryName");
//                    }else if(OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(displayConfig.getString("lookuptable").trim().toLowerCase())){
//                        spinnervalues = dl.getSpinnerData(OFISDBContract.productSipEntry.TABLE_NAME, false, null, null, true, "productName");
//
//                    }
//                    else{
//                        spinnervalues = dl.getSpinnerData(displayConfig.getString("lookuptable"), false, null, null, true, "name");
//                    }
//                } else {
//                    spinnervalues = dl.getLookupValuesForQuestion(qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), columnName);
//                }
//
//                final boolean isDensityQuestion = (displayConfig.has("densityquestionparent") && (displayConfig.getBoolean("densityquestionparent")));
//
//                dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);
//
//
//            } catch (JSONException e) {
//                FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error gettign dropdown display config "+e.getMessage());
//            }
        }

        for (int j=0;j<dmLookupValuesISubmoduleList.size();j++ ) {

            DmLookupValuesISubmodule dmLookupValuesItem = dmLookupValuesISubmoduleList.get(j);


        }

    }



    private void displayItems(List<DmSectionsItem> dmSectionsItemList) {
        for (int i=0;i<dmSectionsItemList.size();i++) {
            DmSectionsItem dmSectionsItem = dmSectionsItemList.get(i);
            TextView textView = new TextView(getActivity());
            textView.setText(dmSectionsItem.name);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(15);
            //textView.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);
            List<DmQuestionsSubModule> dmQuestionsSubModuleList = db.dmQuestionsSubModuleDao().getDmQuestionsSubModulebySectionId(dmSectionsItem.id);
            displayQuestionItems(i+1,dmQuestionsSubModuleList);

        }
    }
}


