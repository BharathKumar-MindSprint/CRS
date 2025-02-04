package com.olam.digital.ofis.mvvmmodule;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Constants {


    public static String MODULE_ID = "0";

    public static String MODULE_NAME = "0";

    public static String FARMER_ID = "0";
    // OW-4574
    public static String FARMER_NAME = "0";
    public static String FARMER_GROUP_ID = "0";
    public static String ENUMERATOR_ID = "0";
    public static String ACTIONPlAN_ID = "0";
    public static String complaintSize = "0";
    public static String noncomplaintSize = "0";
    public static String satisfactorySize = "0";
    public static int ATTEMPT = 0;
    public static Boolean EDITABLE = false;

    public static ArrayList<String> ComplaintQuestionIds = new ArrayList<>();
    public static ArrayList<String> questionIds = new ArrayList<>();
    public static ArrayList<String> questionIdsSection = new ArrayList<>();

    public static ArrayList<String> mandatedQuestionIds = new ArrayList<>();


    public static String RED = "RED";
    public static String GREEN = "GREEN";
    public static final String ORANGE = "YELLOW";

    public static String RESOLVED = "Resolved";

    public static String NOTRESOLVED = "NOT Resolved";

    public static String TotalActionPlanCount = "0";

    public static int capture_count;

    public static ArrayList<String> questionIdsFollowUp = new ArrayList<>();

    public static int PLOT_ID = 0;

    public static String CHECKBOX = "checkbox";

    public static String COMPLIANT = "Compliant";
    public static String NON_COMPLIANT = "Non Compliant";
    public static String SATISFACTORY = "Satisfactory";


    public static Set<Integer> getUniqueQuestion(List<DmSurveyAnswerTable> dmSurveyAnswerTableList) {
        Set<Integer> uniqueQuestionIds = new HashSet<>();
        for (DmSurveyAnswerTable answer : dmSurveyAnswerTableList) {
            uniqueQuestionIds.add(Integer.valueOf(answer.questionID));
        }
        return uniqueQuestionIds;
    }

}
