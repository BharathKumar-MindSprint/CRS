package com.olam.digital.ofis.coffeesurveyconstraints;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.coffeesurveyconstraints.utils.CommonUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;


@SuppressWarnings("ConstantConditions")
public class CoffeeConstraints {

    private QuestionInfo questionInfo;
    private Context mContext;
    private DataLayer dataLayer;
    private ArrayList<JSONObject> answerSet;
    private Cursor questionsCursor;


    public CoffeeConstraints(Context context, QuestionInfo questionInfo, DataLayer dataLayer,ArrayList<JSONObject> answerSet,Cursor questionsCursor) {
        this.mContext = context;
        this.questionInfo = questionInfo;
        this.dataLayer = dataLayer;
        this.answerSet=answerSet;
        this.questionsCursor=questionsCursor;

    }

    public CoffeeConstraints() {
    }


    public static int isDecimalRestrict(JSONObject finalDisplayConfig) throws JSONException {
        int digit= 0;
        String calculationNode = finalDisplayConfig.optString(CoffeeConstants.CALCULATION);
        JSONObject jsonObject = new JSONObject(calculationNode);
        String type = jsonObject.optString(CoffeeConstants.TYPE);
        if (type.equalsIgnoreCase(CoffeeConstants.DECIMAL_DIGITS)){
            String digits = jsonObject.optString(CoffeeConstants.DIGITS).trim();
            if (digits.length() > 0){
                digit=Integer.parseInt(digits);
            }
        }
         return digit;
    }

    public String getConstraintExceedMsg(Context context,DataLayer dl) {
        String errorMsg=context.getResources().getString(R.string.please_check_question);
        Set<Integer> constraintQuestions= Objects.requireNonNull(DynamicQuestions.getInstance()).constraintsSet;
        String questions = "";
        if (constraintQuestions.size()>0){
            StringBuilder csvBuilder = new StringBuilder();
            for(Integer question : constraintQuestions){
                csvBuilder.append(dl.getQuestionNo(String.valueOf(question)));
                csvBuilder.append(",");
            }
            questions= csvBuilder.toString();
            questions = questions.substring(0, questions.length() - ",".length());
        }

        return errorMsg+" "+questions;
    }


    public Constraint constraintType(JSONObject displayConfig, String value) throws JSONException {

        String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION);
        JSONObject calculationJsonObject = new JSONObject(calculationNode);
        String type = calculationJsonObject.optString(CoffeeConstants.TYPE);
        Constraint constraint = new Constraint();

        if (type.equalsIgnoreCase(CoffeeConstants.COMPARE)) {

            int  questionPosition=calculationJsonObject.optInt(CoffeeConstants.QUESTION_NUMBER,0);

            // If value s empty
            if (value.equalsIgnoreCase("")){
//                ArrayList<Integer> errorList=new ArrayList<>();
//                errorList.add(questionPosition);
//                constraint.setErrorMsg(generateErrorMsgByQuestionId(errorList,displayConfig));
                return constraint;
            }

            JSONArray valuesInOrder = (JSONArray) calculationJsonObject.get(CoffeeConstants.VALUES_IN_ORDER);
            ArrayList<Integer> valueList = new ArrayList<>();
            for (int i = 0; i < valuesInOrder.length(); i++) {
                valueList.add(valuesInOrder.getInt(i));
            }
            String operation = calculationJsonObject.optString(CoffeeConstants.OPERATION);
            int x = 0, y = 0;
            if (valueList.size() == 0) {
                Toast.makeText(mContext, "Survey Config error. Please check the display configuration",Toast.LENGTH_SHORT).show();
            } else if (valueList.size() == 1) {
                x = (int) valuesInOrder.get(0);
            } else if (valueList.size() == 2) {
                y = (int) valuesInOrder.get(1);
            }
            constraint = comparisionCheck(operation, Integer.parseInt(value), x, y,questionPosition,calculationJsonObject);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MIN_MAX)) {
            int min = Integer.parseInt(calculationJsonObject.getString(CoffeeConstants.MIN));
            int max = Integer.parseInt(calculationJsonObject.getString(CoffeeConstants.MAX));
            int  questionPosition=calculationJsonObject.optInt(CoffeeConstants.QUESTION_NUMBER,0);
            if (value.equalsIgnoreCase("")){
//                ArrayList<Integer> errorList=new ArrayList<>();
//                errorList.add(questionPosition);
//                constraint.setErrorMsg(generateErrorMsgByQuestionId(errorList,displayConfig));
                return constraint;
            }
            constraint = minMaxCheck(Integer.parseInt(value), min, max,questionPosition,calculationJsonObject);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MULTIPLE_FORMULA_DROPDOWN)) {
            constraint = multipleCalculationDropdown(displayConfig);
        }else if (type.equalsIgnoreCase(CoffeeConstants.MULTIPLE_CALCULATION_COUNTRY)){
            constraint=multipleCalculationCountry(displayConfig);
        }else if (type.equalsIgnoreCase(CoffeeConstants.SINGLE_CALCULATION)) {
            try {
                constraint = singleCalculation(displayConfig);
            }catch (Exception ignored){}

        }
        return constraint;
    }


    private Constraint comparisionCheck(String operation, int value, int x, int y, int position,JSONObject calculationObj) {
        Constraint constraint = new Constraint();
        boolean error=false;
        switch (operation) {
            case "<x":
                if (value < x) {
                    error =true;
                }
                break;
            case ">x":
                if (value > x) {
                    error=true;
                }
                break;

            case "<=x":
                if (value <= x) {
                    error=true;
                }
                break;
            case ">=x":
                if (value >= x) {
                    error=true;
                }
                break;
            case ">x<y":
                if (value > x && value < y) {
                    error=true;
                }
                break;
            default:
                Toast.makeText(mContext, "Configuration error. Operator is out of the system assignment", Toast.LENGTH_SHORT).show();
        }
        if (error) {
            try {
                constraint.setErrorMsg(getErrorMsg(calculationObj,position,0));
                constraint.setOverLimit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return constraint;
    }

    private Constraint minMaxCheck(int value, int min, int max,int position,JSONObject calculationObj) {
        boolean minMax = false;
        Constraint constraint = new Constraint();
        if (value >= min && value <= max) {
            minMax = true;
        }
        if (!minMax) {
            try {
                constraint.setErrorMsg(getErrorMsg(calculationObj,position,0));
                constraint.setOverLimit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return constraint;
    }


    private Constraint multipleCalculationDropdown(JSONObject displayConfig) throws JSONException {
        Constraint constraint = new Constraint();
        // get Question Information
        String calculation = displayConfig.getString(CoffeeConstants.CALCULATION);
        JSONObject calculationObj = new JSONObject(calculation);
        // Question No
        int  questionPosition=calculationObj.optInt(CoffeeConstants.QUESTION_NUMBER,0);
        // Questions Info
        JSONArray questionsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.QUESTIONS_IN_ORDER);
        ArrayList<Integer> questionList = new ArrayList<>();
        for (int i = 0; i < questionsInOrder.length(); i++) {
            questionList.add(questionsInOrder.getInt(i));
        }
        // LookUpValues
        JSONArray lookUpValuesInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LOOK_UP_VALUES_IN_ORDER);
        ArrayList<Integer> lookUpValueList = new ArrayList<>();
        for (int i = 0; i < lookUpValuesInOrder.length(); i++) {
            lookUpValueList.add(lookUpValuesInOrder.getInt(i));
        }
        //dropdown question number
        String dropdownQuestion = calculationObj.getString(CoffeeConstants.DROPDOWN_CHECK);
        // get the formula Type - calculation
        String formula = calculationObj.getString(CoffeeConstants.FORMULA).replaceAll("\\\\", "");
        // get the limits in Order
        JSONArray limitsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LIMITS);
        ArrayList<Integer> limitsList = new ArrayList<>();
        for (int i = 0; i < limitsInOrder.length(); i++) {
            limitsList.add(Integer.parseInt(limitsInOrder.getString(i)));
        }


        ArrayList<Integer> errorEmptyList=new ArrayList<>();
        if (!dropdownQuestion.equalsIgnoreCase("")) {
                int farmerModuleId = (int) Utilities.farmerModuleId;
                // collecting answers for Questions In Order
                HashMap<String,Double> answerHashMap= new HashMap<>();
            for (int i=0; i< questionList.size();i++) {
                    if (isQuestionAvailableInSubModule(questionList.get(i))){
                        // get the answers from answer set
                        double answer= getAnswerFromSameSubmodule(questionList.get(i));
                        if (answer == -1){
                            if(questionList.get(i)!=questionInfo.getQuestionId()) {
                                errorEmptyList.add(questionList.get(i));
                            }
                        }else {
                            int questionNo = i + 1;
                            answerHashMap.put("q" + questionNo, answer);
                        }
                    }else {
                        Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionList.get(i));
                        if (answerQuery.getCount() > 0) {
                            while (answerQuery.moveToNext()) {
                                String dataType = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                                double answer = 0d;
                                if (dataType.equalsIgnoreCase("int")) {
                                    answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                                } else if (dataType.equalsIgnoreCase("decimal")) {
                                    answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                                }
                                answerHashMap.put("q" + i + 1, answer);
                            }
                        }else{
                            if(questionList.get(i)!=questionInfo.getQuestionId()) {
                                errorEmptyList.add(questionList.get(i));
                            }
                        }
                    }
                }

                // Collecting answer for dropdown Question and added in answer Hash Map
            int selectedLookupValue=0;
            if (isQuestionAvailableInSubModule(Integer.parseInt(dropdownQuestion))){
                selectedLookupValue =getLookUpValueAnswerSameModule(Integer.parseInt(dropdownQuestion));
                if (selectedLookupValue == 0){
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


            // If the calculation Questions does not answered
                if (errorEmptyList.size()>0){
                    String errorMsg = generateErrorMsgByQuestionId(errorEmptyList,displayConfig);
                    constraint.setErrorMsg(errorMsg);
                }else{
                    try {
                        constraint= doCalculationWithLookUpValue(formula,answerHashMap,limitsList,selectedLookupValue,lookUpValueList,questionPosition,calculationObj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            return constraint;
        }else{
            constraint.setErrorMsg(mContext.getResources().getString(R.string.common_survey_config_error));
        }
               return  constraint;
    }

    private boolean isQuestionAvailableInSubModule(int questionId) {
        ArrayList<Integer> questionList=new ArrayList<>();
        boolean available=false;
        if(questionsCursor != null && questionsCursor.moveToFirst()) {
            do {
                        String questionsString = questionsCursor.getString(questionsCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                        questionList.add(Integer.parseInt(questionsString));
            } while (questionsCursor.moveToNext());
        }

        if (questionList.contains(questionId)){ available=true;}
        return available;
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
            }
        }
        return answer;
    }

    private double getAnswerFromSameSubmodule(int questionId) throws JSONException {
        double answer = -1;
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                if(currentAnswer.optInt("question_id") == questionId){
                   String answerStr= currentAnswer.getString("answer");
                   answer= Double.parseDouble(answerStr);
                }
            }
        }
        return answer;
    }



    private Constraint doCalculationWithLookUpValue(String formula, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, int selectedLookupValue, ArrayList<Integer> lookUpValueList,int position,JSONObject calculationObj) {
        Constraint constraint=new Constraint();
        if (selectedLookupValue !=0) {
            for (int i=0; i<limitsList.size();i++) {
                if (selectedLookupValue == lookUpValueList.get(i)) {
                    // Check the all possible formulas
                    boolean error =false;
                    try {
                        if (formula.equalsIgnoreCase("q1/q2>x")) {
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                                error=true;
                            }
                        }else if (formula.equalsIgnoreCase("q1/q2<x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") < limitsList.get(i)) {
                                error=true;
                            }
                        }else if (formula.equalsIgnoreCase("q1/q2<=x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") <= limitsList.get(i)) {
                                error=true;
                            }
                        }
                        else if (formula.equalsIgnoreCase("q1/q2>=x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") >= limitsList.get(i)) {
                                error=true;
                            }
                        }
                        if (error) {
                            constraint.setErrorMsg(getErrorMsg(calculationObj, position, limitsList.get(i)));
                            constraint.setOverLimit(true);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return constraint;
    }


    private String generateErrorMsgByQuestionId(ArrayList<Integer> errorEmptyList,JSONObject displayConfig){
        // arrange the proper position - Old
        String error="";
        try {
            String calcNode=displayConfig.getString(CoffeeConstants.CALCULATION);
            JSONObject jsonObject=new JSONObject(calcNode).getJSONObject("mandatoryErrorMsg");
            String msg= jsonObject.optString("error"+CommonUtilities.getLocaleSuffix(mContext.getResources()),"Please configure the error message");
            error= msg.replace("~Q",String.valueOf(dataLayer.getQuestionNo(errorEmptyList.get(0).toString())));
        }catch (Exception e){
            e.printStackTrace();
        }
       return  error;
    }


// Multiple Calculation Country
private Constraint multipleCalculationCountry( JSONObject displayConfig) throws JSONException {
    Constraint constraint = new Constraint();
    String calculation = displayConfig.getString(CoffeeConstants.CALCULATION);
    JSONObject calculationObj = new JSONObject(calculation);
    // Questions In order
    JSONArray questionsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.QUESTIONS_IN_ORDER);
    ArrayList<Integer> questionList = new ArrayList<>();
    for (int i = 0; i < questionsInOrder.length(); i++) {
        questionList.add(questionsInOrder.getInt(i));
    }
    // Question No
    int  questionPosition=calculationObj.optInt(CoffeeConstants.QUESTION_NUMBER,0);
    // get the formula Type - calculation
    String formula = calculationObj.getString(CoffeeConstants.FORMULA).replaceAll("\\\\", "");
   //  get limits in order
    JSONArray limitsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LIMITS);
    ArrayList<Integer> limitsList = new ArrayList<>();
    for (int i = 0; i < limitsInOrder.length(); i++) {
        limitsList.add(Integer.parseInt(limitsInOrder.getString(i)));
    }

        int farmerModuleId = (int) Utilities.farmerModuleId;
        // collecting answers for Questions In Order
        HashMap<String,Double> answerHashMap= new HashMap<>();
        ArrayList<Integer> errorEmptyList=new ArrayList<>();
        for (int i=0; i< questionList.size();i++) {

            if (isQuestionAvailableInSubModule(questionList.get(i))){
                // get the answers from answer set
                double answer= getAnswerFromSameSubmodule(questionList.get(i));
                if (answer == -1){
                    if(questionList.get(i)!=questionInfo.getQuestionId()) {
                        errorEmptyList.add(questionList.get(i));
                    }
                }else {
                    int questionNo = i + 1;
                    answerHashMap.put("q" + questionNo, answer);
                }
            }else {

                Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionList.get(i));
                if (answerQuery.getCount() > 0) {
                    while (answerQuery.moveToNext()) {
                        String dataType = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                        double answer = 0d;
                        if (dataType.equalsIgnoreCase("int")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                        } else if (dataType.equalsIgnoreCase("decimal")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                        }
                        int questionNo = i + 1;
                        answerHashMap.put("q" + questionNo, answer);
                    }
                }else{
                    if(questionList.get(i)!=questionInfo.getQuestionId()) {
                        errorEmptyList.add(questionList.get(i));
                    }
                }
            }
        }

        // get Country Id  from farmer details
        Cursor farmer=dataLayer.getFarmerById(Utilities.farmerId);
        int  countryId = 0;
        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
           countryId= Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));
        }

        // Multiple country arrays - getting it in list
        JSONArray countryInOrder = (JSONArray) calculationObj.get(CoffeeConstants.COUNTRY_IN_ORDER);
        ArrayList<ArrayList<Integer>> countryList = new ArrayList<>();
        for (int i = 0; i < countryInOrder.length(); i++) {
            JSONArray countryGroup= countryInOrder.getJSONArray(i);
            ArrayList<Integer> countryGroupList=new ArrayList<>();
            for (int j=0; j<countryGroup.length(); j++){
                countryGroupList.add(countryGroup.getInt(j));
            }
            countryList.add(countryGroupList);
        }


        if (errorEmptyList.size()>0){
            String errorMsg = generateErrorMsgByQuestionId(errorEmptyList,displayConfig);
            constraint.setErrorMsg(errorMsg);
        }else{
            constraint= doCalculationWithMultipleCountry(formula,answerHashMap,limitsList,countryId,countryList,questionPosition,calculationObj);
        }
        return constraint;
}

    private Constraint doCalculationWithMultipleCountry(String formula, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, int countryId, ArrayList<ArrayList<Integer>> countryList,int questionPosition,JSONObject calculationObj) {
        Constraint constraint=new Constraint();
        if (countryId !=0) {
            for (int i=0; i<limitsList.size();i++) {
                if (countryList.get(i).contains(countryId)) {
                    // Check the all possible formulas
                    boolean error =false;
                    try {
                        if (formula.equalsIgnoreCase("q1/q2>x")) {
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                                error=true;
                            }
                        }else if (formula.equalsIgnoreCase("q1/q2<x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") < limitsList.get(i)) {
                                error=true;
                            }
                        }else if (formula.equalsIgnoreCase("q1/q2<=x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") <= limitsList.get(i)) {
                                error=true;
                            }
                        }
                        else if (formula.equalsIgnoreCase("q1/q2>=x")){
                            if (answerHashMap.get("q1") / answerHashMap.get("q2") >= limitsList.get(i)) {
                                error=true;
                            }
                        }else if ((formula.equalsIgnoreCase("q1>x"))){
                            if (answerHashMap.get("q1") > limitsList.get(i)){
                                error =true;
                            }
                        }
                        if (error) {
                            constraint.setErrorMsg(getErrorMsg(calculationObj, questionPosition, limitsList.get(i)));
                            constraint.setOverLimit(true);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return constraint;
    }


    private Constraint singleCalculation(JSONObject displayConfig) throws Exception {
        Constraint constraint=new Constraint();
        // get Question Information
        String calculation =displayConfig.getString(CoffeeConstants.CALCULATION);
        JSONObject calculationObj = new JSONObject(calculation);
        // Question No
        int  questionPosition=calculationObj.optInt(CoffeeConstants.QUESTION_NUMBER,0);
        // questions
        JSONArray questionsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.QUESTIONS_IN_ORDER);
        ArrayList<Integer> questionList = new ArrayList<>();
        for (int i = 0; i < questionsInOrder.length(); i++) {
            questionList.add(questionsInOrder.getInt(i));
        }
        // get the formula Type - calculation
        String formula = calculationObj.getString(CoffeeConstants.FORMULA).replaceAll("\\\\", "");

        JSONArray limitsInOrder = (JSONArray) calculationObj.get(CoffeeConstants.LIMITS);
        ArrayList<Integer> limitsList = new ArrayList<>();
        for (int i = 0; i < limitsInOrder.length(); i++) {
            limitsList.add(Integer.parseInt(limitsInOrder.getString(i)));
        }
        int farmerModuleId = (int) Utilities.farmerModuleId;
        // collecting answers for Questions In Order
        HashMap<String,Double> answerHashMap= new HashMap<>();
        ArrayList<Integer> errorEmptyList=new ArrayList<>();
        for (int i=0; i< questionList.size();i++) {

            if (isQuestionAvailableInSubModule(questionList.get(i))){
                // get the answers from answer set
                double answer= getAnswerFromSameSubmodule(questionList.get(i));
                if (answer == -1){
                    if(questionList.get(i)!=questionInfo.getQuestionId()) {
                        errorEmptyList.add(questionList.get(i));
                    }
                }else {
                    int questionNo = i + 1;
                    answerHashMap.put("q" + questionNo, answer);
                }
            }else {
                Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionList.get(i));
                if (answerQuery.getCount() > 0) {
                    while (answerQuery.moveToNext()) {
                        String dataType = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                        double answer = 0d;
                        if (dataType.equalsIgnoreCase("int")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                        } else if (dataType.equalsIgnoreCase("decimal")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                        }
                        int questionNo = i + 1;
                        answerHashMap.put("q" + questionNo, answer);
                    }
                }else{
                    if(questionList.get(i)!=questionInfo.getQuestionId()) {
                        errorEmptyList.add(questionList.get(i));
                    }
                }
            }
        }

        if (errorEmptyList.size()>0){
            String errorMsg = generateErrorMsgByQuestionId(errorEmptyList,displayConfig);
            constraint.setErrorMsg(errorMsg);
        }else{
            constraint= doCalculationWithSingleLogic(formula,answerHashMap,limitsList,calculationObj,questionPosition);
        }

        return constraint;
    }

    private Constraint doCalculationWithSingleLogic(String formula, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, JSONObject constraintObj, int questionPosition)  {
        Constraint constraint=new Constraint();
        for (int i=0; i<limitsList.size();i++) {
            // Check the all possible formulas
            boolean error =false;
            try {
                if (formula.equalsIgnoreCase("q1/q2>x")) {
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                        error=true;
                    }
                }else if (formula.equalsIgnoreCase("q1/q2<x")){
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") < limitsList.get(i)) {
                        error=true;
                    }
                }else if (formula.equalsIgnoreCase("q1/q2<=x")){
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") <= limitsList.get(i)) {
                        error=true;
                    }
                }
                else if (formula.equalsIgnoreCase("q1/q2>=x")){
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") >= limitsList.get(i)) {
                        error=true;
                    }
                }
                if (error) {
                    constraint.setErrorMsg(getErrorMsg(constraintObj, questionPosition, limitsList.get(i)));
                    constraint.setOverLimit(true);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return constraint;
    }

    private String getErrorMsg(JSONObject constraintObj,int questionPosition,int limitValue) throws Exception{
        String errorObject= constraintObj.getString("errorMsg");
        JSONObject errorObj=new JSONObject(errorObject);
        String msg= errorObj.optString("error"+CommonUtilities.getLocaleSuffix(mContext.getResources()),"Please configure the error message");
        msg= msg.replace("~Q",String.valueOf(questionPosition)).replace("~V",CommonUtilities.FormatNumbers(limitValue));
        return  msg;
    }
}


