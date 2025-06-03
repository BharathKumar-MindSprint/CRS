package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.agrisupplier.dto.SupplierModuleResponse;
import com.olam.digital.ofis.agrisupplier.dto.SupplierResponse;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSubmittedModuleResponse;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyAssignments;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyResponse;
import com.olam.digital.ofis.agrisupplier.entity.Supplier;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierSubmittedModules;
import com.olam.digital.ofis.agrisupplier.entity.SupplierType;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ComplianceIndicatorItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmRangeTextBoxActionPlans;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmRangeTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.LookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.TextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmModuleAssignments;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmTextBoxModuleQuestions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateSupplierTask extends DatabaseTask {

    ProgressDialog pd;
    Context context;
    OFISRoomDB roomDB;

    public UpdateSupplierTask(Context context) {
        super(context);
        this.context = context;
        pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);
        roomDB = OFISRoomDB.getDbInstance(context);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {
        try {
            JSONObject supplierResponseObject = params[0];
            JSONObject supplierSurveys = params[1];
            JSONObject supplierSurveySubmittedModulesResponse = params[2];

            if (supplierResponseObject != null) {
                SupplierResponse supplierResponse = new Gson().fromJson(supplierResponseObject.toString(), SupplierResponse.class);
                List<Supplier> supplier = supplierResponse.getSuppliers();
                List<SupplierGroup> supplierGroups = supplierResponse.getSupplierGroup();
                List<SupplierType> supplierTypes = supplierResponse.getSupplierType();

                roomDB.supplierDao().insertSuppliers(supplier);
                roomDB.supplierGroupDao().insertSupplierGroups(supplierGroups);
                roomDB.supplierTypeDao().insertSupplierType(supplierTypes);
            }

            if (supplierSurveys != null) {
                SupplierModuleResponse supplierModuleResponse = new Gson().fromJson(supplierSurveys.toString(), SupplierModuleResponse.class);
                List<SupplierSurveyResponse> supplierSurveyResponse = supplierModuleResponse.getSupplierSurveyModules();

                if (!supplierSurveyResponse.isEmpty()) {
                    for (int i = 0; i < supplierSurveyResponse.size(); i++) {
                        int id = supplierSurveyResponse.get(i).getId();
                        String assessmentType = supplierSurveyResponse.get(i).getAssessmentType();
                        int surveyType = supplierSurveyResponse.get(i).getSurveyType();
                        String name = supplierSurveyResponse.get(i).getName();
                        String nameFr = supplierSurveyResponse.get(i).getNameFr();
                        String nameIdn = supplierSurveyResponse.get(i).getNameIdn();
                        String namePt = supplierSurveyResponse.get(i).getNamePt();
                        String nameLo = supplierSurveyResponse.get(i).getNameLo();
                        String nameTr = supplierSurveyResponse.get(i).getNameTr();
                        String nameTh = supplierSurveyResponse.get(i).getNameTh();
                        String nameVi = supplierSurveyResponse.get(i).getNameVi();
                        String nameEs = supplierSurveyResponse.get(i).getNameEs();
                        String descriptionPt = supplierSurveyResponse.get(i).getDescriptionPt();
                        String description = supplierSurveyResponse.get(i).getDescription();
                        String descriptionVi = supplierSurveyResponse.get(i).getDescriptionVi();
                        String descriptionEs = supplierSurveyResponse.get(i).getDescriptionEs();
                        String descriptionTh = supplierSurveyResponse.get(i).getDescriptionTh();
                        String descriptionLo = supplierSurveyResponse.get(i).getDescriptionLo();
                        String descriptionIdn = supplierSurveyResponse.get(i).getDescriptionIdn();
                        String descriptionTr = supplierSurveyResponse.get(i).getDescriptionTr();
                        String descriptionFr = supplierSurveyResponse.get(i).getDescriptionFr();
                        SupplierSurveyAssignments assignmentsModel = supplierSurveyResponse.get(i).getModuleAssignment();
                        String startDate = assignmentsModel.getStartDate();
                        String captureLocation = supplierSurveyResponse.get(i).getCaptureLocation();
                        Integer supplierType = supplierSurveyResponse.get(i).getSupplierType();
                        String displayLogic = supplierSurveyResponse.get(i).getDisplayLogic();
                        int maxScore = supplierSurveyResponse.get(i).getMaxScore();

                        List<ComplianceIndicatorItem> complianceIndicatorItemList = supplierSurveyResponse.get(i).getComplianceIndicator();
                        List<SectionsItem> sectionsItemList = supplierSurveyResponse.get(i).getSections();

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
                        user.assessmentType = assessmentType;
                        user.captureLocation = captureLocation;
                        user.supplierType = supplierType;
                        user.displayLogic = displayLogic;
                        user.maxScore = maxScore;

                        roomDB.dmMainModuleDao().insert(user);

                        List<DmModuleAssignments> dmModuleAssignments = generateModuleAssignments(assignmentsModel, id);
                        if (!dmModuleAssignments.isEmpty()) {
                            roomDB.dmModuleAssignmentsDao().insertDmModuleAssignMents(dmModuleAssignments);
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

                        DmRecurrence dmRecurrence = new DmRecurrence();
                        dmRecurrence.recurrenceFrequency = assignmentsModel.getRecurrenceFrequency();
                        dmRecurrence.isCustomRecurrence = assignmentsModel.isCustomRecurrence();
                        dmRecurrence.recurrenceType = assignmentsModel.getRecurrenceType();
                        dmRecurrence.moduleId = String.valueOf(id);
                        roomDB.dmRecurrenceDao().insert(dmRecurrence);
                    }
                }
            }

            if (supplierSurveySubmittedModulesResponse != null) {
                SupplierSubmittedModuleResponse supplierSubmittedModuleResponse = new Gson().fromJson(supplierSurveySubmittedModulesResponse.toString(), SupplierSubmittedModuleResponse.class);
                List<SupplierSubmittedModules> supplierSubmittedModules = supplierSubmittedModuleResponse.supplierModuleData;

                roomDB.supplierSubmittedModuleDao().insertSubmittedModule(supplierSubmittedModules);
            }

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("UpdateSupplierResponse: Error Saving Supplier response", e));
            e.printStackTrace();
        }
        return null;
    }

    private List<DmModuleAssignments> generateModuleAssignments(SupplierSurveyAssignments assignmentsModels, int moduleId) {
        List<DmModuleAssignments> dmModuleAssignments = new ArrayList<>();
        String assignmentType = String.valueOf(assignmentsModels.getAssignmentType());

        for (Integer assignmentId : assignmentsModels.getAssignmentValue()) {
            dmModuleAssignments.add(new DmModuleAssignments(moduleId, assignmentType, assignmentId));
        }

        return dmModuleAssignments;
    }
}
