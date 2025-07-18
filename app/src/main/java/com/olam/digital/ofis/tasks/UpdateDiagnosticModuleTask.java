package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AnswerTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AssessmentResultTypeItems;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AssignmentsModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ComplianceIndicatorItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrencyItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModulesList;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmMasterDataModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmRangeTextBoxActionPlans;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmRangeTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.LookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.Recurrence;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.TextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.model.followup.AnswersItem;
import com.olam.digital.ofis.mvvmmodule.model.followup.ExistingModuleItem;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpItem;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpResponseItem;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.followup.Result;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmAssessmentResultType;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMasterData;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmModuleAssignments;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateDiagnosticModuleTask extends DatabaseTask {
    ProgressDialog pd;
    JSONObject dm;
    JSONObject dmFollowUp;
    OFISRoomDB roomDB;
    DataLayer dl;

    private SQLiteDatabase db;
    public UpdateDiagnosticModuleTask(Context context, JSONObject dm, JSONObject dmFollowUpResponse, Resources res, JSONObject farmerResponse) {
        super(context);
        pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);
        this.dm = dm;
        this.dmFollowUp = dmFollowUpResponse;
        roomDB = OFISRoomDB.getDbInstance(context);
        dl = new DataLayer(res, context);
        db = Utilities.getWriteableDb(context);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {
        JSONArray plotsArray = new JSONArray();
        
        try {
            JSONObject farmerResponse = params[0];
            if (farmerResponse.getInt("resultcount") > 0) {
                JSONObject response = farmerResponse.getJSONObject("outputData");
                JSONArray farmerArray = response.getJSONArray(OFISDBContract.FarmerEntry.TABLE_NAME);
               db.delete(OFISDBContract.PlotEntry.TABLE_NAME, null, null);
                for (int j = 0; j < farmerArray.length(); j++) {
                    JSONObject farmerObject = farmerArray.getJSONObject(j);
                    JSONArray plotArray = farmerObject.getJSONArray(OFISDBContract.PlotEntry.TABLE_NAME);
                    for (int i = 0; i < plotArray.length(); i++) {
                        JSONObject plotObject = plotArray.getJSONObject(i);
                        plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID,farmerObject.get("id"));
                        plotObject.put(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED,"1");
                        plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED, "false");
                        plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED, "false");
                        plotsArray.put(plotObject);
                    }
                }
            }
            
            if(plotsArray.length() > 0 ) {
                fillTable(plotsArray, OFISDBContract.PlotEntry.TABLE_NAME, OFISDBContract.PlotEntry.COLUMN_LIST);
            }

        }catch (Exception e) {
            Log.d("Plots", "Error in Plot Response");
            e.printStackTrace();
        }
        
        Map<Boolean, List<Integer>> submittedPlots = new HashMap<>();
        if (dm != null) {
            try {
                int diagnosticType = 0;
                int eudrGroundVerificationSurveyId = 0;
                ResponseModel dmResponse = new Gson().fromJson(dm.toString(), ResponseModel.class);
                if (dmResponse.getDmMasterData() != null) {
                    for (DmMasterDataModel dmMasterDataModel : dmResponse.getDmMasterData()) {
                        DmMasterData dmMasterData = new DmMasterData();
                        dmMasterData.setId(dmMasterDataModel.getId());
                        dmMasterData.setType(dmMasterDataModel.getType());
                        dmMasterData.setValue(dmMasterDataModel.getValue());

                        if (dmMasterDataModel.getValue().equalsIgnoreCase("Plot")) {
                            diagnosticType = dmMasterDataModel.getId();
                        }

                        if (dmMasterDataModel.getValue().equalsIgnoreCase(Utilities.EUDR_GROUND_VERIFICATION_SURVEY)) {
                            eudrGroundVerificationSurveyId = dmMasterDataModel.getId();
                        }

                        roomDB.dmMasterDataDao().insert(dmMasterData);
                    }
                }

                if (dmResponse.getDiagnosticModules() != null) {
                    ArrayList<DiagnosticModulesList> mainResponseList = dmResponse.getDiagnosticModules();
                    for (int i = 0; i < mainResponseList.size(); i++) {
                        int id = mainResponseList.get(i).getId();
                        int followUpDuration = mainResponseList.get(i).getFollowUpDuration();
                        String followUpDurationVal = mainResponseList.get(i).getFollowUpDurationVal();
                        String assessmentType = mainResponseList.get(i).getAssessmentType();
                        int surveyType = mainResponseList.get(i).getSurveyType();
                        int followUpAttempts = mainResponseList.get(i).getFollowUpAttempts();
                        String followUpAttemptsVal = mainResponseList.get(i).getFollowUpAttemptsVal();
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
                        String startDate = mainResponseList.get(i).getStartDate();
                        String displayLogic = mainResponseList.get(i).getDisplayLogic();
                        int maxScore = mainResponseList.get(i).getMaxScore();
                        Integer diagnosticTypeId = mainResponseList.get(i).getDiagnosticTypeId();
                        Integer plotSurveyType = mainResponseList.get(i).getPlotSurveyType();
                        List<Integer> submittedPlotIds = mainResponseList.get(i).getSubmittedPlotIds();
                        String captureLocation = mainResponseList.get(i).getCaptureLocation();
                        List<AssignmentsModel> assignmentsModel = mainResponseList.get(i).getAssignments();
                        List<AssessmentResultTypeItems> assessmentResultTypeItemsList = mainResponseList.get(i).getAssessmentResultType();
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
                        user.startDate = startDate;

                        user.surveyType = surveyType;
                        user.followUpDuration = followUpDuration;
                        user.assessmentType = assessmentType;
                        user.followUpAttempts = followUpAttempts;
                        user.displayLogic = displayLogic;
                        user.maxScore = maxScore;
                        user.followUpAttemptsVal = followUpAttemptsVal;
                        user.followUpDurationVal = followUpDurationVal;
                        user.diagnosticTypeId = diagnosticTypeId;
                        user.plotSurveyType = plotSurveyType;
                        user.captureLocation = captureLocation;

                        roomDB.dmMainModuleDao().insert(user);

                        if (diagnosticTypeId != null && diagnosticTypeId == diagnosticType && !submittedPlotIds.isEmpty()) {
                            submittedPlots.put(eudrGroundVerificationSurveyId == plotSurveyType, submittedPlotIds);
                        }

                        List<DmModuleAssignments> dmModuleAssignments = generateModuleAssignments(assignmentsModel, id);
                        if (!dmModuleAssignments.isEmpty()) {
                            roomDB.dmModuleAssignmentsDao().insertDmModuleAssignMents(dmModuleAssignments);
                        }

                        if (assessmentResultTypeItemsList != null) {
                            for (int a = 0; a<assessmentResultTypeItemsList.size();a++){
                                int minRange = assessmentResultTypeItemsList.get(a).getMinRange();
                                int maxRange = assessmentResultTypeItemsList.get(a).getMaxRange();
                                String remarks = assessmentResultTypeItemsList.get(a).getRemarks();

                                DmAssessmentResultType dmAssessmentResultType = new DmAssessmentResultType();
                                dmAssessmentResultType.moduleId = id;
                                dmAssessmentResultType.minRange = minRange;
                                dmAssessmentResultType.maxRange = maxRange;
                                dmAssessmentResultType.remarks = remarks;

                                roomDB.dmAssessmentResultTypeDao().insert(dmAssessmentResultType);
                            }
                        }

                        if (complianceIndicatorItemList != null) {
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
                                int score = complianceIndicatorItemList.get(j).getScore();
                                boolean isActionPlanRequired = complianceIndicatorItemList.get(j).isActionPlanRequired();
                                boolean allowCustomActionPlan = complianceIndicatorItemList.get(j).isAllowCustomActionPlan();

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
                                complianceIndicator.score = score;
                                complianceIndicator.isActionPlanRequired = isActionPlanRequired;
                                complianceIndicator.allowCustomActionPlan = allowCustomActionPlan;

                                roomDB.complianceIndicatorDao().insert(complianceIndicator);
                            }
                        }

                        if (currencyItemList != null) {
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

                                roomDB.dmCurrencyDao().insert(dmCurrency);
                            }
                        }

                        if (questionsItemList != null) {
                            for (int l = 0; l < questionsItemList.size(); l++) {
                                boolean mandate = questionsItemList.get(l).isMandate();
                                Integer maxSelection = questionsItemList.get(l).getMaxSelection();
                                String question = questionsItemList.get(l).getQuestion();
                                String questionTr = questionsItemList.get(l).getQuestionTr();
                                String questionPt = questionsItemList.get(l).getQuestionPt();
                                String questionLo = questionsItemList.get(l).getQuestionLo();
                                String questionFr = questionsItemList.get(l).getQuestionFr();
                                String questionIdn = questionsItemList.get(l).getQuestionIdn();
                                String questionEs = questionsItemList.get(l).getQuestionEs();
                                String questionTh = questionsItemList.get(l).getQuestionTh();
                                String questionVi = questionsItemList.get(l).getQuestionVi();
                                int displayTypeId = questionsItemList.get(l).getDisplaytypeId();
                                boolean isCriticalQuest = questionsItemList.get(l).isCriticalQuest();
                                String displayType = questionsItemList.get(l).getDisplayType();
                                int id_questionModule = questionsItemList.get(l).getId();
                                int position = questionsItemList.get(l).getPosition();
                                int moduleId = questionsItemList.get(l).getModuleId();
                                int submoduleId = questionsItemList.get(l).getSubmoduleId();

                                DmQuestionsModule dmQuestionsModule = new DmQuestionsModule();
                                dmQuestionsModule.mandate = mandate;
                                dmQuestionsModule.maxSelection = maxSelection;
                                dmQuestionsModule.question = question;
                                dmQuestionsModule.questionTr = questionTr;
                                dmQuestionsModule.questionPt = questionPt;
                                dmQuestionsModule.questionLo = questionLo;
                                dmQuestionsModule.questionFr = questionFr;
                                dmQuestionsModule.questionIdn = questionIdn;
                                dmQuestionsModule.questionEs = questionEs;
                                dmQuestionsModule.questionTh = questionTh;
                                dmQuestionsModule.questionVi = questionVi;
                                dmQuestionsModule.displaytypeId = displayTypeId;
                                dmQuestionsModule.isCriticalQuest = isCriticalQuest;
                                dmQuestionsModule.displayType = displayType;
                                dmQuestionsModule.id = id_questionModule;
                                dmQuestionsModule.position = position;
                                dmQuestionsModule.moduleId = moduleId;
                                dmQuestionsModule.submoduleId = submoduleId;
                                dmQuestionsModule.sectionId = 0;
                                roomDB.dmQuestionsModuleDao().insert(dmQuestionsModule);

                                List<LookupValuesItem> lookupValuesItemList = questionsItemList.get(l).getLookupValues();
                                TextBoxModuleQuestions textBoxModuleQuestions = questionsItemList.get(l).getTextBoxModuleQuestions();

                                if (lookupValuesItemList != null) {
                                    for (int m = 0; m < lookupValuesItemList.size(); m++) {
                                        String valueId = lookupValuesItemList.get(m).getValueId();
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

                                        roomDB.dmLookupValuesItemDao().insert(dmLookupValuesItem);

                                        List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList = lookupValuesItemList.get(m).getActionPlans();
                                        if (dmRangeTextBoxActionPlansList != null) {
                                            for (int n = 0; n < dmRangeTextBoxActionPlansList.size(); n++) {
                                                DmRangeActionPlansItems dmRangeActionPlansItems = new DmRangeActionPlansItems();

                                                dmRangeActionPlansItems.id = dmRangeTextBoxActionPlansList.get(n).getId();
                                                dmRangeActionPlansItems.isDefault = dmRangeTextBoxActionPlansList.get(n).isDefault();
                                                dmRangeActionPlansItems.actionPlan = dmRangeTextBoxActionPlansList.get(n).getActionPlan();
                                                dmRangeActionPlansItems.actionValueId = dmRangeTextBoxActionPlansList.get(n).getActionValueId();
                                                dmRangeActionPlansItems.questionId = id_questionModule;
                                                dmRangeActionPlansItems.complainceId = complianceId;
                                                dmRangeActionPlansItems.actionPlanFr = dmRangeTextBoxActionPlansList.get(n).getActionPlanFr();
                                                dmRangeActionPlansItems.actionPlanEs = dmRangeTextBoxActionPlansList.get(n).getActionPlanEs();
                                                dmRangeActionPlansItems.actionPlanPt = dmRangeTextBoxActionPlansList.get(n).getActionPlanPt();
                                                dmRangeActionPlansItems.actionPlanId = dmRangeTextBoxActionPlansList.get(n).getActionPlanId();
                                                dmRangeActionPlansItems.actionPlanLo = dmRangeTextBoxActionPlansList.get(n).getActionPlanLo();
                                                dmRangeActionPlansItems.actionPlanTh = dmRangeTextBoxActionPlansList.get(n).getActionPlanTh();
                                                dmRangeActionPlansItems.actionPlanTr = dmRangeTextBoxActionPlansList.get(n).getActionPlanTr();
                                                dmRangeActionPlansItems.actionPlanVi = dmRangeTextBoxActionPlansList.get(n).getActionPlanVi();

                                                roomDB.dmRangeActionPlansItemsDao().insert(dmRangeActionPlansItems);
                                            }
                                        }
                                    }
                                }

                                if (textBoxModuleQuestions != null) {
                                    int answerType = textBoxModuleQuestions.getAnswerType();
                                    boolean isAllowNegative = textBoxModuleQuestions.isAllowNegative();
                                    boolean isAllowDecimal = textBoxModuleQuestions.isAllowDecimal();
                                    double minimumRange = textBoxModuleQuestions.getMinimumRange();
                                    double maximumRange = textBoxModuleQuestions.getMaximumRange();

                                    DmTextBoxModuleQuestions dmTextBoxModuleQuestions = new DmTextBoxModuleQuestions();
                                    dmTextBoxModuleQuestions.answerType = answerType;
                                    dmTextBoxModuleQuestions.isAllowNegative = isAllowNegative;
                                    dmTextBoxModuleQuestions.isAllowDecimal = isAllowDecimal;
                                    dmTextBoxModuleQuestions.minimumRange = minimumRange;
                                    dmTextBoxModuleQuestions.maximumRange = maximumRange;
                                    dmTextBoxModuleQuestions.questionId = id_questionModule;
                                    roomDB.dmTextBoxModuleQuestionsDao().insert(dmTextBoxModuleQuestions);

                                    List<DmRangeTextBoxModuleQuestions> dmRangeTextBoxModuleQuestionsList = textBoxModuleQuestions.getDmRangeQuestionsList();
                                    if (dmRangeTextBoxModuleQuestionsList != null) {
                                        for (int r = 0; r < dmRangeTextBoxModuleQuestionsList.size(); r++) {
                                            Integer id_range = dmRangeTextBoxModuleQuestionsList.get(r).getId();
                                            double minimumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMinimumRange();
                                            double maximumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMaximumRange();
                                            String compliance = dmRangeTextBoxModuleQuestionsList.get(r).getCompliance();
                                            int complianceId = dmRangeTextBoxModuleQuestionsList.get(r).getComplianceId();
                                            boolean allowCustomActionPlan = dmRangeTextBoxModuleQuestionsList.get(r).isAllowCustomActionPlan();
                                            boolean isCaptureProof = dmRangeTextBoxModuleQuestionsList.get(r).isCaptureProof();
                                            String proofFormat = dmRangeTextBoxModuleQuestionsList.get(r).getProofFormat();

                                            DmRangeTextBoxModuleQuestionsTable dmRangeTextBoxModuleQuestionsTable = new DmRangeTextBoxModuleQuestionsTable();
                                            dmRangeTextBoxModuleQuestionsTable.id = id_range;
                                            dmRangeTextBoxModuleQuestionsTable.minimumRange = minimumRange_range;
                                            dmRangeTextBoxModuleQuestionsTable.maximumRange = maximumRange_range;
                                            dmRangeTextBoxModuleQuestionsTable.compliance = compliance;
                                            dmRangeTextBoxModuleQuestionsTable.complianceId = complianceId;
                                            dmRangeTextBoxModuleQuestionsTable.allowCustomActionPlan = allowCustomActionPlan;
                                            dmRangeTextBoxModuleQuestionsTable.isCaptureProof = isCaptureProof;
                                            dmRangeTextBoxModuleQuestionsTable.proofFormat = proofFormat;
                                            dmRangeTextBoxModuleQuestionsTable.questionId = id_questionModule;
                                            roomDB.dmRangeTextBoxModuleQuestionsTableDao().insert(dmRangeTextBoxModuleQuestionsTable);

                                            List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList = dmRangeTextBoxModuleQuestionsList.get(r).getDmRangeTextBoxActionPlansList();
                                            if (dmRangeTextBoxActionPlansList != null) {
                                                for (int ra = 0; ra < dmRangeTextBoxActionPlansList.size(); ra++) {
                                                    DmRangeActionPlansItems dmRangeActionPlansItems = new DmRangeActionPlansItems();

                                                    dmRangeActionPlansItems.id = dmRangeTextBoxActionPlansList.get(ra).getId();
                                                    dmRangeActionPlansItems.isDefault = dmRangeTextBoxActionPlansList.get(ra).isDefault();
                                                    dmRangeActionPlansItems.actionPlan = dmRangeTextBoxActionPlansList.get(ra).getActionPlan();
                                                    dmRangeActionPlansItems.actionValueId = dmRangeTextBoxActionPlansList.get(ra).getActionValueId();
                                                    dmRangeActionPlansItems.questionId = id_questionModule;
                                                    dmRangeActionPlansItems.complainceId = complianceId;
                                                    dmRangeActionPlansItems.actionPlanFr = dmRangeTextBoxActionPlansList.get(ra).getActionPlanFr();
                                                    dmRangeActionPlansItems.actionPlanEs = dmRangeTextBoxActionPlansList.get(ra).getActionPlanEs();
                                                    dmRangeActionPlansItems.actionPlanPt = dmRangeTextBoxActionPlansList.get(ra).getActionPlanPt();
                                                    dmRangeActionPlansItems.actionPlanId = dmRangeTextBoxActionPlansList.get(ra).getActionPlanId();
                                                    dmRangeActionPlansItems.actionPlanLo = dmRangeTextBoxActionPlansList.get(ra).getActionPlanLo();
                                                    dmRangeActionPlansItems.actionPlanTh = dmRangeTextBoxActionPlansList.get(ra).getActionPlanTh();
                                                    dmRangeActionPlansItems.actionPlanTr = dmRangeTextBoxActionPlansList.get(ra).getActionPlanTr();
                                                    dmRangeActionPlansItems.actionPlanVi = dmRangeTextBoxActionPlansList.get(ra).getActionPlanVi();

                                                    roomDB.dmRangeActionPlansItemsDao().insert(dmRangeActionPlansItems);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (sectionsItemList != null) {
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

                                roomDB.dmSectionsItemDao().insert(dmSectionsItem);

                                List<QuestionsItem> questionsItems = sectionsItemList.get(s).getQuestions();
                                if (questionsItems != null) {
                                    for (int l = 0; l < questionsItems.size(); l++) {
                                        boolean mandate = questionsItems.get(l).isMandate();
                                        Integer maxSelection = questionsItems.get(l).getMaxSelection();
                                        String question = questionsItems.get(l).getQuestion();
                                        String questionTr = questionsItems.get(l).getQuestionTr();
                                        String questionPt = questionsItems.get(l).getQuestionPt();
                                        String questionLo = questionsItems.get(l).getQuestionLo();
                                        String questionFr = questionsItems.get(l).getQuestionFr();
                                        String questionIdn = questionsItems.get(l).getQuestionIdn();
                                        String questionEs = questionsItems.get(l).getQuestionEs();
                                        String questionTh = questionsItems.get(l).getQuestionTh();
                                        String questionVi = questionsItems.get(l).getQuestionVi();
                                        int displayTypeId = questionsItems.get(l).getDisplaytypeId();
                                        boolean isCriticalQuest = questionsItems.get(l).isCriticalQuest();
                                        String displayType = questionsItems.get(l).getDisplayType();
                                        int id_questionModule = questionsItems.get(l).getId();
                                        int position = questionsItems.get(l).getPosition();
                                        int moduleId = questionsItems.get(l).getModuleId();
                                        int submoduleId = questionsItems.get(l).getSubmoduleId();

                                        DmQuestionsModule dmQuestionsModule = new DmQuestionsModule();
                                        dmQuestionsModule.mandate = mandate;
                                        dmQuestionsModule.maxSelection = maxSelection;
                                        dmQuestionsModule.question = question;
                                        dmQuestionsModule.questionTr = questionTr;
                                        dmQuestionsModule.questionPt = questionPt;
                                        dmQuestionsModule.questionLo = questionLo;
                                        dmQuestionsModule.questionFr = questionFr;
                                        dmQuestionsModule.questionIdn = questionIdn;
                                        dmQuestionsModule.questionEs = questionEs;
                                        dmQuestionsModule.questionTh = questionTh;
                                        dmQuestionsModule.questionVi = questionVi;
                                        dmQuestionsModule.displaytypeId = displayTypeId;
                                        dmQuestionsModule.isCriticalQuest = isCriticalQuest;
                                        dmQuestionsModule.displayType = displayType;
                                        dmQuestionsModule.id = id_questionModule;
                                        dmQuestionsModule.position = position;
                                        dmQuestionsModule.moduleId = moduleId;
                                        dmQuestionsModule.submoduleId = submoduleId;
                                        dmQuestionsModule.sectionId = id_Sec;

                                        roomDB.dmQuestionsModuleDao().insert(dmQuestionsModule);

                                        List<LookupValuesItem> lookupValuesItemList = questionsItems.get(l).getLookupValues();
                                        TextBoxModuleQuestions textBoxModuleQuestions = questionsItems.get(l).getTextBoxModuleQuestions();

                                        if (lookupValuesItemList != null) {
                                            for (int ls = 0; ls < lookupValuesItemList.size(); ls++) {
                                                String valueId = lookupValuesItemList.get(ls).getValueId();
                                                String valueTr = lookupValuesItemList.get(ls).getValueTr();
                                                String valueVi = lookupValuesItemList.get(ls).getValueVi();
                                                String valueTh = lookupValuesItemList.get(ls).getValueTh();
                                                String valueEs = lookupValuesItemList.get(ls).getValueEs();
                                                String valueFr = lookupValuesItemList.get(ls).getValueFr();
                                                String valueLo = lookupValuesItemList.get(ls).getValueLo();
                                                String valuePt = lookupValuesItemList.get(ls).getValuePt();
                                                String value = lookupValuesItemList.get(ls).getValue();
                                                int position_lookupValues = lookupValuesItemList.get(ls).getPosition();
                                                int id_lookupValues = lookupValuesItemList.get(ls).getId();
                                                boolean allowCustomActionPlan = lookupValuesItemList.get(ls).isAllowCustomActionPlan();
                                                String compliance = lookupValuesItemList.get(ls).getCompliance();
                                                int complianceId = lookupValuesItemList.get(ls).getComplianceId();
                                                boolean isCaptureProof = lookupValuesItemList.get(ls).isCaptureProof();
                                                String proofFormat = lookupValuesItemList.get(ls).getProofFormat();

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

                                                roomDB.dmLookupValuesItemDao().insert(dmLookupValuesItem);

                                                List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList = lookupValuesItemList.get(ls).getActionPlans();
                                                if (dmRangeTextBoxActionPlansList != null) {
                                                    for (int lsa = 0; lsa < dmRangeTextBoxActionPlansList.size(); lsa++) {
                                                        DmRangeActionPlansItems dmRangeActionPlansItems = new DmRangeActionPlansItems();
                                                        dmRangeActionPlansItems.id = dmRangeTextBoxActionPlansList.get(lsa).getId();
                                                        dmRangeActionPlansItems.isDefault = dmRangeTextBoxActionPlansList.get(lsa).isDefault();
                                                        dmRangeActionPlansItems.actionPlan = dmRangeTextBoxActionPlansList.get(lsa).getActionPlan();
                                                        dmRangeActionPlansItems.actionValueId = dmRangeTextBoxActionPlansList.get(lsa).getActionValueId();
                                                        dmRangeActionPlansItems.questionId = id_questionModule;
                                                        dmRangeActionPlansItems.complainceId = complianceId;
                                                        dmRangeActionPlansItems.actionPlanFr = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanFr();
                                                        dmRangeActionPlansItems.actionPlanEs = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanEs();
                                                        dmRangeActionPlansItems.actionPlanPt = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanPt();
                                                        dmRangeActionPlansItems.actionPlanId = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanId();
                                                        dmRangeActionPlansItems.actionPlanLo = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanLo();
                                                        dmRangeActionPlansItems.actionPlanTh = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanTh();
                                                        dmRangeActionPlansItems.actionPlanTr = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanTr();
                                                        dmRangeActionPlansItems.actionPlanVi = dmRangeTextBoxActionPlansList.get(lsa).getActionPlanVi();

                                                        roomDB.dmRangeActionPlansItemsDao().insert(dmRangeActionPlansItems);
                                                    }
                                                }
                                            }
                                        }

                                        if (textBoxModuleQuestions != null) {
                                            int answerType = textBoxModuleQuestions.getAnswerType();
                                            boolean isAllowNegative = textBoxModuleQuestions.isAllowNegative();
                                            boolean isAllowDecimal = textBoxModuleQuestions.isAllowDecimal();
                                            double minimumRange = textBoxModuleQuestions.getMinimumRange();
                                            double maximumRange = textBoxModuleQuestions.getMaximumRange();

                                            DmTextBoxModuleQuestions dmTextBoxModuleQuestions = new DmTextBoxModuleQuestions();
                                            dmTextBoxModuleQuestions.answerType = answerType;
                                            dmTextBoxModuleQuestions.isAllowNegative = isAllowNegative;
                                            dmTextBoxModuleQuestions.isAllowDecimal = isAllowDecimal;
                                            dmTextBoxModuleQuestions.minimumRange = minimumRange;
                                            dmTextBoxModuleQuestions.maximumRange = maximumRange;
                                            dmTextBoxModuleQuestions.questionId = id_questionModule;
                                            roomDB.dmTextBoxModuleQuestionsDao().insert(dmTextBoxModuleQuestions);

                                            List<DmRangeTextBoxModuleQuestions> dmRangeTextBoxModuleQuestionsList = textBoxModuleQuestions.getDmRangeQuestionsList();
                                            if (dmRangeTextBoxModuleQuestionsList != null) {
                                                for (int r = 0; r < dmRangeTextBoxModuleQuestionsList.size(); r++) {
                                                    Integer id_range = dmRangeTextBoxModuleQuestionsList.get(r).getId();
                                                    double minimumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMinimumRange();
                                                    double maximumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMaximumRange();
                                                    String compliance = dmRangeTextBoxModuleQuestionsList.get(r).getCompliance();
                                                    int complianceId = dmRangeTextBoxModuleQuestionsList.get(r).getComplianceId();
                                                    boolean allowCustomActionPlan = dmRangeTextBoxModuleQuestionsList.get(r).isAllowCustomActionPlan();
                                                    boolean isCaptureProof = dmRangeTextBoxModuleQuestionsList.get(r).isCaptureProof();
                                                    String proofFormat = dmRangeTextBoxModuleQuestionsList.get(r).getProofFormat();

                                                    DmRangeTextBoxModuleQuestionsTable dmRangeTextBoxModuleQuestionsTable = new DmRangeTextBoxModuleQuestionsTable();
                                                    dmRangeTextBoxModuleQuestionsTable.id = id_range;
                                                    dmRangeTextBoxModuleQuestionsTable.minimumRange = minimumRange_range;
                                                    dmRangeTextBoxModuleQuestionsTable.maximumRange = maximumRange_range;
                                                    dmRangeTextBoxModuleQuestionsTable.compliance = compliance;
                                                    dmRangeTextBoxModuleQuestionsTable.complianceId = complianceId;
                                                    dmRangeTextBoxModuleQuestionsTable.allowCustomActionPlan = allowCustomActionPlan;
                                                    dmRangeTextBoxModuleQuestionsTable.isCaptureProof = isCaptureProof;
                                                    dmRangeTextBoxModuleQuestionsTable.proofFormat = proofFormat;
                                                    dmRangeTextBoxModuleQuestionsTable.questionId = id_questionModule;

                                                    roomDB.dmRangeTextBoxModuleQuestionsTableDao().insert(dmRangeTextBoxModuleQuestionsTable);

                                                    List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList = dmRangeTextBoxModuleQuestionsList.get(r).getDmRangeTextBoxActionPlansList();
                                                    if (dmRangeTextBoxActionPlansList != null) {
                                                        for (int ra = 0; ra < dmRangeTextBoxActionPlansList.size(); ra++) {
                                                            DmRangeActionPlansItems dmRangeActionPlansItems = new DmRangeActionPlansItems();
                                                            dmRangeActionPlansItems.id = dmRangeTextBoxActionPlansList.get(ra).getId();
                                                            dmRangeActionPlansItems.isDefault = dmRangeTextBoxActionPlansList.get(ra).isDefault();
                                                            dmRangeActionPlansItems.actionPlan = dmRangeTextBoxActionPlansList.get(ra).getActionPlan();
                                                            dmRangeActionPlansItems.actionValueId = dmRangeTextBoxActionPlansList.get(ra).getActionValueId();
                                                            dmRangeActionPlansItems.questionId = id_questionModule;
                                                            dmRangeActionPlansItems.complainceId = complianceId;
                                                            dmRangeActionPlansItems.actionPlanFr = dmRangeTextBoxActionPlansList.get(ra).getActionPlanFr();
                                                            dmRangeActionPlansItems.actionPlanEs = dmRangeTextBoxActionPlansList.get(ra).getActionPlanEs();
                                                            dmRangeActionPlansItems.actionPlanPt = dmRangeTextBoxActionPlansList.get(ra).getActionPlanPt();
                                                            dmRangeActionPlansItems.actionPlanId = dmRangeTextBoxActionPlansList.get(ra).getActionPlanId();
                                                            dmRangeActionPlansItems.actionPlanLo = dmRangeTextBoxActionPlansList.get(ra).getActionPlanLo();
                                                            dmRangeActionPlansItems.actionPlanTh = dmRangeTextBoxActionPlansList.get(ra).getActionPlanTh();
                                                            dmRangeActionPlansItems.actionPlanTr = dmRangeTextBoxActionPlansList.get(ra).getActionPlanTr();
                                                            dmRangeActionPlansItems.actionPlanVi = dmRangeTextBoxActionPlansList.get(ra).getActionPlanVi();

                                                            roomDB.dmRangeActionPlansItemsDao().insert(dmRangeActionPlansItems);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (recurrences != null) {
                            int recurrenceFrequency = recurrences.getRecurrenceFrequency();
                            boolean isCustomRecurrence = recurrences.isIsCustomRecurrence();
                            String recurrenceType = recurrences.getRecurrenceType();

                            DmRecurrence dmRecurrence = new DmRecurrence();
                            dmRecurrence.recurrenceFrequency = recurrenceFrequency;
                            dmRecurrence.isCustomRecurrence = isCustomRecurrence;
                            dmRecurrence.recurrenceType = recurrenceType;
                            dmRecurrence.moduleId = String.valueOf(id);
                            roomDB.dmRecurrenceDao().insert(dmRecurrence);
                        }
                    }
                }

                if (dmResponse.getAnswerTypes() != null) {
                    ArrayList<AnswerTypesItem> answerTypesItemArrayList = dmResponse.getAnswerTypes();
                    for (int p = 0; p < answerTypesItemArrayList.size(); p++) {
                        int id = answerTypesItemArrayList.get(p).getId();
                        String value = answerTypesItemArrayList.get(p).getValue();

                        DmAnswerTypes dmAnswerTypes = new DmAnswerTypes();
                        dmAnswerTypes.id = id;
                        dmAnswerTypes.value = value;

                        roomDB.dmAnswerTypesDao().insert(dmAnswerTypes);
                    }
                }

                if (dmResponse.getQuestionTypes() != null) {
                    ArrayList<QuestionTypesItem> questionTypesItemArrayList = dmResponse.getQuestionTypes();
                    for (int q = 0; q < questionTypesItemArrayList.size(); q++) {
                        int id = questionTypesItemArrayList.get(q).getId();
                        String value = questionTypesItemArrayList.get(q).getValue();

                        DmQuestionTypes dmQuestionTypes = new DmQuestionTypes();
                        dmQuestionTypes.id = id;
                        dmQuestionTypes.value = value;

                        roomDB.dmQuestionTypesDao().insert(dmQuestionTypes);
                    }
                }

                if (dmResponse.getCurrencies() != null) {
                    ArrayList<CurrenciesItem> currenciesItemArrayList = dmResponse.getCurrencies();
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

                        roomDB.dmCurrenciesItemDao().insert(dmCurrenciesItem);
                    }
                }
                
                if (plotsArray.length() > 0 && !submittedPlots.isEmpty()) {
                    dl.updateSurveyCapturedForPlots(submittedPlots);
                    dl.insertOnGroundSurveyNotificationData();
                }
                
            } catch (Exception e) {
                Log.d("Diagnostic Module", "Error in Dm Response");
                e.printStackTrace();
            }
        }
        
        if (dmFollowUp != null) {
            try {
                FollowUpResponseModel dmFollowUpResponse = new Gson().fromJson(dmFollowUp.toString(), FollowUpResponseModel.class);
                ArrayList<FollowUpResponseItem> followUpResponseItemList = dmFollowUpResponse.getFollowUpResponseItemList();
                if (followUpResponseItemList != null) {
                    for (int i = 0; i < followUpResponseItemList.size(); i++) {
                        ArrayList<FollowUpItem> followUpItemArrayList = followUpResponseItemList.get(i).getFollowUpItemArrayList();
                        ArrayList<ExistingModuleItem> existingModuleItemArrayList = followUpResponseItemList.get(i).getExistingModuleItemArrayList();

                        if (followUpItemArrayList!=null){
                            for (int j = 0; j < followUpItemArrayList.size(); j++) {
                                int questionId = followUpItemArrayList.get(j).getQuestionId();
                                int id = followUpItemArrayList.get(j).getId();
                                int complianceId = followUpItemArrayList.get(j).getComplianceId();
                                int moduleId = followUpItemArrayList.get(j).getModuleId();
                                String actionPlanId = followUpItemArrayList.get(j).getActionPlanId();
                                int dmSubModId = followUpItemArrayList.get(j).getDmSubModId();
                                int attempt = followUpItemArrayList.get(j).getAttempt();
                                long farmerId = followUpItemArrayList.get(j).getFarmerId();
                                String actionPlanText = followUpItemArrayList.get(j).getActionPlanText();
                                String createdAt = followUpItemArrayList.get(j).getCreatedAt();
                                String createdBy = followUpItemArrayList.get(j).getCreatedBy();
                                List<String> captureProofList = followUpItemArrayList.get(j).getDmCapturedProof();
                                String status = followUpItemArrayList.get(j).getStatus().trim();
                                String updatedAt = followUpItemArrayList.get(j).getUpdatedAt();
                                String updatedBy = followUpItemArrayList.get(j).getUpdatedBy();
                                boolean isPortal = followUpItemArrayList.get(j).isPortal();

                                FollowUpItemMainTable followUpItemMainTable = new FollowUpItemMainTable();
                                followUpItemMainTable.actionPlanId = actionPlanId;
                                followUpItemMainTable.questionId = questionId;
                                followUpItemMainTable.id = id;
                                followUpItemMainTable.complianceId = complianceId;
                                followUpItemMainTable.moduleId = moduleId;
                                followUpItemMainTable.dmSubModId = dmSubModId;
                                followUpItemMainTable.attempt = attempt;
                                followUpItemMainTable.actionPlanText = actionPlanText;
                                followUpItemMainTable.createdAt = createdAt;
                                followUpItemMainTable.createdBy = createdBy;
                                followUpItemMainTable.status = status;
                                followUpItemMainTable.updatedAt = updatedAt;
                                followUpItemMainTable.updatedBy = updatedBy;
                                followUpItemMainTable.isPortal = isPortal;
                                followUpItemMainTable.farmerId = farmerId;
                                followUpItemMainTable.isSync = true;
                                followUpItemMainTable.dmCapturedProof = captureProofList != null ? captureProofList.size() : 0;

                                roomDB.followUpItemMainTableDao().insert(followUpItemMainTable);
                            }
                        }

                        if (existingModuleItemArrayList!= null){
                            for (int k = 0; k < existingModuleItemArrayList.size(); k++) {
                                int farmerId = existingModuleItemArrayList.get(k).getFarmerId();
                                int submittedModuleId = existingModuleItemArrayList.get(k).getId();
                                String submissionDate = existingModuleItemArrayList.get(k).getSubmissionDate();
                                int farmerGroupId = existingModuleItemArrayList.get(k).getFarmerGroupId();
                                int moduleId = existingModuleItemArrayList.get(k).getModuleId();
                                int currencyId = existingModuleItemArrayList.get(k).getCurrencyId();
                                ArrayList<AnswersItem> answersItemArrayList = new ArrayList<>();
                                answersItemArrayList = existingModuleItemArrayList.get(k).getAnswers();
                                Result result = existingModuleItemArrayList.get(k).getResult();

                                for (int l = 0; l < answersItemArrayList.size(); l++) {
                                    int answerId = answersItemArrayList.get(l).getAnswerId();
                                    int complianceStatusId = answersItemArrayList.get(l).getComplianceStatusId();
                                    int questionId = answersItemArrayList.get(l).getQuestionId();
                                    String answerValue = answersItemArrayList.get(l).getAnswerValue();
                                    String captureProofs = answersItemArrayList.get(l).getCaptureProofs();
                                    int actionPlanId = answersItemArrayList.get(l).getActionPlanId();
                                    String actionPlanText = answersItemArrayList.get(l).getActionPlanText();
                                    String displayType = answersItemArrayList.get(l).getDisplayType();

                                    DmSurveyAnswerTable dmSurveyAnswerTable = new DmSurveyAnswerTable();
                                    dmSurveyAnswerTable.answerID = String.valueOf(answerId);
                                    dmSurveyAnswerTable.farmerId = String.valueOf(farmerId);
                                    dmSurveyAnswerTable.moduleId = String.valueOf(moduleId);
                                    dmSurveyAnswerTable.submittedModuleId = submittedModuleId;
                                    dmSurveyAnswerTable.complinceTypeId = String.valueOf(complianceStatusId);
                                    dmSurveyAnswerTable.questionID = String.valueOf(questionId);
                                    dmSurveyAnswerTable.answer = answerValue;
                                    dmSurveyAnswerTable.actionplanId = String.valueOf(actionPlanId);
                                    dmSurveyAnswerTable.actionPlanText = actionPlanText;
                                    dmSurveyAnswerTable.isSynced = true;
                                    dmSurveyAnswerTable.plotId = 0;
                                    dmSurveyAnswerTable.question = roomDB.dmQuestionsModuleDao().getQuestionById(questionId);
                                    dmSurveyAnswerTable.displayType = displayType;

                                    roomDB.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable);
                                }

                                String[] totalBudget = result.getTotalBudget().split(" ");
                                DMSurveyReport dmSurveyReport = new DMSurveyReport(result.getTotalComplaint(),
                                        result.getTotalSatisfactory(),
                                        result.getTotalNonComplaint(),
                                        result.getTotalActionPlan(),
                                        Double.parseDouble(totalBudget[0].trim()),
                                        result.getResultType(),
                                        result.getResultValue(),
                                        result.getResultRemarks(),
                                        farmerId,moduleId,
                                        farmerGroupId,
                                        currencyId,
                                        submissionDate,
                                        result.getResultColour(),
                                        result.getOverAllResult(), 0);
                                roomDB.dmSurveyReportDao().insert(dmSurveyReport);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("Diagnostic Module", "Error in DM FollowUp Response");
                e.printStackTrace();
            }
        }

        return null;
    }

    private List<DmModuleAssignments> generateModuleAssignments(List<AssignmentsModel> assignmentsModels, int moduleId) {
        List<DmModuleAssignments> dmModuleAssignments = new ArrayList<>();

        for (AssignmentsModel assignmentsModel : assignmentsModels) {
            String assignmentType = assignmentsModel.getAssignmentType();

            for (int id : assignmentsModel.getAssignmentId()) {
                dmModuleAssignments.add(new DmModuleAssignments(moduleId, assignmentType, id));
            }
        }

        return dmModuleAssignments;
    }

}
