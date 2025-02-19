package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static com.olam.digital.ofis.mvvmmodule.Constants.getUniqueQuestion;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;


public class FollowUpSurvey extends Fragment implements View.OnClickListener {
    LinearLayout dm_question_complaint_followup_survey, ll_servey_back_followup_survey, ll_servey_next_followup_survey;

    OFISRoomDB db;

    ArrayList<String> CoQId = new ArrayList<>();
    HashSet<Integer> totalQuestion = new HashSet<>();
    int capture_count = 0;

    public FollowUpSurvey() {
        // Required empty public constructor
    }

    Set<Integer> questionIdSet = new HashSet<>();
    int quesNo = 1;
    List<Integer> compliantList = new ArrayList<>();
    List<Integer> satisfactoryList = new ArrayList<>();
    List<Integer> nonComplaintList = new ArrayList<>();

    List<Integer> complianceActionId = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_up_survey, container, false);

        dm_question_complaint_followup_survey = view.findViewById(R.id.dm_question_complaint_followup_survey);
        ll_servey_back_followup_survey = view.findViewById(R.id.ll_servey_back_followup_survey);
        ll_servey_next_followup_survey = view.findViewById(R.id.ll_servey_next_followup_survey);
        ll_servey_back_followup_survey.setOnClickListener(this);
        ll_servey_next_followup_survey.setOnClickListener(this);
        return view;
    }

    private void initializeComplianceStatus() {
        questionIdSet.clear();
        compliantList.clear();
        satisfactoryList.clear();
        nonComplaintList.clear();

        if (!db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).isEmpty()) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                String complianceName = db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).compliance;
                if (complianceName.equalsIgnoreCase(Constants.SATISFACTORY)) {
                    satisfactoryList.add(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id);
                } else if (complianceName.equalsIgnoreCase(Constants.COMPLIANT)) {
                    compliantList.add(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id);
                } else if (complianceName.equalsIgnoreCase(Constants.NON_COMPLIANT)) {
                    nonComplaintList.add(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id);
                }
            }


            initializeComplaintLayout(compliantList, Constants.COMPLIANT);
            initializeComplaintLayout(satisfactoryList, Constants.SATISFACTORY);
            initializeComplaintLayout(nonComplaintList, Constants.NON_COMPLIANT);
        }
    }

    private void initializeComplaintLayout(List<Integer> complianceList, String type) {
        quesNo = 1;
        if (complianceList.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View convertView = inflater.inflate(R.layout.complaint_layout, null, false);
        TextView selected_imgName = convertView.findViewById(R.id.tv_complaint);
        ImageView complaint_dropdown = convertView.findViewById(R.id.iv_complaint_dropdown);

        dm_question_complaint_followup_survey.addView(convertView);

        int tagValue = complianceList.get(0);
        String tagPrefix = "1" + tagValue;
        String resourceString = "";

        if (type.equals(Constants.COMPLIANT)) {
            resourceString = getResources().getString(R.string.dm_compliant);
        } else if (type.equals(Constants.SATISFACTORY)) {
            resourceString = getResources().getString(R.string.dm_satisfactory);
        } else if (type.equals(Constants.NON_COMPLIANT)) {
            resourceString = getResources().getString(R.string.dm_non_compliant);
        }

        convertView.setTag(tagValue);
        complaint_dropdown.setTag(tagPrefix);
        selected_imgName.setText(resourceString);

        LinearLayout linearLayoutItems = new LinearLayout(getActivity());
        linearLayoutItems.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayoutItems.setPadding(25, 10, 10, 10);
        linearLayoutItems.setTag("item" + tagValue);
        linearLayoutItems.setOrientation(LinearLayout.VERTICAL);
        linearLayoutItems.setBackground(getActivity().getDrawable(R.drawable.dm_actionplan_module_bg));

        for (Integer compliance : complianceList) {
            displayQuestionsModuleComplaint(String.valueOf(compliance), complaint_dropdown, linearLayoutItems);
        }
    }

    List<DmSurveyAnswerTable> getComplianceCheckboxAnswers(String complianceName, List<Integer> complianceActionId) {

        List<DmSurveyAnswerTable> answers = new ArrayList<>();

        if (complianceName.equals(Constants.COMPLIANT)) {
            answers.addAll(db.dmSurveyAnswerTableDao().getComplaintList(compliantList, satisfactoryList, nonComplaintList, Constants.FARMER_ID, Constants.PLOT_ID, Constants.CHECKBOX));
            answers.addAll(db.dmSurveyAnswerTableDao().getSatisfactoryComplaintList(compliantList, satisfactoryList, nonComplaintList, Constants.FARMER_ID, Constants.PLOT_ID, Constants.CHECKBOX));
        } else if (complianceName.equals(Constants.NON_COMPLIANT)) {
            answers = db.dmSurveyAnswerTableDao().getNonComplaintList(nonComplaintList, Constants.FARMER_ID, Constants.PLOT_ID, Constants.CHECKBOX);
        } else if (complianceName.equals(Constants.SATISFACTORY)) {
            answers = db.dmSurveyAnswerTableDao().getSatisfactoryList(compliantList, nonComplaintList, satisfactoryList, Constants.FARMER_ID, Constants.PLOT_ID, Constants.CHECKBOX);
        }

        List<DmSurveyAnswerTable> checkboxAnswers = new ArrayList<>();
        for (DmSurveyAnswerTable answer : answers) {
            if (complianceActionId.contains(Integer.parseInt(answer. complinceTypeId))) {
                checkboxAnswers.add(answer);
            }
        }

        return checkboxAnswers;
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void displayQuestionsModuleComplaint(String complaint_id, ImageView complaint_dropdown, LinearLayout linearLayoutItems)  {
        boolean isActionPlanRequired = db.complianceIndicatorDao().getIsActionPlanRequired(complaint_id);
        boolean isActionPlanCheckbox = false;
        List<ComplianceIndicator> dmComplaintList = db.complianceIndicatorDao().getComplianceNameById(complaint_id);

        if (!dmComplaintList.isEmpty()) {
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceIdFilterByActionPlan(complaint_id, Constants.FARMER_ID, Constants.PLOT_ID, Constants.CHECKBOX);
            String complianceName = dmComplaintList.get(0).compliance;

            // fetching checkbox answers based on action plan required
            List<DmSurveyAnswerTable> dmSurveyAnswerCheckbox = getComplianceCheckboxAnswers(complianceName, complianceActionId);
            if (!dmSurveyAnswerCheckbox.isEmpty()){
                isActionPlanCheckbox = true;
            }

            boolean finalIsActionPlanCheckbox = isActionPlanCheckbox;
            complaint_dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = complaint_dropdown.getTag().toString();
                    int tagFirst = Integer.parseInt(String.valueOf(tag.charAt(0)));
                    List<Integer> list;

                    if (tagFirst == 0) {
                        // Expand
                        complaint_dropdown.setRotation(180);
                        list = complianceName.equalsIgnoreCase(Constants.SATISFACTORY) ? satisfactoryList :
                                complianceName.equalsIgnoreCase(Constants.COMPLIANT) ? compliantList : nonComplaintList;
                        complaint_dropdown.setTag("1" + list.get(0));
                    } else {
                        // Collapse
                        complaint_dropdown.setRotation(0);
                        list = complianceName.equalsIgnoreCase(Constants.SATISFACTORY) ? satisfactoryList :
                                complianceName.equalsIgnoreCase(Constants.COMPLIANT) ? compliantList : nonComplaintList;
                        complaint_dropdown.setTag("0" + list.get(0));
                    }

                    if(isActionPlanRequired || finalIsActionPlanCheckbox){
                        View desiredView = dm_question_complaint_followup_survey.findViewWithTag("item" + list.get(0));
                        if (desiredView != null) {
                            LinearLayout linearLayout = (LinearLayout) desiredView;
                            if (linearLayout != null) {
                                linearLayout.setVisibility(tagFirst == 0 ? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                }
            });

            if (isActionPlanRequired) {
                renderSurveyQuestions(dmSurveyAnswerTableList, complaint_id, linearLayoutItems);
            } else if (isActionPlanCheckbox) {
                renderSurveyQuestions(dmSurveyAnswerCheckbox, complaint_id, linearLayoutItems);
            }

        }
    }


    private void renderSurveyQuestions(List<DmSurveyAnswerTable> surveyList, String complaintId, LinearLayout linearLayout) {
        if (!surveyList.isEmpty()) {
            String answerIdDb = surveyList.get(0).answerID;
            for (int m = 0; m < surveyList.size(); m++) {
                DmSurveyAnswerTable questionModule = surveyList.get(m);
                if (questionIdSet.add(Integer.valueOf(questionModule.questionID))) {
                    questionModule.question = getNamesbyLanguage(questionModule);
                    TextView textView = new TextView(getActivity());
                    textView.setText(quesNo + "." + questionModule.question);
                    textView.setTextColor(getResources().getColor(R.color.black));
                    textView.setTextSize(14);
                    textView.setPadding(0, 15, 0, 0);
                    textView.setTypeface(null, Typeface.BOLD);
                    linearLayout.addView(textView);
                    renderTextboxQuestionModuleComplaint(String.valueOf(questionModule.questionID), complaintId, linearLayout);
                    quesNo++;
                }
            }
        }
    }
    public String getNamesbyLanguage(DmSurveyAnswerTable dmSurveyAnswerTable){
        List<DmQuestionsModule> dmQuestionsModule = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(dmSurveyAnswerTable.questionID));
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "en" -> dmQuestionsModule.get(0).question;
            case "es" -> dmQuestionsModule.get(0).questionEs;
            case "fr" -> dmQuestionsModule.get(0).questionFr;
            case "id" -> dmQuestionsModule.get(0).questionIdn;
            case "in" -> dmQuestionsModule.get(0).questionIdn;
            case "lo" -> dmQuestionsModule.get(0).questionLo;
            case "pt" -> dmQuestionsModule.get(0).questionPt;
            case "th" -> dmQuestionsModule.get(0).questionTh;
            case "tr" -> dmQuestionsModule.get(0).questionTr;
            case "vi" -> dmQuestionsModule.get(0).questionVi;
            default -> dmQuestionsModule.get(0).question;
        };
    }

    public void renderTextboxQuestionModuleComplaint(String id, String complaint_id, LinearLayout linearLayoutItems) {
        totalQuestion.add(Integer.parseInt(id));
        LinearLayout.LayoutParams layoutParamsQes = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setPadding(10, 10, 0, 10);
        layoutParamsQes.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(layoutParamsQes);
        editText.setTag("et" + complaint_id);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID, Constants.PLOT_ID);

        LinearLayout linearLayout1 = new LinearLayout(getContext());
        linearLayout1.setGravity(Gravity.CENTER);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        if (!dmSurveyAnswerTableList.isEmpty()) {

            String answerIdDb = dmSurveyAnswerTableList.get(0).actionplanId;
            String questionIdDb = dmSurveyAnswerTableList.get(0).questionID;

            if (!Objects.equals(answerIdDb, null)) {
                if (!Objects.equals(answerIdDb, "1") && !Objects.equals(answerIdDb,"0")){
                    DmRangeActionPlansItems answerActionPlan = db.dmRangeActionPlansItemsDao().getActionPlanItem(Integer.parseInt(answerIdDb));
                    editText.setText(Utilities.getActionPlanByLanguage(answerActionPlan));
                } else {
                    editText.setText(dmSurveyAnswerTableList.get(0).actionPlanText);
                }
            }

            TextView notResolveText = new TextView(getContext());
            notResolveText.setText(getResources().getString(R.string.dm_not_resolved));
            notResolveText.setTextColor(getResources().getColor(R.color.gray));
            notResolveText.setTag("rb1" + questionIdDb);
            notResolveText.setPadding(70, 25, 70, 25);
            notResolveText.setTextSize(14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notResolveText.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
            }
            TextView resolveText = new TextView(getContext());
            resolveText.setText(getResources().getString(R.string.dm_resolved));
            resolveText.setTag("rb2" + questionIdDb);
            resolveText.setTextColor(getResources().getColor(R.color.gray));
            resolveText.setPadding(110, 25, 110, 25);
            resolveText.setTextSize(14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resolveText.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            layoutParams.setMargins(10, 20, 10, 20);


            linearLayout1.addView(notResolveText, layoutParams);
            linearLayout1.addView(resolveText, layoutParams);
            List<FollowUpItemMainTable> followupData = db.followUpItemMainTableDao().getFollowupByQuestionId1(questionIdDb, Constants.FARMER_ID, Constants.ATTEMPT);

            if(!followupData.isEmpty()) {
                if (followupData.get(0).status.equalsIgnoreCase(Constants.RESOLVED)) {

                    CoQId.add(dmSurveyAnswerTableList.get(0).questionID);
                    Constants.questionIdsFollowUp.add(dmSurveyAnswerTableList.get(0).questionID);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        resolveText.setBackground(getActivity().getDrawable(R.drawable.dm_next_background));
                    }
                    resolveText.setTextColor(getResources().getColor(R.color.white));

                } else if (followupData.get(0).status.equalsIgnoreCase(Constants.NOTRESOLVED)) {
                    CoQId.add(dmSurveyAnswerTableList.get(0).questionID);
                    Constants.questionIdsFollowUp.add(dmSurveyAnswerTableList.get(0).questionID);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notResolveText.setBackground(getActivity().getDrawable(R.drawable.dm_next_background));
                    }
                    notResolveText.setTextColor(getResources().getColor(R.color.white));
                }
            }
            notResolveText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitValue(questionIdDb,Constants.NOTRESOLVED, complaint_id);

                    if (!CoQId.contains(id)) {
                        CoQId.add(id);
                    }
                    if (!Constants.questionIdsFollowUp.contains(id)) {
                        Constants.questionIdsFollowUp.add(id);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notResolveText.setBackground(getActivity().getDrawable(R.drawable.dm_next_background));
                    }
                    notResolveText.setTextColor(getResources().getColor(R.color.white));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        resolveText.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
                    }

                    if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionIdDb, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                        db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionIdDb,Constants.FARMER_ID,Constants.ATTEMPT, Constants.PLOT_ID);
                    }

                    resolveText.setTextColor(getResources().getColor(R.color.gray));
                }
            });

            resolveText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitValue(questionIdDb,Constants.RESOLVED, complaint_id);

                    if (!CoQId.contains(id)) {
                        CoQId.add(id);

                    }

                    if (!Constants.questionIdsFollowUp.contains(id)) {
                        Constants.questionIdsFollowUp.add(id);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        resolveText.setBackground(getActivity().getDrawable(R.drawable.dm_next_background));
                    }
                    resolveText.setTextColor(getResources().getColor(R.color.white));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notResolveText.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
                    }
                    notResolveText.setTextColor(getResources().getColor(R.color.gray));

                    if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionIdDb, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                        db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionIdDb,Constants.FARMER_ID,Constants.ATTEMPT, Constants.PLOT_ID);
                    }

                }
            });


        }

        editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));

        if (linearLayoutItems != null) {
            linearLayoutItems.addView(editText);
            linearLayoutItems.addView(linearLayout1);

        }

        linearLayoutItems.setVisibility(View.VISIBLE);

        dm_question_complaint_followup_survey.removeView(linearLayoutItems);
        if (dm_question_complaint_followup_survey != null) {
            dm_question_complaint_followup_survey.addView(linearLayoutItems);

        }

    }

    private void submitValue(String questionId, String status, String complianceId) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String submittedDate = format.format(today);
        FollowUpItemMainTable followUpItemMainTable = new FollowUpItemMainTable();
        followUpItemMainTable.questionId = Integer.parseInt(questionId);
        followUpItemMainTable.complianceId = Integer.parseInt(complianceId);
        followUpItemMainTable.moduleId = Integer.parseInt(Constants.MODULE_ID);
        followUpItemMainTable.attempt = Constants.ATTEMPT;
        followUpItemMainTable.createdAt = submittedDate;
        followUpItemMainTable.status = status;
        followUpItemMainTable.updatedAt = submittedDate;
        followUpItemMainTable.farmerId = Long.parseLong(Constants.FARMER_ID);
        followUpItemMainTable.isSync = false;

        db.followUpItemMainTableDao().deleteByQnAttempt(questionId,
                Constants.FARMER_ID,
                followUpItemMainTable.attempt);
        db.followUpItemMainTableDao().insert(followUpItemMainTable);
    }


    @Override
    public void onResume() {
        totalQuestion.clear();
        dm_question_complaint_followup_survey.removeAllViews();

        db = OFISRoomDB.getDbInstance(getActivity());
        CoQId.clear();
        Constants.questionIdsFollowUp.clear();
        complianceActionId = db.complianceIndicatorDao().getComplianceActionByModuleId(Constants.MODULE_ID);

        // Display all compliance's based in module Id
        initializeComplianceStatus();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.ll_servey_next_followup_survey) {
            List<Integer> resolve_count = db.followUpItemMainTableDao().getCountOfResolvedNotResolved( Constants.MODULE_ID, String.valueOf(Constants.ATTEMPT) , Constants.FARMER_ID);

            if (new HashSet<>(resolve_count).containsAll(totalQuestion)) {
                ViewPager2 viewPager = getActivity().findViewById(R.id.viewPagerFollowUp);
                viewPager.setCurrentItem(1);
                Constants.capture_count = capture_count;
            }
            else {
                Toast.makeText(getActivity(), getResources().getString(R.string.dm_please_select_action_plan_status_before_proceed), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.ll_servey_back_followup_survey) {
            requireActivity().onBackPressed();
        }
    }
}