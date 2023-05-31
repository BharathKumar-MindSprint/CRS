package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AnswerTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ComplianceIndicatorItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrencyItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModulesList;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.LookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsSectionItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.Recurrence;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionLookupValuesItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.adapter.PagerAdapter;
import com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule.DiagnosticModuleRepository;
import com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule.DiagnosticViewModel;


import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DiagnosticModuleActivity extends BaseActivity {
    private DiagnosticModuleRepository diagnosticModuleRepository;
    private DiagnosticViewModel diagnosticViewModel;

    public TabLayout tabLayout;
    ViewPager2 viewPager;
    TextView tv_subtitle;
    private ProgressDialog progressDialog;
    private String[] titles = new String[]{"Survey", "Action plan and proof", "Survey Results"};
    OFISRoomDB db;
    List<DiagnosticModulesList> dMmoduleList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_module);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));
        progressDialog.show();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tv_subtitle = findViewById(R.id.tv_subtitle);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("SUB_TITLE");
            tv_subtitle.setText(value);
        }

        PagerAdapter sa = new PagerAdapter(this);
        viewPager.setAdapter(sa);
        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> {
            tab.setText(titles[position]);
            //tab.view.setClickable(false);

        })).attach();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        //
        diagnosticModuleRepository = new DiagnosticModuleRepository(getApplication());

        diagnosticViewModel = ViewModelProviders.of(this).get(DiagnosticViewModel.class);
        GetAllDiagnosticModuleNew();

     /*   db = OFISRoomDB.getDbInstance(this.getApplicationContext());
        dMmoduleList =db.DiagnosticModuleDao().getDMmodule();

        Log.d("DiagnosticRoomResult", "diagnostic room db:: " + dMmoduleList.toString());*/


    }

    public void goLastTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab - 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);

    }

    public void goNextTab(View view) {
        int currentTab = viewPager.getCurrentItem();
        int nextTab = currentTab + 1;
        if (nextTab >= viewPager.getAdapter().getItemCount()) {
            nextTab = 0;
        }
        viewPager.setCurrentItem(nextTab);
    }


    /*private void GetAllDiagnosticModule() {
        Toast.makeText(this, "ResponseMethod", Toast.LENGTH_SHORT).show();

        diagnosticViewModel.responseLiveData().observe(this, responseLiveData -> {
            if (responseLiveData != null) {
                Toast.makeText(this, "ResponseMethodINN", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
                progressDialog.hide();

            }
        });

    }*/

    public void GetAllDiagnosticModuleNew() {

        ManageRecords AllDiagnosticModuleData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<ResponseModel> call = AllDiagnosticModuleData.getAllDiagnosticModuleData(1);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                OFISRoomDB db = OFISRoomDB.getDbInstance(DiagnosticModuleActivity.this);


                if (response.code() == 200) {
                    //Toast.makeText(DownloadFarmPlanActivity.this, "Farm Plan is available to download", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Toast.makeText(DiagnosticModuleActivity.this, "ResponseMethodINN", Toast.LENGTH_SHORT).show();
                    Toast.makeText(DiagnosticModuleActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    Log.d("DiagnosticActivity", "diagnostic total result:: " + response.body());
                    // Log.d("DiagnosticActivity", "diagnostic valuesHome:: " +  response.body().getDiagnosticModules().get(0).getAnswerTypes());

                    db.dmSectionsItemDao().deleteAll();
                    db.dmQuestionsSubModuleDao().deleteAll();
                    db.dmLookupValuesISubmoduleDao().deleteAll();
                    if (response.body().getDiagnosticModules() != null) {
                        ArrayList<DiagnosticModulesList> mainResponseList = response.body().getDiagnosticModules();

                        for (int i = 0; i < mainResponseList.size(); i++) {
                            int id = mainResponseList.get(i).getId();
                            int followUpDuration = mainResponseList.get(i).getFollowUpDuration();
                            String assessmentType = mainResponseList.get(i).getAssessmentType();
                            int surveyType = mainResponseList.get(i).getSurveyType();
                            int followUpAttempts = mainResponseList.get(i).getFollowUpAttempts();
                            String name = mainResponseList.get(i).getName();
                            String nameFr = mainResponseList.get(i).getNameFr();
                            String nameIdn = mainResponseList.get(i).getNameIdn();
                            String namePt = mainResponseList.get(i).getNamePt();
                            String nameLo = mainResponseList.get(i).getNameLo();
                            String nameTr = mainResponseList.get(i).getNameTr();
                            String nameTh = mainResponseList.get(i).getNameTh();
                            String nameVi = mainResponseList.get(i).getNameVi();
                            String nameEs = mainResponseList.get(i).getNameEs();
                            String descriptionPt = mainResponseList.get(i).getDescriptionPt();
                            String description = mainResponseList.get(i).getDescription();
                            String descriptionVi = mainResponseList.get(i).getDescriptionVi();
                            String descriptionEs = mainResponseList.get(i).getDescriptionEs();
                            String descriptionTh = mainResponseList.get(i).getDescriptionTh();
                            String descriptionLo = mainResponseList.get(i).getDescriptionLo();
                            String descriptionIdn = mainResponseList.get(i).getDescriptionIdn();
                            String descriptionTr = mainResponseList.get(i).getDescriptionTr();
                            String descriptionFr = mainResponseList.get(i).getDescriptionFr();

                            List<ComplianceIndicatorItem> complianceIndicatorItemList = mainResponseList.get(i).getComplianceIndicator();
                            List<CurrencyItem> currencyItemList = mainResponseList.get(i).getCurrency();
                            List<QuestionsItem> questionsItemList = mainResponseList.get(i).getQuestions();
                            List<SectionsItem> sectionsItemList = mainResponseList.get(i).getSections();
                            Recurrence recurrences = mainResponseList.get(i).getRecurrence();

                            DmMainModule user = new DmMainModule();
                            user.id = id;
                            user.name = name;
                            user.nameFr = nameFr;
                            user.nameIdn = nameIdn;
                            user.namePt = namePt;
                            user.nameLo = nameLo;
                            user.nameTr = nameTr;
                            user.nameTh = nameTh;
                            user.nameVi = nameVi;
                            user.nameEs = nameEs;
                            user.descriptionPt = descriptionPt;
                            user.description = description;
                            user.descriptionVi = descriptionVi;
                            user.descriptionEs = descriptionEs;
                            user.descriptionTh = descriptionTh;
                            user.descriptionLo = descriptionLo;
                            user.descriptionIdn = descriptionIdn;
                            user.descriptionTr = descriptionTr;
                            user.descriptionFr = descriptionFr;

                            user.surveyType = surveyType;
                            user.followUpDuration = followUpDuration;
                            user.assessmentType = assessmentType;
                            user.followUpAttempts = followUpAttempts;

                            db.dmMainModuleDao().insert(user);

                            for (int j = 0; j < complianceIndicatorItemList.size(); j++) {


                                int moduleDraftId = complianceIndicatorItemList.get(j).getModuleDraftId();
                                int id_Compliance = complianceIndicatorItemList.get(j).getId();
                                String compliance = complianceIndicatorItemList.get(j).getCompliance();
                                String label = complianceIndicatorItemList.get(j).getLabel();
                                String labelTh = complianceIndicatorItemList.get(j).getLabelTh();
                                String labelPt = complianceIndicatorItemList.get(j).getLabelPt();
                                String labelIdn = complianceIndicatorItemList.get(j).getLabelIdn();
                                String labelEs = complianceIndicatorItemList.get(j).getLabelEs();
                                String labelTr = complianceIndicatorItemList.get(j).getLabelTr();
                                String labelFr = complianceIndicatorItemList.get(j).getLabelFr();
                                String labelLo = complianceIndicatorItemList.get(j).getLabelLo();
                                String labelVi = complianceIndicatorItemList.get(j).getLabelVi();


                                ComplianceIndicator complianceIndicator = new ComplianceIndicator();
                                complianceIndicator.moduleDraftId = moduleDraftId;
                                complianceIndicator.id = id_Compliance;
                                complianceIndicator.compliance = compliance;
                                complianceIndicator.label = label;
                                complianceIndicator.labelTh = labelTh;
                                complianceIndicator.labelPt = labelPt;
                                complianceIndicator.labelIdn = labelIdn;
                                complianceIndicator.labelEs = labelEs;
                                complianceIndicator.labelTr = labelTr;
                                complianceIndicator.labelFr = labelFr;
                                complianceIndicator.labelLo = labelLo;
                                complianceIndicator.labelVi = labelVi;

                                db.complianceIndicatorDao().insert(complianceIndicator);
                            }

                            for (int k = 0; k < currencyItemList.size(); k++) {

                                int moduleDraftId = currencyItemList.get(k).getModuleDraftId();
                                int currencyDraftId = currencyItemList.get(k).getCurrencyDraftId();
                                int currencyId = currencyItemList.get(k).getCurrencyId();
                                boolean isDefault = currencyItemList.get(k).isIsDefault();

                                DmCurrency dmCurrency = new DmCurrency();
                                dmCurrency.moduleDraftId = moduleDraftId;
                                dmCurrency.currencyDraftId = currencyDraftId;
                                dmCurrency.currencyId = currencyId;
                                dmCurrency.isDefault = isDefault;

                                db.dmCurrencyDao().insert(dmCurrency);

                            }

                            db.dmQuestionsModuleDao().deleteAll();
                            db.dmLookupValuesItemDao().deleteAll();
                            db.dmActionPlansItemDao().deleteAll();

                            for (int l = 0; l < questionsItemList.size(); l++) {

                                boolean mandate = questionsItemList.get(l).isMandate();
                                String textBox = questionsItemList.get(l).getTextBox();
                                String question = questionsItemList.get(l).getQuestion();
                                String questionTr = questionsItemList.get(l).getQuestionTr();
                                String questionPt = questionsItemList.get(l).getQuestionPt();
                                String questionLo = questionsItemList.get(l).getQuestionLo();
                                String questionFr = questionsItemList.get(l).getQuestionFr();
                                String questionIdn = questionsItemList.get(l).getQuestionIdn();
                                String questionEs = questionsItemList.get(l).getQuestionEs();
                                String questionTh = questionsItemList.get(l).getQuestionTh();
                                String questionVi = questionsItemList.get(l).getQuestionVi();
                                int questionTypeId = questionsItemList.get(l).getQuestionTypeId();
                                int datatypeId = questionsItemList.get(l).getDatatypeId();
                                boolean isCriticalQuest = questionsItemList.get(l).isCriticalQuest();
                                String displayType = questionsItemList.get(l).getDisplayType();
                                int id_questionModule = questionsItemList.get(l).getId();
                                int position = questionsItemList.get(l).getPosition();
                                int moduleId = questionsItemList.get(l).getModuleId();
                                int submoduleId = questionsItemList.get(l).getSubmoduleId();

                                DmQuestionsModule dmQuestionsModule = new DmQuestionsModule();
                                dmQuestionsModule.mandate = mandate;
                                dmQuestionsModule.textBox = textBox;
                                dmQuestionsModule.question = question;
                                dmQuestionsModule.questionTr = questionTr;
                                dmQuestionsModule.questionPt = questionPt;
                                dmQuestionsModule.questionLo = questionLo;
                                dmQuestionsModule.questionFr = questionFr;
                                dmQuestionsModule.questionIdn = questionIdn;
                                dmQuestionsModule.questionEs = questionEs;
                                dmQuestionsModule.questionTh = questionTh;
                                dmQuestionsModule.questionVi = questionVi;
                                dmQuestionsModule.questionTypeId = questionTypeId;
                                dmQuestionsModule.datatypeId = datatypeId;
                                dmQuestionsModule.isCriticalQuest = isCriticalQuest;
                                dmQuestionsModule.displayType = displayType;
                                dmQuestionsModule.id = id_questionModule;
                                dmQuestionsModule.position = position;
                                dmQuestionsModule.moduleId = moduleId;
                                dmQuestionsModule.submoduleId = submoduleId;
                                db.dmQuestionsModuleDao().insert(dmQuestionsModule);


                                List<LookupValuesItem> lookupValuesItemList = questionsItemList.get(l).getLookupValues();

                                if (lookupValuesItemList != null) {
                                    for (int m = 0; m < lookupValuesItemList.size(); m++) {

                                        int valueId = lookupValuesItemList.get(m).getValueId();
                                        String valueTr = lookupValuesItemList.get(m).getValueTr();
                                        String valueVi = lookupValuesItemList.get(m).getValueVi();
                                        String valueTh = lookupValuesItemList.get(m).getValueTh();
                                        String valueEs = lookupValuesItemList.get(m).getValueEs();
                                        String valueFr = lookupValuesItemList.get(m).getValueFr();
                                        String valueLo = lookupValuesItemList.get(m).getValueLo();
                                        String valuePt = lookupValuesItemList.get(m).getValuePt();
                                        String value = lookupValuesItemList.get(m).getValue();
                                        int position_lookupValues = lookupValuesItemList.get(m).getPosition();
                                        int id_lookupValues = lookupValuesItemList.get(m).getId();
                                        boolean allowCustomActionPlan = lookupValuesItemList.get(m).isAllowCustomActionPlan();
                                        String compliance = lookupValuesItemList.get(m).getCompliance();
                                        int complianceId = lookupValuesItemList.get(m).getComplianceId();
                                        boolean isCaptureProof = lookupValuesItemList.get(m).isCaptureProof();
                                        String proofFormat = lookupValuesItemList.get(m).getProofFormat();

                                        DmLookupValuesItem dmLookupValuesItem = new DmLookupValuesItem();
                                        dmLookupValuesItem.valueId = valueId;
                                        dmLookupValuesItem.valueTr = valueTr;
                                        dmLookupValuesItem.valueVi = valueVi;
                                        dmLookupValuesItem.valueTh = valueTh;
                                        dmLookupValuesItem.valueEs = valueEs;
                                        dmLookupValuesItem.valueFr = valueFr;
                                        dmLookupValuesItem.valueLo = valueLo;
                                        dmLookupValuesItem.valuePt = valuePt;
                                        dmLookupValuesItem.value = value;
                                        dmLookupValuesItem.position = position_lookupValues;
                                        dmLookupValuesItem.id = id_lookupValues;
                                        dmLookupValuesItem.idQuestionsMudule = id_questionModule;
                                        dmLookupValuesItem.allowCustomActionPlan = allowCustomActionPlan;
                                        dmLookupValuesItem.compliance = compliance;
                                        dmLookupValuesItem.complianceId = complianceId;
                                        dmLookupValuesItem.isCaptureProof = isCaptureProof;
                                        dmLookupValuesItem.proofFormat = proofFormat;

                                        db.dmLookupValuesItemDao().insert(dmLookupValuesItem);


                                        List<ActionPlansItem> actionPlansItemList = lookupValuesItemList.get(m).getActionPlans();

                                        if (actionPlansItemList != null) {
                                            for (int n = 0; n < actionPlansItemList.size(); n++) {
                                                int id_actionPlan = actionPlansItemList.get(n).getId();
                                                boolean isDefault = actionPlansItemList.get(n).isIsDefault();
                                                String actionPlan = actionPlansItemList.get(n).getActionPlan();

                                                DmActionPlansItem dmActionPlansItem = new DmActionPlansItem();
                                                dmActionPlansItem.id = id_actionPlan;
                                                dmActionPlansItem.isDefault = isDefault;
                                                dmActionPlansItem.actionPlan = actionPlan;

                                                db.dmActionPlansItemDao().insert(dmActionPlansItem);
                                            }
                                        }


                                    }
                                }

                            }


                            for (int s = 0; s < sectionsItemList.size(); s++) {
                                String nameTh_Sec = sectionsItemList.get(s).getNameTh();
                                String nameVi_Sec = sectionsItemList.get(s).getNameVi();
                                String namePt_Sec = sectionsItemList.get(s).getNamePt();
                                String nameLo_Sec = sectionsItemList.get(s).getNameLo();
                                String nameEs_Sec = sectionsItemList.get(s).getNameEs();
                                String nameFr_Sec = sectionsItemList.get(s).getNameFr();
                                String nameTr_Sec = sectionsItemList.get(s).getNameTr();
                                String name_Sec = sectionsItemList.get(s).getName();
                                String nameId_Sec = sectionsItemList.get(s).getNameId();
                                String description_Sec = sectionsItemList.get(s).getDescription();
                                String descriptionPt_Sec = sectionsItemList.get(s).getDescriptionPt();
                                String descriptionVi_Sec = sectionsItemList.get(s).getDescriptionVi();
                                String descriptionFr_Sec = sectionsItemList.get(s).getDescriptionFr();
                                String descriptionEs_Sec = sectionsItemList.get(s).getDescriptionEs();
                                String descriptionTh_Sec = sectionsItemList.get(s).getDescriptionTh();
                                String descriptionLo_Sec = sectionsItemList.get(s).getDescriptionLo();
                                String descriptionIdn_Sec = sectionsItemList.get(s).getDescriptionIdn();
                                String descriptionTr_Sec = sectionsItemList.get(s).getDescriptionTr();
                                boolean isDefault_Sec = sectionsItemList.get(s).isDefault();
                                boolean showDescription_Sec = sectionsItemList.get(s).isShowDescription();
                                int id_Sec = sectionsItemList.get(s).getId();
                                int position_Sec = sectionsItemList.get(s).getPosition();
                                int moduleId_Sec = sectionsItemList.get(s).getModuleId();

                                DmSectionsItem dmSectionsItem = new DmSectionsItem();
                                dmSectionsItem.nameTh = nameTh_Sec;
                                dmSectionsItem.nameVi = nameVi_Sec;
                                dmSectionsItem.namePt = namePt_Sec;
                                dmSectionsItem.nameLo = nameLo_Sec;
                                dmSectionsItem.nameEs = nameEs_Sec;
                                dmSectionsItem.nameFr = nameFr_Sec;
                                dmSectionsItem.nameTr = nameTr_Sec;
                                dmSectionsItem.name = name_Sec;
                                dmSectionsItem.nameId = nameId_Sec;
                                dmSectionsItem.description = description_Sec;
                                dmSectionsItem.descriptionPt = descriptionPt_Sec;
                                dmSectionsItem.descriptionVi = descriptionVi_Sec;
                                dmSectionsItem.descriptionFr = descriptionFr_Sec;
                                dmSectionsItem.descriptionEs = descriptionEs_Sec;
                                dmSectionsItem.descriptionTh = descriptionTh_Sec;
                                dmSectionsItem.descriptionLo = descriptionLo_Sec;
                                dmSectionsItem.descriptionIdn = descriptionIdn_Sec;
                                dmSectionsItem.descriptionTr = descriptionTr_Sec;
                                dmSectionsItem.isDefault = isDefault_Sec;
                                dmSectionsItem.showDescription = showDescription_Sec;
                                dmSectionsItem.id = id_Sec;
                                dmSectionsItem.position = position_Sec;
                                dmSectionsItem.moduleId = moduleId_Sec;
                                db.dmSectionsItemDao().insert(dmSectionsItem);

                                List<QuestionsSectionItemSubmodule> questionsSectionItemSubmoduleList = sectionsItemList.get(s).getQuestions();


                                for (int t = 0; t < questionsSectionItemSubmoduleList.size(); t++) {

                                    boolean mandate = questionsItemList.get(t).isMandate();
                                    String textBox = questionsItemList.get(t).getTextBox();
                                    String question = questionsItemList.get(t).getQuestion();
                                    String questionTr = questionsItemList.get(t).getQuestionTr();
                                    String questionPt = questionsItemList.get(t).getQuestionPt();
                                    String questionLo = questionsItemList.get(t).getQuestionLo();
                                    String questionFr = questionsItemList.get(t).getQuestionFr();
                                    String questionIdn = questionsItemList.get(t).getQuestionIdn();
                                    String questionEs = questionsItemList.get(t).getQuestionEs();
                                    String questionTh = questionsItemList.get(t).getQuestionTh();
                                    String questionVi = questionsItemList.get(t).getQuestionVi();
                                    int questionTypeId = questionsItemList.get(t).getQuestionTypeId();
                                    int datatypeId = questionsItemList.get(t).getDatatypeId();
                                    boolean isCriticalQuest = questionsItemList.get(t).isCriticalQuest();
                                    String displayType = questionsItemList.get(t).getDisplayType();
                                    int id_questionSubModule = questionsItemList.get(t).getId();
                                    int position = questionsItemList.get(t).getPosition();
                                    int moduleId = questionsItemList.get(t).getModuleId();
                                    int submoduleId = questionsItemList.get(t).getSubmoduleId();

                                    DmQuestionsSubModule dmQuestionsSubModule = new DmQuestionsSubModule();
                                    dmQuestionsSubModule.mandate = mandate;
                                    dmQuestionsSubModule.textBox = textBox;
                                    dmQuestionsSubModule.question = question;
                                    dmQuestionsSubModule.questionTr = questionTr;
                                    dmQuestionsSubModule.questionPt = questionPt;
                                    dmQuestionsSubModule.questionLo = questionLo;
                                    dmQuestionsSubModule.questionFr = questionFr;
                                    dmQuestionsSubModule.sectionId = id_Sec;
                                    dmQuestionsSubModule.questionIdn = questionIdn;
                                    dmQuestionsSubModule.questionEs = questionEs;
                                    dmQuestionsSubModule.questionTh = questionTh;
                                    dmQuestionsSubModule.questionVi = questionVi;
                                    dmQuestionsSubModule.questionTypeId = questionTypeId;
                                    dmQuestionsSubModule.datatypeId = datatypeId;
                                    dmQuestionsSubModule.isCriticalQuest = isCriticalQuest;
                                    dmQuestionsSubModule.displayType = displayType;
                                    dmQuestionsSubModule.id = id_questionSubModule;
                                    dmQuestionsSubModule.position = position;
                                    dmQuestionsSubModule.moduleId = moduleId;
                                    dmQuestionsSubModule.submoduleId = submoduleId;
                                    db.dmQuestionsSubModuleDao().insert(dmQuestionsSubModule);

                                    List<SectionLookupValuesItemSubmodule> sectionLookupValuesItemSubmoduleList = questionsSectionItemSubmoduleList.get(t).getLookupValues();

                                    if (sectionLookupValuesItemSubmoduleList != null) {
                                        for (int u = 0; u < sectionLookupValuesItemSubmoduleList.size(); u++) {

                                            int valueId = sectionLookupValuesItemSubmoduleList.get(u).getValueId();
                                            String valueTr = sectionLookupValuesItemSubmoduleList.get(u).getValueTr();
                                            String valueVi = sectionLookupValuesItemSubmoduleList.get(u).getValueVi();
                                            String valueTh = sectionLookupValuesItemSubmoduleList.get(u).getValueTh();
                                            String valueEs = sectionLookupValuesItemSubmoduleList.get(u).getValueEs();
                                            String valueFr = sectionLookupValuesItemSubmoduleList.get(u).getValueFr();
                                            String valueLo = sectionLookupValuesItemSubmoduleList.get(u).getValueLo();
                                            String valuePt = sectionLookupValuesItemSubmoduleList.get(u).getValuePt();
                                            String value = sectionLookupValuesItemSubmoduleList.get(u).getValue();
                                            int position_lookupValues = sectionLookupValuesItemSubmoduleList.get(u).getPosition();
                                            int id_lookupValues = sectionLookupValuesItemSubmoduleList.get(u).getId();
                                            boolean allowCustomActionPlan = sectionLookupValuesItemSubmoduleList.get(u).isAllowCustomActionPlan();
                                            String compliance = sectionLookupValuesItemSubmoduleList.get(u).getCompliance();
                                            int complianceId = sectionLookupValuesItemSubmoduleList.get(u).getComplianceId();
                                            boolean isCaptureProof = sectionLookupValuesItemSubmoduleList.get(u).isCaptureProof();
                                            String proofFormat = sectionLookupValuesItemSubmoduleList.get(u).getProofFormat();

                                            DmLookupValuesISubmodule dmLookupValuesISubmodule = new DmLookupValuesISubmodule();
                                            dmLookupValuesISubmodule.valueId = valueId;
                                            dmLookupValuesISubmodule.valueTr = valueTr;
                                            dmLookupValuesISubmodule.valueVi = valueVi;
                                            dmLookupValuesISubmodule.valueTh = valueTh;
                                            dmLookupValuesISubmodule.valueEs = valueEs;
                                            dmLookupValuesISubmodule.valueFr = valueFr;
                                            dmLookupValuesISubmodule.valueLo = valueLo;
                                            dmLookupValuesISubmodule.valuePt = valuePt;
                                            dmLookupValuesISubmodule.value = value;
                                            dmLookupValuesISubmodule.position = position_lookupValues;
                                            dmLookupValuesISubmodule.id = id_lookupValues;
                                            dmLookupValuesISubmodule.idQuestionSubModule = id_questionSubModule;
                                            dmLookupValuesISubmodule.allowCustomActionPlan = allowCustomActionPlan;
                                            dmLookupValuesISubmodule.compliance = compliance;
                                            dmLookupValuesISubmodule.complianceId = complianceId;
                                            dmLookupValuesISubmodule.isCaptureProof = isCaptureProof;
                                            dmLookupValuesISubmodule.proofFormat = proofFormat;

                                            db.dmLookupValuesISubmoduleDao().insert(dmLookupValuesISubmodule);


                                            List<SectionActionPlansItemSubmodule> sectionActionPlansItemSubmoduleList = sectionLookupValuesItemSubmoduleList.get(u).getActionPlans();

                                            if (sectionActionPlansItemSubmoduleList != null) {
                                                for (int v = 0; v < sectionActionPlansItemSubmoduleList.size(); v++) {
                                                    int id_actionPlan = sectionActionPlansItemSubmoduleList.get(v).getId();
                                                    boolean isDefault = sectionActionPlansItemSubmoduleList.get(v).isIsDefault();
                                                    String actionPlan = sectionActionPlansItemSubmoduleList.get(v).getActionPlan();

                                                    DmActionPlansItemSubmodule dmActionPlansItemSubmodule = new DmActionPlansItemSubmodule();
                                                    dmActionPlansItemSubmodule.id = id_actionPlan;
                                                    dmActionPlansItemSubmodule.isDefault = isDefault;
                                                    dmActionPlansItemSubmodule.actionPlan = actionPlan;

                                                    db.dmActionPlansItemSubmoduleDao().insert(dmActionPlansItemSubmodule);
                                                }
                                            }

                                        }
                                    }
                                }

                            }

                            int recurrenceFrequency = recurrences.getRecurrenceFrequency();
                            boolean isCustomRecurrence = recurrences.isIsCustomRecurrence();
                            String recurrenceType = recurrences.getRecurrenceType();

                            DmRecurrence dmRecurrence = new DmRecurrence();
                            dmRecurrence.recurrenceFrequency = recurrenceFrequency;
                            dmRecurrence.isCustomRecurrence = isCustomRecurrence;
                            dmRecurrence.recurrenceType = recurrenceType;
                            db.dmRecurrenceDao().insert(dmRecurrence);

                        }
                    }

                    if (response.body().getAnswerTypes() != null) {
                        ArrayList<AnswerTypesItem> answerTypesItemArrayList = response.body().getAnswerTypes();

                        for (int p = 0; p < answerTypesItemArrayList.size(); p++) {

                            int id = answerTypesItemArrayList.get(p).getId();
                            String value = answerTypesItemArrayList.get(p).getValue();

                            DmAnswerTypes dmAnswerTypes = new DmAnswerTypes();
                            dmAnswerTypes.id = id;
                            dmAnswerTypes.value = value;
                            db.dmAnswerTypesDao().insert(dmAnswerTypes);

                        }

                    }

                    if (response.body().getQuestionTypes() != null) {
                        ArrayList<QuestionTypesItem> questionTypesItemArrayList = response.body().getQuestionTypes();

                        for (int q = 0; q < questionTypesItemArrayList.size(); q++) {

                            int id = questionTypesItemArrayList.get(q).getId();
                            String value = questionTypesItemArrayList.get(q).getValue();

                            DmQuestionTypes dmQuestionTypes = new DmQuestionTypes();
                            dmQuestionTypes.id = id;
                            dmQuestionTypes.value = value;
                            db.dmQuestionTypesDao().insert(dmQuestionTypes);


                        }

                    }

                    db.dmCurrenciesItemDao().deleteAll();

                    if (response.body().getCurrencies() != null) {
                        ArrayList<CurrenciesItem> currenciesItemArrayList = response.body().getCurrencies();

                        for (int r = 0; r < currenciesItemArrayList.size(); r++) {

                            int currencyId = currenciesItemArrayList.get(r).getCurrencyId();
                            String notation = currenciesItemArrayList.get(r).getNotation();
                            String code = currenciesItemArrayList.get(r).getCode();
                            String name = currenciesItemArrayList.get(r).getName();

                            DmCurrenciesItem dmCurrenciesItem = new DmCurrenciesItem();
                            dmCurrenciesItem.currencyId = currencyId;
                            dmCurrenciesItem.code = code;
                            dmCurrenciesItem.notation = notation;
                            dmCurrenciesItem.name = name;
                            db.dmCurrenciesItemDao().insert(dmCurrenciesItem);

                        }


                    }


                    Log.d("DbData-", db.dmMainModuleDao().getDMmodule().get(0).name);
                    Log.d("DbDataCompliance-", db.complianceIndicatorDao().getComplianceIndicator().get(0).compliance);
                    Log.d("DbDataCurrency-", String.valueOf(db.dmCurrencyDao().getDMCurrency().get(0).currencyId));
                    Log.d("DbAnswerTypes-", db.dmAnswerTypesDao().getDmAnswerTypes().get(0).value);
                    Log.d("DbQuestionsType-", db.dmQuestionTypesDao().getDmQuestionTypes().get(0).value);
                    Log.d("DbCurrenciesItem-", db.dmCurrenciesItemDao().getDmCurrenciesItem().get(0).notation);
                    Log.d("DbModuleQuestionsItem-", db.dmQuestionsModuleDao().getDmQuestionsModule().get(0).question);
                    Log.d("DbModuleLookupValues-", db.dmLookupValuesItemDao().getDmLookupValuesItem().get(0).value);
                    Log.d("DbModuleActionPlan-", db.dmActionPlansItemDao().getDmActionPlansItem().get(1).actionPlan);
                    Log.d("DbSectionValues-", db.dmSectionsItemDao().getDmSectionsItem().get(0).description);
                    Log.d("DbSubModuleQuestions-", db.dmQuestionsSubModuleDao().getDmQuestionsSubModule().get(0).displayType);
                    Log.d("DbSubModuleLookup-", db.dmLookupValuesISubmoduleDao().getDmLookupValuesISubmodule().get(0).proofFormat);
                    Log.d("DbSubModuleActionPlan-", db.dmActionPlansItemSubmoduleDao().getDmActionPlansItemSubmodule().get(0).actionPlan);
                    Log.d("DbRecurranceData-", db.dmRecurrenceDao().getDmRecurrence().recurrenceType);

                } else if (response.code() == 400) {

                    Log.d("BadRequest-", response.message());
                } else if (response.code() == 500) {

                } else {
                    // showAlert("Server Issue!!"+response.message().toString());
                }

            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                //progressDialog.hide();
                //Toast.makeText(staticContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

}