package com.olam.digital.ofis.childlabour;

import android.content.Context;

import com.olam.digital.ofis.R;
//  Static text mapped in object

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChildLabourUtils {

    public static final String VILLAGE_NAME="village";
    public static final String MALE="Male";
    public static final String FEMALE="Female";
    public static final String IS_CHILD_ENUMERATOR="child_enumerator";
    public static final String CHILD_FARMER_ID="farmer_id";
    public static final String CHILD_ID="child_id";
    public static final String REG_CHILD = "registerchild";
    public static final String CHILDLABOUR_SURVEY = "childlaboursurvey";

    public static final String HOUSEHOLD_SURVEY_CODE="01";
    public static final String INSPECTION_SURVEY_CODE="02";
    public static final String FOLLOWUP_SURVEY_CODE="03";
    public static final String SURVEY_YEAR ="2019";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String ACTION_DATE = "actionDate";
    public static final String CHILD_LABOUR_ACTION_TICK_LIST = "actionTickList";
    public static final String NOT_IN_CL = "Not in CL";
    public static final String ACTION_TICK = "Action Ticked";
    public final static String INSP_SURVEY_STATUS = "inspectionEnabled";
    public final static String FLWUP_SURVEY_STATUS = "followupEnabled";
    public final static String HH_SURVEY_ENABLE  = "householdEnabled";
    public static boolean isChildEnumerator=false;

    public static final String ACTION="action";
    public static final String NEW="new";
    public static final String EDIT="edit";
    public static final String SYNC_EDIT="sync_edit";

    public static final String HOUSEHOLD_ENABLED="householdEnabled";
    public static final String INSPECTION_ENABLED="inspectionEnabled";
    public static final String FOLLOWUP_ENABLED="followupEnabled";
    public static final String PAGENUMBER_CONSTANT="&pageNo=";
    public static final String LIMIT_CONSTANT="&limit=";
    public static final String MODULE_CONSTANT="&module=";


    public static String getTranslatedGender(Context mContext, String gender) {
        String sex;
        if (gender.equalsIgnoreCase(MALE)){
            sex=mContext.getResources().getString(R.string.gender_male);
        }else if (gender.equalsIgnoreCase(FEMALE)){
            sex=mContext.getResources().getString(R.string.gender_female);
        }else {
            sex="";
        }

        return sex;
    }

    public static String getAfterThreeMonths(String actionTickDate) {
        String actionTickString=actionTickDate.substring(0,10);
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date3Months=null;
        try {
            Date date = formatter.parse(actionTickString);
            cal.setTime(date);
            cal.add(Calendar.MONTH, 3);
            Date MonthsDate=cal.getTime();
            date3Months=formatter.format(MonthsDate);
        } catch (ParseException e) {e.printStackTrace();}
        return date3Months;
    }
}
