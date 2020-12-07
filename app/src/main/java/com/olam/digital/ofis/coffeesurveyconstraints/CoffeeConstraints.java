package com.olam.digital.ofis.coffeesurveyconstraints;

import android.content.Context;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.crash.FirebaseCrash;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.ViewFarmerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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

    public CoffeeConstraints(ViewFarmerActivity context,DataLayer dataLayer) {
        this.mContext=context;
        this.dataLayer = dataLayer;
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

           int  questionPosition=jsonObject.optInt(CoffeeConstants.QUESTION_NUMBER,0);;

            int x = 0, y = 0;
            if (valueList.size() == 0) {
                Toast.makeText(mContext, "Survey Config error. There is no survey",Toast.LENGTH_SHORT).show();
            }
            else if (valueList.size() == 1) {
                x = (int) valuesInOrder.get(0);
            } else if (valueList.size() == 2) {
                y = (int) valuesInOrder.get(1);
            }
            constraint = comparisionCheck(operation, value, x, y,questionPosition);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MIN_MAX)) {
            int min = Integer.parseInt(jsonObject.getString(CoffeeConstants.MIN));
            int max = Integer.parseInt(jsonObject.getString(CoffeeConstants.MAX));
            int  questionPosition=jsonObject.optInt(CoffeeConstants.QUESTION_NUMBER,0);
            constraint = minMaxCheck(value, min, max,questionPosition);
        } else if (type.equalsIgnoreCase(CoffeeConstants.MULTIPLE_FORMULA_DROPDOWN)) {
            constraint = multipleCalculationDropdown(value,displayConfig);
        }else if (type.equalsIgnoreCase(CoffeeConstants.MULTIPLE_CALCULATION_COUNTRY)){
            constraint=multipleCalculationCountry(value,displayConfig);
        }
        return constraint;
    }


    private Constraint comparisionCheck(String operation, int value, int x, int y, int position) {
        boolean success = false;
        Constraint constraint = new Constraint();
        switch (operation) {
          //  Please enter value of question %1$d within %2$d<
            case "<x":
                if (value < x) {
                    success = true;
                }else{
                    constraint.setErrorMsg(mContext.getResources().getString(R.string.direct_value_above_error,position,x));
                }
                break;
            case ">x":
                if (value > x) {
                    success = true;
                }else{
                    constraint.setErrorMsg(mContext.getResources().getString(R.string.direct_value_less_error,position,x));
                }
                break;

            case "<=x":
                if (value <= x) {
                    success = true;
                }else{
                    constraint.setErrorMsg(mContext.getResources().getString(R.string.direct_value_less_than_error,position,x));
                }
                break;
            case ">=x":
                if (value >= x) {
                    success = true;
                }else{
                    constraint.setErrorMsg(mContext.getResources().getString(R.string.direct_value_greater_than_error,position,x));
                }
                break;
            case ">x<y":
                if (value > x && value < y) {
                    success = true;
                }else{
                    constraint.setErrorMsg(mContext.getResources().getString(R.string.direct_greater_lesser_error,position,x,y));
                }
                break;
            default:
                Toast.makeText(mContext, "Congiguration error. Operator is out of the system assignment", Toast.LENGTH_SHORT).show();
        }
        constraint.setSuccess(success);
        return constraint;
    }

    private Constraint minMaxCheck(int value, int min, int max,int position) {
        boolean minMax = false;
        Constraint constraint = new Constraint();
        if (value >= min && value <= max) {
            minMax = true;
        }
        constraint.setSuccess(minMax);
        if (!minMax){
            constraint.setErrorMsg(mContext.getResources().getString(R.string.min_max_error_msg,position,min,max));
        }
        return constraint;
    }


    private Constraint multipleCalculationDropdown(int value, JSONObject displayConfig) throws JSONException {
        Constraint constraint = new Constraint();

        // get Question Information
        String calculation = questionInfo.getDisplayConfig().getString(CoffeeConstants.CALCULATION);

        JSONObject calculationObj = new JSONObject(calculation);

        // Question No
        int  questionPosition=calculationObj.optInt(CoffeeConstants.QUESTION_NUMBER,0);


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

                    if (isQuestionAvailableInSubModule(questionList.get(i))){
                        // get the answers from answer set
                        double answer= getAnswerFromSameSubmodule(questionList.get(i));
                        if (answer == -1){
                            errorEmptyList.add(i);
                        }else {
                            int questionNo = i + 1;
                            answerHashMap.put("q" + questionNo, answer);
                        }
                    }else {

                        Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionList.get(i));
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
                        }else{
                            errorEmptyList.add(i);
                        }
                    }
                }

                // Collecting answer for dropdown Question and added in answer Hash Map
            int selectedLookupValue=-1;
            if (isQuestionAvailableInSubModule(Integer.parseInt(dropdownQuestion))){
                selectedLookupValue =getLookUpValueAnswerSameModule(Integer.parseInt(dropdownQuestion));
                if (selectedLookupValue == -1){
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
                    constraint= doCalculationWithLookUpValue(formulaList,answerHashMap,limitsList,selectedLookupValue,lookUpValueList,questionPosition);
                }
       //     }


            return constraint;
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
                //   String datatype = currentAnswer.getString("datatype");
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
             //   String datatype = currentAnswer.getString("datatype");
            }
        }
        return answer;
    }



    private Constraint doCalculationWithLookUpValue(ArrayList<String> formulaList, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, int selectedLookupValue, ArrayList<Integer> lookUpValueList,int position) {
        Constraint constraint=new Constraint();
//        if(formulaList.get(0).equalsIgnoreCase("q1/q2>x") &&
//                formulaList.get(0).equalsIgnoreCase("q1/q2>y")) {
        if (selectedLookupValue !=0) {
            for (int i=0; i<limitsList.size();i++) {
                if (selectedLookupValue == lookUpValueList.get(i)) {
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                        constraint.setSuccess(false);
                        constraint.setErrorMsg(mContext.getResources().getString(R.string.error_msg_dropdown,limitsList.get(i),position));
                    } else {
                        constraint.setSuccess(true);
                    }
                }
            }
        }
        return constraint;
    }


    private String generateErrorMsgByQuestionId(ArrayList<Integer> errorEmptyList) {
        // arrange the proper position
        return "error msg by 2 nd and 6th Question";
    }




// Checking the certification call check
public boolean findSurveyStatus(int farmerId, int moduleId) throws JSONException {
    boolean submitted=false;
    Cursor farmer=dataLayer.getFarmerById(farmerId);
    String submittedModulesJSON = null;
    if (farmer.getCount() > 0) {
        farmer.moveToFirst();
        submittedModulesJSON= farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES));
    }

        String Samplestr = String.valueOf("{\"modules\":null}");
    if (Samplestr.equals(submittedModulesJSON)) {
        submittedModulesJSON = submittedModulesJSON.replaceAll("\"modules\":null", "");
    }
    JSONObject submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();
    boolean hasSubmittedModules = (submittedModules.has("modules"));
    JSONArray submittedModulesArray = null;
    if (hasSubmittedModules) {
        submittedModulesArray = submittedModules.getJSONArray("modules");
    }


    Cursor moduleAssignments = dataLayer.getFarmerModules(farmerId, mContext);
    if(moduleAssignments != null && moduleAssignments.moveToFirst()) {
        do {
            int currentId = moduleAssignments.getInt(moduleAssignments.getColumnIndex("_id"));
            if (currentId == moduleId) {
                String disabled_months_string = moduleAssignments.getString(moduleAssignments.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_DISABLED_MONTHS));
                List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
                Calendar currentDate = Calendar.getInstance();
                if (hasSubmittedModules) {
                    try {
                        int recurrence_frequency = moduleAssignments.getInt(moduleAssignments.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_FREQUENCY));
                        String recurrence_type = moduleAssignments.getString(moduleAssignments.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_TYPE));
                        Calendar calculationDate = (Calendar) currentDate.clone();
                        boolean disable_entry = false;

                        for (int i = 0; i < submittedModulesArray.length(); i++) {
                            JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                            JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");


                            if (submittedModuleDetails.has("id") && submittedModuleDetails.getInt("id") == currentId) {

                                if (recurrence_frequency > 0 && !disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                                    // for recurring
                                    try {
                                        // get last submitted date
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                                        Calendar lastSubmittedDate = Calendar.getInstance();

                                        //O20HS-84 - To enable the annual survey.
                                        calculationDate = (Calendar) currentDate.clone();
                                        //

                                        try {
                                            lastSubmittedDate.setTime(sdf.parse(submittedModuleDetails.getString("created_at")));
                                        } catch (ParseException e) {
                                            FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence date values", e));
                                            e.printStackTrace();
                                        }
                                        if (recurrence_type != null) {
                                            if (recurrence_type.equalsIgnoreCase("years")) {
                                                calculationDate.add(Calendar.YEAR, -recurrence_frequency);
                                                calculationDate.set(Calendar.MONTH, Calendar.DECEMBER);
                                                calculationDate.set(Calendar.DAY_OF_MONTH, 31);
                                            } else if (recurrence_type.equalsIgnoreCase("months")) {
                                                calculationDate.add(Calendar.MONTH, -recurrence_frequency);
                                                calculationDate.set(Calendar.DAY_OF_MONTH, calculationDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                                            } else if (recurrence_type.equalsIgnoreCase("weeks")) {
                                                calculationDate.add(Calendar.WEEK_OF_YEAR, -recurrence_frequency);
                                                calculationDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                                            } else {
                                                // must be days
                                                calculationDate.add(Calendar.DAY_OF_YEAR, -recurrence_frequency);
                                            }

                                            // set the time
                                            calculationDate.set(Calendar.HOUR_OF_DAY, 23);
                                            calculationDate.set(Calendar.MINUTE, 59);
                                            calculationDate.set(Calendar.SECOND, 59);
                                            calculationDate.set(Calendar.MILLISECOND, 999);

                                            // force the recalculation
                                            calculationDate.get(Calendar.DAY_OF_MONTH);


                                            if (lastSubmittedDate.after(calculationDate)) {
                                                disable_entry = true;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // for non-recurring
                                    disable_entry = true;
                                }

                                if (disable_entry) {
                                    submitted = true;
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence values", e));
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!submitted) {
                    if ((moduleAssignments.getInt(moduleAssignments.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (moduleAssignments.getInt(moduleAssignments.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 0)) {
                        submitted=false;
                    } else if ((moduleAssignments.getInt(moduleAssignments.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (moduleAssignments.getInt(moduleAssignments.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 1)) {
                        submitted=true;
                    } else {
                        submitted=false;
                        if (disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                            submitted=true;
                        }
                    }
                }
            }
        } while (moduleAssignments.moveToNext());
    }

    return submitted;
}


// Multiple Calculation Country
private Constraint multipleCalculationCountry(int value, JSONObject displayConfig) throws JSONException {
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

    String innerOperation = calculationObj.getString(CoffeeConstants.INNER_OPERATION);
    String outerOperation = calculationObj.getString(CoffeeConstants.OUTER_OPERATION);

    // formula  Info
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


    if (formulaList.size() == 2 ) {
//            if (formulaList.get(0).equalsIgnoreCase("q1/q2>x") &&
//                    formulaList.get(0).equalsIgnoreCase("q1/q2>y")) {
        int farmerModuleId = (int) Utilities.farmerModuleId;


        // collecting answers for Questions In Order
        HashMap<String,Double> answerHashMap= new HashMap<>();
        ArrayList<Integer> errorEmptyList=new ArrayList<>();
        for (int i=0; i< questionList.size();i++) {

            if (isQuestionAvailableInSubModule(questionList.get(i))){
                // get the answers from answer set
                double answer= getAnswerFromSameSubmodule(questionList.get(i));
                if (answer == -1){
                    errorEmptyList.add(i);
                }else {
                    int questionNo = i + 1;
                    answerHashMap.put("q" + questionNo, answer);
                }
            }else {

                Cursor answerQuery = dataLayer.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionList.get(i));
                if (answerQuery.getCount() > 0) {
                    while (answerQuery.moveToNext()) {
                        String datatype = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                        double answer = 0d;
                        if (datatype.equalsIgnoreCase("int")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                        } else if (datatype.equalsIgnoreCase("decimal")) {
                            answer = Double.parseDouble(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                        }
                        int questionNo = i + 1;
                        answerHashMap.put("q" + questionNo, answer);
                    }
                }else{
                    errorEmptyList.add(i);
                }
            }
        }

        // Country Id Check
        Cursor farmer=dataLayer.getFarmerById(Utilities.farmerId);
        int  countryId = 0;
        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
           countryId= Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));
        }

        // Muliple country arrays
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
            constraint.setSuccess(false);
            String errorMsg = generateErrorMsgByQuestionId(errorEmptyList);
            constraint.setErrorMsg(errorMsg);
        }else{
            constraint= doCalculationWithMultipleCountry(formulaList,answerHashMap,limitsList,countryId,countryList);
        }
        //     }


        return constraint;
    }
    return  constraint;
}

    private Constraint doCalculationWithMultipleCountry(ArrayList<String> formulaList, HashMap<String, Double> answerHashMap, ArrayList<Integer> limitsList, int countryId, ArrayList<ArrayList<Integer>> countryList) {
        Constraint constraint=new Constraint();
//        if(formulaList.get(0).equalsIgnoreCase("q1/q2>x") &&
//                formulaList.get(0).equalsIgnoreCase("q1/q2>y")) {
        if (countryId !=0) {
            for (int i=0; i<limitsList.size();i++) {
                if (countryList.get(i).contains(countryId)) {
                    if (answerHashMap.get("q1") / answerHashMap.get("q2") > limitsList.get(i)) {
                        constraint.setSuccess(false);
                        constraint.setErrorMsg("failed the data exceeds for multiple country");
                    } else {
                        constraint.setSuccess(true);
                    }
                }
            }
        }
        return constraint;
    }

}


