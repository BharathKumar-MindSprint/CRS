package com.olam.digital.ofis.childlabour;

import android.content.Context;

import com.olam.digital.ofis.R;

public class ChildLabourUtils {

    public static final String VILLAGE_NAME="village";
    public static final String MALE="Male";
    public static final String FEMALE="Female";
    public static final String CHILD_LABOUR_ENDPOINT="ofismaster/master/ischildenumerator?enumeratorId=";
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

    public static boolean isChildEnumerator=false;

    public static final String ACTION="action";
    public static final String NEW="new";
    public static final String EDIT="edit";
    public static final String SYNC_EDIT="sync_edit";

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
}
