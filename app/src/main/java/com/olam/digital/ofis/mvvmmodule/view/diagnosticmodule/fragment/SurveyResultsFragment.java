package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AssessmentResultTypeItems;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DMSurveyResponseModel;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
//import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SurveyResultsFragment extends Fragment {
    LinearLayout ll_servey_next_servey_results, ll_servey_back_servey_results;
    ConstraintLayout survey_result_main;
    LinearLayout newView;
    ImageView traffic_indicator_green, traffic_indicator_orange, traffic_indicator_red;
    TextView weightageMarks;
    OFISRoomDB db;
    EditText editTextText, editTextText3, editTextText4, editTextText5, editTextText6 , remarks;

    String overAllResultComplianceID = "";
    String finalAssessmentValue = "";


    public SurveyResultsFragment() {
        // Required empty public constructor
    }
    String resultValue;
    String resultColor;
    int amount = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_survey_results, container, false);


        ll_servey_next_servey_results = view.findViewById(R.id.ll_servey_next_servey_results);
        ll_servey_back_servey_results = view.findViewById(R.id.ll_servey_back_servey_results);
        survey_result_main = view.findViewById(R.id.survey_result_main);
        editTextText = view.findViewById(R.id.editTextText);
        editTextText3 = view.findViewById(R.id.editTextText3);
        editTextText4 = view.findViewById(R.id.editTextText4);
        editTextText5 = view.findViewById(R.id.editTextText5);
        editTextText6 = view.findViewById(R.id.editTextText6);
        weightageMarks = view.findViewById(R.id.weightageMarks);
        remarks = view.findViewById(R.id.editTextTextMultiLine);

        db = OFISRoomDB.getDbInstance(getActivity());


        ll_servey_next_servey_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Call API

                List<DmActionPlanCurrenciesTable> actionPlanCurrenciesTables = db.dmActionPlanCurrenciesTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID);

                String currencyId = "0";

                if (actionPlanCurrenciesTables.size() != 0) {
                    currencyId = String.valueOf(actionPlanCurrenciesTables.get(0).currencyId);
                }
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String submittedDate = format.format(today);

                HashMap hashMap = new HashMap<>();
                hashMap.put("farmerId", Constants.FARMER_ID);
                hashMap.put("moduleId", Constants.MODULE_ID);
                hashMap.put("farmerGroupId", Constants.FARMER_GROUP_ID);
                hashMap.put("enumeratorId", Constants.ENUMERATOR_ID);
                hashMap.put("currencyId", currencyId);
                hashMap.put("submissionDate", submittedDate);
                hashMap.put("isMobile", true);

                HashMap answers = new HashMap();

                List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID);

                String totalAmount = "0";

                ArrayList<JSONObject> answerArrayList = new ArrayList<>();

                if (dmSurveyAnswerTableList.size() != 0) {
                    for (int i = 0; i < dmSurveyAnswerTableList.size(); i++) {
                        answers.put("questionId", dmSurveyAnswerTableList.get(i).questionID);
                        answers.put("answerId", dmSurveyAnswerTableList.get(i).answerID);
                        answers.put("answervalue", dmSurveyAnswerTableList.get(i).answer);
                        answers.put("complianceStatusId", dmSurveyAnswerTableList.get(i).complinceTypeId);
                        answers.put("actionplanId", dmSurveyAnswerTableList.get(i).actionplanId);
                        answers.put("actionPlanText", dmSurveyAnswerTableList.get(i).actionPlanText);

                        if (dmSurveyAnswerTableList.get(i).amount != 0) {
                            totalAmount = String.valueOf(Integer.parseInt(totalAmount) + dmSurveyAnswerTableList.get(i).amount);
                        }

                        List<DmComplaintFileTable> complaintFileTableList = new ArrayList<>();

                        complaintFileTableList.clear();

                        complaintFileTableList = db.dmComplaintFileTableDao().getFilesById(Integer.parseInt(dmSurveyAnswerTableList.get(i).questionID),Constants.FARMER_ID, 0);

                        List<String> files = new ArrayList<>();
                        files.clear();

                        for (int j = 0; j < complaintFileTableList.size(); j++) {
                            files.add(complaintFileTableList.get(j).getOfisFileName());
                        }

                        answers.put("captureProof", files);


                        answerArrayList.add(new JSONObject(answers));


                    }
                }

                DMSurveyReport dmSurveyReport = new DMSurveyReport(Integer.parseInt(Constants.complaintSize),
                        Integer.parseInt(Constants.satisfactorySize),
                        Integer.parseInt(Constants.noncomplaintSize),
                        (Integer.parseInt(Constants.complaintSize) + Integer.parseInt(Constants.satisfactorySize) + Integer.parseInt(Constants.noncomplaintSize)),
                        Double.parseDouble(String.valueOf(amount)),
                        finalAssessmentValue,
                        resultValue,
                        remarks.getText().toString(),
                        Long.parseLong(Constants.FARMER_ID),Integer.parseInt(Constants.MODULE_ID),
                        Integer.parseInt(Constants.FARMER_GROUP_ID),
                        Integer.parseInt(currencyId),submittedDate,resultColor,
                        overAllResultComplianceID);
                db.dmSurveyReportDao().insert(dmSurveyReport);


                DataLayer dl = new DataLayer(getResources(), getContext());
                dl.updateNqForDMSurvey(Constants.FARMER_ID, Constants.MODULE_ID, "DMSurvey");
                Intent intent = new Intent(getContext(),ViewFarmerActivity.class);
                getContext().startActivity(intent);


            }
        });

        newView = view.findViewById(R.id.survey_result_indicator);
        traffic_indicator_green = view.findViewById(R.id.indicator_green);
        traffic_indicator_orange = view.findViewById(R.id.indicator_orange);
        traffic_indicator_red = view.findViewById(R.id.indicator_red);

        /*int complaint = 0, nonComplaint = 0, satisfactory = 0;
               List<DmSurveyAnswerTable> dmSurveyAnswerTableList1 = new ArrayList<>();
                dmSurveyAnswerTableList1.clear();
                dmSurveyAnswerTableList1 = db.dmSurveyAnswerTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID);
                for (int j = 0; j < dmSurveyAnswerTableList1.size(); j++) {

                    int compliantTypeId = Integer.parseInt(dmSurveyAnswerTableList1.get(j).complinceTypeId);
                    int value = dmSurveyAnswerTableList1.get(j).amount;
                    amount = amount +value;
                    String complianceName = db.complianceIndicatorDao().getComplianceNameBycomplianceId(String.valueOf(compliantTypeId));
                    if (complianceName.equalsIgnoreCase("Satisfactory")) {
                        satisfactory = satisfactory + 1;
                    } else if (complianceName.equalsIgnoreCase("Compliant")) {
                        complaint = complaint + 1;
                    } else if (complianceName.equalsIgnoreCase("Non Compliant")) {
                        nonComplaint = nonComplaint + 1;
                    }


        }

        editTextText.setText(""+complaint);
        editTextText3.setText(""+nonComplaint);
        editTextText4.setText(""+satisfactory);

        editTextText5.setText(String.valueOf(complaint + nonComplaint + satisfactory));
        editTextText6.setText(""+ amount);
//        int complaint = Integer.parseInt(complaint);
//        int nonComplaint = Integer.parseInt(Constants.noncomplaintSize);
//        int satisfactory = Integer.parseInt(Constants.satisfactorySize);

        DmQuestionsModule isCritical = new DmQuestionsModule();
        Boolean val = isCritical.isCriticalQuest;


        if (val && nonComplaint >= 1 && satisfactory >= 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));
                traffic_indicator_green.setVisibility(View.GONE);
                traffic_indicator_orange.setVisibility(View.GONE);
                traffic_indicator_red.setVisibility(View.VISIBLE);
                resultValue = Constants.RED;
            }
        } else {

            if (nonComplaint > complaint && nonComplaint > satisfactory) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));
                    traffic_indicator_green.setVisibility(View.GONE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.VISIBLE);
                    resultValue = Constants.RED;
                }
            } else if (satisfactory > complaint && satisfactory > nonComplaint) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.orange_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.ambur_survey_result));
                    traffic_indicator_green.setVisibility(View.GONE);
                    traffic_indicator_orange.setVisibility(View.VISIBLE);
                    traffic_indicator_red.setVisibility(View.GONE);
                    resultValue = Constants.ORANGE;
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.green_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.green_survey_result));
                    traffic_indicator_green.setVisibility(View.VISIBLE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.GONE);
                    resultValue = Constants.GREEN;
                }
            }

        }*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Boolean val = false;
        double totalWeightages = 0;
        double finalScore = 0;
        int complaint = 0, nonComplaint = 0, satisfactory = 0;
        List<DmSurveyAnswerTable> dmSurveyAnswerTableList1 = new ArrayList<>();
        dmSurveyAnswerTableList1.clear();
        dmSurveyAnswerTableList1 = db.dmSurveyAnswerTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID);
        for (int j = 0; j < dmSurveyAnswerTableList1.size(); j++) {

            int compliantTypeId = Integer.parseInt(dmSurveyAnswerTableList1.get(j).complinceTypeId);
            int questionId = Integer.parseInt(dmSurveyAnswerTableList1.get(j).questionID);
            int value = dmSurveyAnswerTableList1.get(j).amount;
            amount = amount +value;
            String complianceName = db.complianceIndicatorDao().getComplianceNameBycomplianceId(String.valueOf(compliantTypeId));
            int newWeightage = db.complianceIndicatorDao().getComplianceScore(String.valueOf(compliantTypeId),complianceName);

            int newColorIsCritical = db.dmQuestionsModuleDao().getIsCriticalQuestForColor(Constants.MODULE_ID ,questionId);
            if (complianceName.equalsIgnoreCase("Satisfactory")) {
                totalWeightages = totalWeightages + newWeightage;
                satisfactory = satisfactory + 1;
                if (Objects.equals(newColorIsCritical, 1)){
                    val = true;
                }else {
                    val = false;
                }
            } else if (complianceName.equalsIgnoreCase("Compliant")) {
                complaint = complaint + 1;
                totalWeightages = totalWeightages + newWeightage;
            } else if (complianceName.equalsIgnoreCase("Non Compliant")) {
                nonComplaint = nonComplaint + 1;
                totalWeightages = totalWeightages + newWeightage;
                if (Objects.equals(newColorIsCritical, 1)){
                    val = true;
                }else {
                    val = false;
                }
            }


        }



        editTextText.setText(""+complaint);
        editTextText3.setText(""+nonComplaint);
        editTextText4.setText(""+satisfactory);


        String currencyIdDb = "0";

        HashMap<String, String> hashMap = new HashMap<>();
        String currency_id = hashMap.get("keyCurrencies");
        String totalActionPlanCount = hashMap.get("totalActionPlanCount");

        List<DmActionPlanCurrenciesTable> dmActionPlanCurrenciesTables = db.dmActionPlanCurrenciesTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID);
        if (dmActionPlanCurrenciesTables.size() != 0) {
            currencyIdDb = String.valueOf(dmActionPlanCurrenciesTables.get(0).currencyId);
            currency_id = currencyIdDb;
        }

        List<DmCurrenciesItem> currenciesItems = new ArrayList<>();
        currenciesItems.clear();
        currenciesItems = db.dmCurrenciesItemDao().getCurrencyItemById(Integer.parseInt(currency_id));

        String currency = "";

        if (currenciesItems.size() != 0) {
            currency = currenciesItems.get(0).notation;
        }


        int totalQuestion = complaint + nonComplaint + satisfactory;


        editTextText5.setText(Constants.TotalActionPlanCount);
        editTextText6.setText(""+ amount + "   " + currency);
//        int complaint = Integer.parseInt(complaint);
//        int nonComplaint = Integer.parseInt(Constants.noncomplaintSize);
//        int satisfactory = Integer.parseInt(Constants.satisfactorySize);




        String assessmentType = db.dmMainModuleDao().getAssessmentType(Integer.parseInt(Constants.MODULE_ID));
        String displayLogic = db.dmMainModuleDao().getDisplayLogic(Integer.parseInt(Constants.MODULE_ID));
        int maxScore = db.dmMainModuleDao().getMaxScore(Integer.parseInt(Constants.MODULE_ID));
        if (Objects.equals(assessmentType, "WB")){

            traffic_indicator_green.setVisibility(View.GONE);
            traffic_indicator_orange.setVisibility(View.GONE);
            traffic_indicator_red.setVisibility(View.GONE);


            weightageMarks.setVisibility(View.VISIBLE);
            if (Objects.equals(displayLogic, "In Percentage")){

                finalAssessmentValue = "In Percentage";
                remarks.setVisibility(View.VISIBLE);
                finalScore = (totalWeightages * 100 / (maxScore * totalQuestion));
                weightageMarks.setText(finalScore + " %");
                resultValue = String.valueOf(finalScore);


                List<AssessmentResultTypeItems> totalRangeAvailable = db.dmAssessmentResultTypeDao().getAllModuleId(Integer.parseInt(Constants.MODULE_ID));


                for (int i = 0; i <= totalRangeAvailable.size() ; i++) {
                    int minRange = totalRangeAvailable.get(i).getMinRange();
                    int maxRange = totalRangeAvailable.get(i).getMaxRange();
                    if (finalScore >= minRange && finalScore <= maxRange){
                        String remarkVal = String.valueOf(db.dmAssessmentResultTypeDao().getRemarks(Integer.parseInt(Constants.MODULE_ID) , minRange ,maxRange));
                        remarks.setText(remarkVal);
                        break;
                    }


                }

            }else if (Objects.equals(displayLogic, "As Whole Number")){
                finalAssessmentValue = "As Whole Number";
                weightageMarks.setText(String.valueOf(totalWeightages));
                remarks.setVisibility(View.VISIBLE);
                resultValue = String.valueOf(totalWeightages);
                List<AssessmentResultTypeItems> totalRangeAvailable = db.dmAssessmentResultTypeDao().getAllModuleId(Integer.parseInt(Constants.MODULE_ID));


                for (int i = 0; i <= totalRangeAvailable.size() ; i++) {
                    int minRange = totalRangeAvailable.get(i).getMinRange();
                    int maxRange = totalRangeAvailable.get(i).getMaxRange();
                    if (totalWeightages >= minRange && totalWeightages <= maxRange){
                        String remarkVal = String.valueOf(db.dmAssessmentResultTypeDao().getRemarks(Integer.parseInt(Constants.MODULE_ID) , minRange ,maxRange));
                        remarks.setText(remarkVal);
                        break;
                    }


                }
            }


        } else if (Objects.equals(assessmentType, "TL")) {
            weightageMarks.setVisibility(View.GONE);
            remarks.setVisibility(View.GONE);
            finalAssessmentValue = "TL";

        }







        if (val) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));

                if (Objects.equals(assessmentType, "TL")){
                    traffic_indicator_green.setVisibility(View.GONE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.VISIBLE);
//                    resultColor = Constants.RED;
                }

                overAllResultComplianceID = "Non-Compliant";
                resultColor = Constants.RED;
            }
        }else if (satisfactory == 0 && nonComplaint == 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newView.setBackground(getActivity().getDrawable(R.color.green_view_survey));
                survey_result_main.setBackground(getActivity().getDrawable(R.color.green_survey_result));

                if (Objects.equals(assessmentType, "TL")){
                    traffic_indicator_green.setVisibility(View.VISIBLE);
                    traffic_indicator_orange.setVisibility(View.GONE);
                    traffic_indicator_red.setVisibility(View.GONE);
//                    resultColor = Constants.GREEN;
                }
                overAllResultComplianceID = "Compliant";
                resultColor = Constants.GREEN;

            }
        }
        else {

            if (nonComplaint >= satisfactory) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.red_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.red_survey_result));

                    if (Objects.equals(assessmentType, "TL")){
                        traffic_indicator_green.setVisibility(View.GONE);
                        traffic_indicator_orange.setVisibility(View.GONE);
                        traffic_indicator_red.setVisibility(View.VISIBLE);
//                        resultColor = Constants.RED;
                    }

                    overAllResultComplianceID = "Non-Compliant";
                    resultColor = Constants.RED;

                }
            } else if(satisfactory > nonComplaint) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    newView.setBackground(getActivity().getDrawable(R.color.orange_view_survey));
                    survey_result_main.setBackground(getActivity().getDrawable(R.color.ambur_survey_result));

                    if (Objects.equals(assessmentType, "TL")){
                        traffic_indicator_green.setVisibility(View.GONE);
                        traffic_indicator_orange.setVisibility(View.VISIBLE);
                        traffic_indicator_red.setVisibility(View.GONE);
//                        resultColor = Constants.ORANGE;
                    }
                    overAllResultComplianceID = "Satisfactory";
                    resultColor = Constants.ORANGE;
                }
            }

        }

    }

    private void SubmitResult(String strRequest) {

        ManageRecords postAllDMSurveyData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DMSurveyResponseModel> call = postAllDMSurveyData.postAllDiagnosticModuleSurveyData(1, strRequest);
        call.enqueue(new Callback<DMSurveyResponseModel>() {

            @Override
            public void onResponse(Call<DMSurveyResponseModel> call, Response<DMSurveyResponseModel> response) {
//                Toast.makeText(getActivity(), "Inside Submit Servey", Toast.LENGTH_SHORT).show();

                try {
                    if (response.code() == 200) {
//                        Toast.makeText(getActivity(), "Inside Submit Servey Success", Toast.LENGTH_SHORT).show();
                        Log.d("ResultFragment", "total result:: " + response.body());

                    } else if (response.code() == 400) {
                        Log.d("Error400", "Inside 400");

                    } else if (response.code() == 500) {
                        Log.d("Error500", "Inside 500");
                        Toast.makeText(getActivity(), "Server Issue", Toast.LENGTH_SHORT).show();

                    } else {

                    }
                } catch (Exception e) {
                    Log.d("SrvySubmitEx", e.toString());
                    throw new RuntimeException(e);

                }
            }

            @Override
            public void onFailure(Call<DMSurveyResponseModel> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

            }
        });


    }
}