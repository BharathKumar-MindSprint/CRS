package com.olam.digital.ofis.coffeesurveyconstraints;

import android.content.Context;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.Toast;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class CoffeeConstraints {

    private QuestionInfo questionInfo;
    private Context mContext;
    private DataLayer dataLayer;
    private ArrayList<JSONObject> answerSet;


    public CoffeeConstraints(Context context, QuestionInfo questionInfo, DataLayer dataLayer,ArrayList<JSONObject> answerSet) {
        this.mContext = context;
        this.questionInfo = questionInfo;
        this.dataLayer = dataLayer;
        this.answerSet=answerSet;

    }


    public static int isDecimalRestrict(JSONObject finalDisplayConfig) throws JSONException {
        int digit= 0;
        String calculationNode = finalDisplayConfig.optString(CoffeeConstants.CALCULATION);
        JSONObject jsonObject = new JSONObject(calculationNode);
        String type = jsonObject.optString(CoffeeConstants.TYPE);
        if (type.equalsIgnoreCase(CoffeeConstants.DECIMAL_DIGITS)){
            String digits = jsonObject.optString(CoffeeConstants.DIGITS).trim();
            if (digits !=null && digits.length()>0){
                digit=Integer.parseInt(digits);
            }
        }
         return digit;
    }



    public Constraint constraintType(JSONObject displayConfig, int value) throws JSONException {

        String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION);
        JSONObject jsonObject = new JSONObject(calculationNode);
        String type = jsonObject.optString(CoffeeConstants.TYPE);
        Constraint constraint = null;

        if (type.equalsIgnoreCase(CoffeeConstants.COMPARE)) {
            JSONArray valuesInOrder = (JSONArray) jsonObject.get(CoffeeConstants.VALUES_IN_ORDER);
            ArrayList<Integer> valueList = new ArrayList<>();
            for (int i = 0; i < valuesInOrder.length(); i++) {
                valueList.add(valuesInOrder.getInt(i));
            }

            String operation = jsonObject.optString(CoffeeConstants.OPERATION);
            int x = 0, y = 0;
            if (valueList.size() == 0) {
            } else if (valueList.size() == 1) {
                x = (int) valuesInOrder.get(0);
            } else if (valueList.size() == 2) {
                y = (int) valuesInOrder.get(1);
            }
            constraint = comparisionCheck(operation, value, x, y);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MIN_MAX)) {
            int min = Integer.parseInt(jsonObject.getString(CoffeeConstants.MIN));
            int max = Integer.parseInt(jsonObject.getString(CoffeeConstants.MAX));
            constraint = minMaxCheck(value, min, max);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MULTIPLE_FORMULA_DROPDOWN)) {
            constraint = multipleCalculationDropdown(value,displayConfig);
        }
        return constraint;
    }


    private Constraint comparisionCheck(String operation, int value, int x, int y) {
        boolean success = false;
        Constraint constraint = new Constraint();
        switch (operation) {
            case "<x":
                if (value < x) {
                    success = true;
                }
                break;
            case ">x":
                if (value > x) {
                    success = true;
                }
                break;

            case "<=x":
                if (value <= x) {
                    success = true;
                }
                break;
            case ">=x":
                if (value >= x) {
                    success = true;
                }
                break;
            case ">x<y":
                if (value > x && value < y) {
                    success = true;
                }
                break;
            default:
                Toast.makeText(mContext, "New Operator", Toast.LENGTH_SHORT).show();
        }
        constraint.setSuccess(success);
        constraint.setErrorMsg("This is compare error message");
        return constraint;
    }

    private Constraint minMaxCheck(int value, int min, int max) {
        boolean minMax = false;
        Constraint constraint = new Constraint();
        if (value >= min && value <= max) {
            minMax = true;
        }
        constraint.setSuccess(minMax);
        if (!minMax){
            constraint.setErrorMsg("Error Message : " + min + "   " + max);
        }
        return constraint;
    }


    private Constraint multipleCalculationDropdown(int value, JSONObject displayConfig) throws JSONException {
        Constraint constraint = new Constraint();

        // get Question Information
        String calculation = questionInfo.getDisplayConfig().getString(CoffeeConstants.CALCULATION);

        JSONObject calculationObj = new JSONObject(calculation);

        // Questions Info
        JSONArray questionsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.QUESTIONS_IN_ORDER);
        ArrayList<Integer> questionList = new ArrayList<>();
        for (int i = 0; i < questionsInOrder.length(); i++) {
            questionList.add(questionsInOrder.getInt(i));
        }

        // LookUpValue  Info
        JSONArray lookUpValuesInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LOOK_UP_VALUES_IN_ORDER);
        ArrayList<Integer> lookUpValueList = new ArrayList<>();
        for (int i = 0; i < lookUpValuesInOrder.length(); i++) {
            lookUpValueList.add(lookUpValuesInOrder.getInt(i));
        }

        //dropdown LookUpvalue
        String dropdownQuestion = calculationObj.getString(CoffeeConstants.DROPDOWN_CHECK);

        String innerOperation = calculationObj.getString(CoffeeConstants.INNER_OPERATION);
        String outerOperation = calculationObj.getString(CoffeeConstants.OUTER_OPERATION);

        // LookUpValue  Info
        JSONArray formulaInOrder = (JSONArray) calculationObj.get(CoffeeConstants.FORMULA);
        ArrayList<String> formulaList = new ArrayList<>();
        for (int i = 0; i < formulaInOrder.length(); i++) {
            formulaList.add(formulaInOrder.getString(i));
        }

        JSONArray limitsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LIMITS);
        ArrayList<Integer> limitsList = new ArrayList<>();
        for (int i = 0; i < limitsInOrder.length(); i++) {
            limitsList.add(Integer.parseInt(limitsInOrder.getString(i)));
        }


        if (formulaList.size() == 2 && !dropdownQuestion.equalsIgnoreCase("")) {
//            if (formulaList.get(0).equalsIgnoreCase("q1/q2>x") &&
//                    formulaList.get(0).equalsIgnoreCase("q1/q2>y")) {
                int farmerModuleId = (int) Utilities.farmerModuleId;




                // collecting answers for Questions In Order
                HashMap<String,Double> answerHashMap= new HashMap<>();
                ArrayList<Integer> errorEmptyList=new ArrayList<>();
            for (int i=0; i< questionList.size();i++) {

                    // if the question is from same submodule
                    if (farmerModuleId ==0){
                        // get the answers from answer set
                        double answer= getAnswerFromSameSubmodule(questionList.get(i));
                        int questionNo=i+1;
                        answerHashMap.put("q" +questionNo, answer);
                        if (answer==0){errorEmptyList.add(i);}
                    }else {

                        Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, i);
                        if (answerQuery.getCount() > 0) {
                            while (answerQuery.moveToNext()) {
                                String datatype = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                                double answer = 0d;
                                if (datatype.equalsIgnoreCase("int")) {
                                    answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                                } else if (datatype.equalsIgnoreCase("decimal")) {
                                    answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                                }
                                answerHashMap.put("q" + i + 1, answer);
                            }
                        }
                    }
                }

                // Collecting answer for dropdown Question and added in answer Hash Map
            int selectedLookupValue=0;
            if (farmerModuleId == 0){
                selectedLookupValue =getLookUpValueAnswerSameModule(Integer.parseInt(dropdownQuestion));
                if (selectedLookupValue ==0){
                    errorEmptyList.add(Integer.parseInt(dropdownQuestion));
                }
            }else{
                Cursor answerQuery=   dataLayer.getAnswersByQuestionId(farmerModuleId,Integer.parseInt(dropdownQuestion));
                if (answerQuery.getCount()>0) {
                    while (answerQuery.moveToNext()) {
                        selectedLookupValue = Integer.parseInt(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                    }
                }else{
                    errorEmptyList.add(Integer.parseInt(dropdownQuestion));
                }
            }


                if (errorEmptyList.size()>0){
                    constraint.setSuccess(false);
                    String errorMsg = generateErrorMsgByQuestionId(errorEmptyList);
                    constraint.setErrorMsg(errorMsg);
                }else{
                    constraint= doCalculationWithLookUpValue(formulaList,answerHashMap,limitsList,selectedLookupValue,lookUpValueList);
                }
       //     }


            return constraint;
        }
               return  constraint;
    }


    private int getLookUpValueAnswerSameModule(int questionId) throws JSONException {
        int answer=0;
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                if(currentAnswer.optInt("question_id") == questionId){
                    String answerStr= currentAnswer.getString("answer");
                    answer= Integer.parseInt(answerStr);
                }
                //   String datatype = currentAnswer.getString("datatype");
            }
        }
        return answer;
    }

    private double getAnswerFromSameSubmodule(int questionId) throws JSONException {
        double answer = 0;
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                if(currentAnswer.optInt("question_id") == questionId){
                   String answerStr= currentAnswer.getString("answer");
                   answer= Double.parseDouble(answerStr);
                }
             //   String datatype = currentAnswer.getString("datatype");
            }
        }
        return answer;
    }



    private Constraint doCalculationWithLookUpValue(ArrayList<String> formulaList, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, int selectedLookupValue, ArrayList<Integer> lookUpValueList) {
        Constraint constraint=new Constraint();
//        if(formulaList.get(0).equalsIgnoreCase("q1/q2>x") &&
//                formulaList.get(0).equalsIgnoreCase("q1/q2>y")) {
        if (selectedLookupValue !=0) {
            for (int i=0; i<limitsList.size();i++) {
                if (selectedLookupValue == lookUpValueList.get(i)) {
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                        constraint.setSuccess(false);
                        constraint.setErrorMsg("failed the data exceeds");
                    } else {
                        constraint.setSuccess(true);
                    }

                }
                //       }

            }
        }
        return constraint;
    }


    private String generateErrorMsgByQuestionId(ArrayList<Integer> errorEmptyList) {
        // arrange the proper position
        return "error msg by 2 nd and 6th Question";
    }





}

