package com.olam.digital.ofis.recordpolygon;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        mSharedPreferencesEditor.putString(aPlotKey, json);
        mSharedPreferencesEditor.commit();

        return json;
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


    String getFarmID() {
        return mSharedPreferences.getString(AppConstants.KEY_FARM_ID, "");
    }

    public void setFarmID(String aFarmID) {
        mSharedPreferencesEditor.putString(AppConstants.KEY_FARM_ID, aFarmID);
        mSharedPreferencesEditor.commit();
    }


    LocationBeanList getLocationBeanList(String aPlotKey) {
        aPlotKey += getFarmID();
        String json = mSharedPreferences.getString(aPlotKey, "");
        return new Gson().fromJson(json, LocationBeanList.class);
    }

    public void removePlotUsingKey(String aPlotKey) {
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

    public List<LocationBeanList> removeAllLocations(String olam_farmer_id) {
        int count = getPlotCount(olam_farmer_id);
        if (count == 0) {
            return null;
        }
        List<LocationBeanList> lListLocationBean = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String key = "Plot " + (i + 1);
            key += olam_farmer_id;
            mSharedPreferencesEditor.remove(key);
            mSharedPreferencesEditor.apply();
        }
        if (lListLocationBean.size() > 0) {
            return lListLocationBean;
        }
        return null;
    }

    public void setPhotoCheckBoxContainer(String value) {
        mSharedPreferencesEditor.putString(AppConstants.CONTAINER_PHOTOCHECKBOX, value);
        mSharedPreferencesEditor.commit();
    }

    public String getPhotoCheckBoxContainer() {
        return mSharedPreferences.getString(AppConstants.CONTAINER_PHOTOCHECKBOX, "");
    }

    public void setPhotoCheckBoxSyncDate(String value) {
        mSharedPreferencesEditor.putString(AppConstants.SYNC_DATE_PHOTOCHECKBOX, value);
        mSharedPreferencesEditor.commit();
    }

    public String getPhotoCheckBoxSyncDate() {
        return mSharedPreferences.getString(AppConstants.SYNC_DATE_PHOTOCHECKBOX, null);
    }

    public void setPhotoCheckBoxModuleNotInUse(ArrayList<Integer> value) {
        String moduleNotInUse = new Gson().toJson(value);
        mSharedPreferencesEditor.putString(AppConstants.MODULE_NOT_IN_USE_PHOTOCHECKBOX, moduleNotInUse);
        mSharedPreferencesEditor.commit();
    }

    public ArrayList<Integer> getPhotoCheckBoxModuleNotInUse() {
        String moduleNotInUse = mSharedPreferences.getString(AppConstants.MODULE_NOT_IN_USE_PHOTOCHECKBOX, null);
        if (moduleNotInUse != null) {
            try {
                return new Gson().fromJson(moduleNotInUse, new TypeToken<List<Integer>>() {}.getType());
            } catch (Exception e) {
                Log.e("TAG", "Error retrieving module not in use list", e);
            }
        }
        return new ArrayList<>();
    }
}
