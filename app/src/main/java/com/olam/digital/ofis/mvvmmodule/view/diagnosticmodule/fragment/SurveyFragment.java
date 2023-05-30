package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    LinearLayout ll_servey_next, ll_servey_back, dm_questionsModule, dm_Section,dm_questionsSubModule , dm_questionsSubModuleTextBox, dm_questionsModuleTextBox;
    OFISRoomDB db;
    public LayoutInflater inflater;
    Resources resources;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        ll_servey_next = view.findViewById(R.id.ll_servey_next);

        //ll_servey_back = view.findViewById(R.id.ll_servey_back);
        dm_questionsModule = view.findViewById(R.id.dm_questionsModule);
        dm_Section = view.findViewById(R.id.dm_Section);
        dm_questionsSubModule = view.findViewById(R.id.dm_questionsSubModule);
        dm_questionsModuleTextBox = view.findViewById(R.id.dm_questionsModuleTextBox);
        dm_questionsSubModuleTextBox = view.findViewById(R.id.dm_questionsSubModuleTextBox);


//        ll_servey_back = view.findViewById(R.id.ll_servey_back);

        db = OFISRoomDB.getDbInstance(getActivity());


        List<DmSectionsItem> dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsItem();
        displaySectionItems(dmSectionsItemList);

        List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModule();
        displayQuestionsModuleItems(dmQuestionsModuleList);



        return view;
    }

    private void displayQuestionItems(int section, List<DmQuestionsSubModule> dmQuestionsSubModuleList) {

        for (int j = 0; j < dmQuestionsSubModuleList.size(); j++) {

            DmQuestionsSubModule dmQuestionsSubModule = dmQuestionsSubModuleList.get(j);
            TextView textView = new TextView(getActivity());
            textView.setText(section + "." + (j + 1) + "." + dmQuestionsSubModule.question);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            textView.setTypeface(null, Typeface.BOLD);
            if(j==0){
                dm_questionsSubModule.addView(textView);

            }else {
                dm_questionsSubModuleTextBox.addView(textView);
            }
//            dm_questionsSubModule.addView(textView);

            if (dmQuestionsSubModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesISubmodule> dmLookupValuesISubmoduleList = db.dmLookupValuesISubmoduleDao().getDmAnswersSubModuleByQuestionId(dmQuestionsSubModule.id);
                renderDropDownQuestion(dmLookupValuesISubmoduleList);
            }
            else if(dmQuestionsSubModule.displayType.equalsIgnoreCase("text box"))
            {
                renderTextboxQuestion();
            }

        }

    }

    private void displaySectionItems(List<DmSectionsItem> dmSectionsItemList) {
        for (int i = 0; i < dmSectionsItemList.size(); i++) {
            DmSectionsItem dmSectionsItem = dmSectionsItemList.get(i);
            TextView textView = new TextView(getActivity());
            textView.setText(dmSectionsItem.name);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            //textView.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);
            List<DmQuestionsSubModule> dmQuestionsSubModuleList = db.dmQuestionsSubModuleDao().getDmQuestionsSubModulebySectionId(dmSectionsItem.id);
            displayQuestionItems(i + 1, dmQuestionsSubModuleList);

        }
    }

    private void displayQuestionsModuleItems(List<DmQuestionsModule> dmQuestionsModuleList){
        for (int m=0;m<dmQuestionsModuleList.size();m++) {
            DmQuestionsModule dmQuestionsModule = dmQuestionsModuleList.get(m);
            TextView textView = new TextView(getActivity());
            textView.setText((m + 1)+ "." +dmQuestionsModule.question);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            textView.setPadding(0, 15, 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            if(m==0) {
                dm_questionsModule.addView(textView);
            }else {
                dm_questionsModuleTextBox.addView(textView);
            }
//            dm_questionsModule.addView(textView);

            if (dmQuestionsModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesItem> dmLookupValuesItemList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsModule.id);
                renderDropDownQuestionModule(dmLookupValuesItemList);
            }
            else if(dmQuestionsModule.displayType.equalsIgnoreCase("text box"))
            {
                renderTextboxQuestionModule();
            }

        }


    }

    public void renderDropDownQuestion(List<DmLookupValuesISubmodule> dmLookupValuesISubmoduleList) {


        final List<String> list = new ArrayList<String>();
        list.clear();

        for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
            list.add(dmLookupValuesISubmoduleList.get(k).value);
        }


        final Spinner spinner = new Spinner(getActivity());
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add Spinner to LinearLayout
        if (dm_questionsSubModule != null) {
            dm_questionsSubModule.addView(spinner);
        }


    }


    public void renderTextboxQuestion() {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setPadding(0,20,0,20);
        editText.setTextSize(14);

        //editText.getLayoutParams().width=40;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }
        // Add Spinner to LinearLayout
        if (dm_questionsSubModule != null) {
            dm_questionsSubModuleTextBox.addView(editText);
        }


    }

    public void renderDropDownQuestionModule(List<DmLookupValuesItem> dmLookupValuesItemList ) {


        final List<String> list = new ArrayList<String>();
        list.clear();

        for (int k = 0; k < dmLookupValuesItemList.size(); k++) {
            list.add(dmLookupValuesItemList.get(k).value);
        }


        final Spinner spinner = new Spinner(getActivity());


//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner,relativeLayout,false);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add Spinner to LinearLayout
        if (dm_questionsModule != null) {
            dm_questionsModule.addView(spinner);
        }


    }


    public void renderTextboxQuestionModule() {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setPadding(0,20,0,20);
        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (dm_questionsModule != null) {
            dm_questionsModuleTextBox.addView(editText);
        }


    }





}


