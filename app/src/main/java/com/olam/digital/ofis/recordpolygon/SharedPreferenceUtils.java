package com.olam.digital.ofis.recordpolygon;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by M1032987 on 10/23/2017.
 */

public class SharedPreferenceUtils {

    private static SharedPreferenceUtils mSharedPreferenceUtils;
    protected Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private SharedPreferenceUtils(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    /**
     * Creates single instance of SharedPreferenceUtils
     *
     * @param context context of Activity or Service
     * @return Returns instance of SharedPreferenceUtils
     */
    public static synchronized SharedPreferenceUtils getInstance(Context context) {

        if (mSharedPreferenceUtils == null) {
            mSharedPreferenceUtils = new SharedPreferenceUtils(context.getApplicationContext());
        }
        return mSharedPreferenceUtils;
    }

    String setLocationBeanList(String aPlotKey, LocationBeanList aLocationBeanList) {
        aPlotKey += getFarmID();
        String json = aLocationBeanList == null ? null : new Gson().toJson(aLocationBeanList);
//        if (json != null && json.length() > 0) {
//            Toast.makeText(mContext, aPlotKey + " getLocationBeanList " + json.substring(0, 15), Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(mContext, aPlotKey + " getLocationBeanList No data", Toast.LENGTH_LONG).show();
//        }
        mSharedPreferencesEditor.putString(aPlotKey, json);
        mSharedPreferencesEditor.commit();

        return json;
    }

    public boolean isPlotsRecordingFinished(int lFarmID) {
        setFarmID("" + lFarmID);
        int count = getPlotCount(""+lFarmID);
        if (count == 0) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            String key = "Plot " + (i + 1);
            LocationBeanList lLocationBeanList = getLocationBeanList(key);
            if (lLocationBeanList == null) {
                return false;
            }
        }
        return true;
    }

    public List<LocationBeanList> getAllLocations() {
        int count = getPlotCount(getFarmID());
        if (count == 0) {
            return null;
        }
        List<LocationBeanList> lListLocationBean = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String key = "Plot " + (i + 1);
            LocationBeanList lLocationBeanList = getLocationBeanList(key);
            if (lLocationBeanList != null) {
                lListLocationBean.add(lLocationBeanList);
            }
        }
        if (lListLocationBean.size() > 0) {
            return lListLocationBean;
        }
        return null;
    }

    public void setFarmerID(String key, String value) {
        mSharedPreferencesEditor.putString(key, value);
        mSharedPreferencesEditor.commit();
    }

    public String getFarmerID(String key) {
        return mSharedPreferences.getString(key, "0");
    }

    public void setFarmID(String aFarmID) {
        mSharedPreferencesEditor.putString(AppConstants.KEY_FARM_ID, aFarmID);
        mSharedPreferencesEditor.commit();
    }

    String getFarmID() {
        return mSharedPreferences.getString(AppConstants.KEY_FARM_ID, "");
    }

    LocationBeanList getLocationBeanList(String aPlotKey) {
        aPlotKey += getFarmID();
        String json = mSharedPreferences.getString(aPlotKey, "");
//        if (json != null && json.length() > 0) {
//            Toast.makeText(mContext, aPlotKey + " getLocationBeanList " + json.substring(0, 15), Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(mContext, aPlotKey + " getLocationBeanList No data", Toast.LENGTH_LONG).show();
//        }
        return new Gson().fromJson(json, LocationBeanList.class);
    }

    void removePlotUsingKey(String aPlotKey) {
        aPlotKey += getFarmID();
        mSharedPreferencesEditor.remove(aPlotKey);
        mSharedPreferencesEditor.apply();
    }

    public void setPlotCount(String lFarmIDKey, int aPlotCount) {
        mSharedPreferencesEditor.putInt(lFarmIDKey, aPlotCount);
        mSharedPreferencesEditor.commit();
    }

    public int getPlotCount(String lFarmID) {
        return mSharedPreferences.getInt(lFarmID, 0);
    }
}
