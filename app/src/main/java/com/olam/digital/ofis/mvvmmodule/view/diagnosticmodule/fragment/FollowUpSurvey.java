package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

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
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class FollowUpSurvey extends Fragment implements View.OnClickListener {
    LinearLayout dm_question_complaint_followup_survey, ll_servey_back_followup_survey, ll_servey_next_followup_survey;

    OFISRoomDB db;

    ArrayList<String> CoQId = new ArrayList<>();
    int totalQuestion = 0;
    int capture_count = 0;

    public FollowUpSurvey() {
        // Required empty public constructor
    }


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

    @SuppressLint("ResourceAsColor")
    private void displayQuestionsModuleComplaint(String complaint_id) {
        boolean isActionPlanRequired = db.complianceIndicatorDao().getIsActionPlanRequired(complaint_id);
        List<ComplianceIndicator> dmComplaintList = db.complianceIndicatorDao().getComplianceNameById(complaint_id);

        if (!dmComplaintList.isEmpty()) {
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceIdFilterByActionPlan(complaint_id, Constants.FARMER_ID, Constants.PLOT_ID);
            String complianceName = dmComplaintList.get(0).compliance;
            if (complianceName.equalsIgnoreCase("Satisfactory")) {
                Constants.satisfactorySize = String.valueOf(dmSurveyAnswerTableList.size());
            } else if (complianceName.equalsIgnoreCase("Compliant")) {
                Constants.complaintSize = String.valueOf(dmSurveyAnswerTableList.size());
            } else if (complianceName.equalsIgnoreCase("Non Compliant")) {
                Constants.noncomplaintSize = String.valueOf(dmSurveyAnswerTableList.size());
            }

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.complaint_layout, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint);
            final ImageView complaint_dropdown = convertView.findViewById(R.id.iv_complaint_dropdown);

            dm_question_complaint_followup_survey.addView(convertView);
            convertView.setTag(complaint_id);
            complaint_dropdown.setTag("0" + complaint_id);
            if (Objects.equals(complianceName,"Compliant")) {
                selected_imgName.setText(getResources().getString(R.string.dm_compliant));
            } else if (Objects.equals(complianceName,"Non Compliant")) {
                selected_imgName.setText(getResources().getString(R.string.dm_non_compliant));
            } else if (Objects.equals(complianceName,"Satisfactory")) {
                selected_imgName.setText(getResources().getString(R.string.dm_satisfactory));
            }


            List<DmSurveyAnswerTable> finalDmSurveyAnswerTableList = dmSurveyAnswerTableList;
            complaint_dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = complaint_dropdown.getTag().toString();

                    int tagFirst = Integer.parseInt(String.valueOf(tag.charAt(0)));

                    if (tagFirst == 0) {
                        complaint_dropdown.setTag("1" + complaint_id);
                        complaint_dropdown.setRotation(180);
                        if (finalDmSurveyAnswerTableList.size() != 0) {

                            if(isActionPlanRequired){
                                View desiredView = dm_question_complaint_followup_survey.findViewWithTag("item" + complaint_id);
                                LinearLayout linearLayout = (LinearLayout) desiredView;
                                linearLayout.setVisibility(View.VISIBLE);
                            }

                        }


                    } else {
                        complaint_dropdown.setTag("0" + complaint_id);
                        complaint_dropdown.setRotation(0);
                        if (finalDmSurveyAnswerTableList.size() != 0) {
                            if(isActionPlanRequired){
                                View desiredView = dm_question_complaint_followup_survey.findViewWithTag("item" + complaint_id);
                                LinearLayout linearLayout = (LinearLayout) desiredView;
                                linearLayout.setVisibility(View.GONE);

                            }


                        }

                    }
                }
            });

            if(isActionPlanRequired){
                LinearLayout linearLayoutItems = new LinearLayout(getActivity());
                LinearLayout.LayoutParams layoutParamLastHp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayoutItems.setLayoutParams(layoutParamLastHp);
                linearLayoutItems.setPadding(25, 10, 10, 10);
                linearLayoutItems.setTag("item" + complaint_id);
                linearLayoutItems.setOrientation(LinearLayout.VERTICAL);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    linearLayoutItems.setBackground(getActivity().getDrawable(R.drawable.dm_actionplan_module_bg));
                }

                if (dmSurveyAnswerTableList.size() != 0) {
                    String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;

                    for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                        DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                        dmQuestionsModule.question = getNamesbyLanguage(dmQuestionsModule);
                        TextView textView = new TextView(getActivity());
                        textView.setText((m + 1) + "." + dmQuestionsModule.question);
                        textView.setTextColor(getResources().getColor(R.color.black));
                        textView.setTextSize(14);
                        textView.setPadding(0, 15, 0, 0);
                        textView.setTypeface(null, Typeface.BOLD);
                        linearLayoutItems.addView(textView);
                        renderTextboxQuestionModuleComplaint(String.valueOf(dmQuestionsModule.questionID), complaint_id, linearLayoutItems);

                    }
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
        totalQuestion++;
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

            boolean isActionPlanRequired = db.complianceIndicatorDao().getIsActionPlanRequired(complaint_id);
            if (isActionPlanRequired){
                String answerIdDb = dmSurveyAnswerTableList.get(0).actionPlanText;
                String questionIdDb = dmSurveyAnswerTableList.get(0).questionID;
                List<DmRangeActionPlansItems> answerActionPlan = db.dmRangeActionPlansItemsDao().getActionPlanItemById(Integer.parseInt(questionIdDb),Integer.parseInt(dmSurveyAnswerTableList.get(0).complinceTypeId),dmSurveyAnswerTableList.get(0).answerID);
                if (!answerActionPlan.isEmpty()){
                    editText.setText(Utilities.getActionPlanByLanguage(answerActionPlan.get(0)));
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


        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

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
        totalQuestion = 0;
        dm_question_complaint_followup_survey.removeAllViews();

        db = OFISRoomDB.getDbInstance(getActivity());
        CoQId.clear();
        Constants.questionIdsFollowUp.clear();

        if (!db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).isEmpty()) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
            }
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.ll_servey_next_followup_survey) {
            int resolve_count = db.followUpItemMainTableDao().getCountOfResolvedNotResolved( Constants.MODULE_ID, String.valueOf(Constants.ATTEMPT) , Constants.FARMER_ID);

            if (resolve_count == totalQuestion) {
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