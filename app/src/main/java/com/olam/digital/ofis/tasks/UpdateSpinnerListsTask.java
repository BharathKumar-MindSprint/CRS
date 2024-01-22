package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Programme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateSpinnerListsTask extends DatabaseTask {

    ProgressDialog pd;

    public UpdateSpinnerListsTask(Context context, ProgressDialog pd) {
        super(context);
        Log.e("UpdateSpinnerListsTask:","inside cons");
        this.pd = pd;
        pd = Utilities.updateProgressDialog(pd, 0, 0, context.getString(R.string.data_initialization));
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {
            Log.e("UpdateSpinnerListsTask:","inside doin");

            JSONObject spinnerResponse = params[0];

            if (spinnerResponse.getInt("resultcount") > 0) {

                JSONObject response = spinnerResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray countryArray, regionArray, districtArray, placeArray, farmergroupArray, sectionArray, productArray, subdistrictArray;
                List<Certifications> certificationsList;
                List<Programme> programmeList;
                List<Partners> partnersList;

                try {
                    countryArray = response.getJSONArray(OFISDBContract.CountriesEntry.TABLE_NAME);
                    fillTable(countryArray, OFISDBContract.CountriesEntry.TABLE_NAME, OFISDBContract.CountriesEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling countries table", e));
                    e.printStackTrace();
                }
                try {
                    regionArray = response.getJSONArray(OFISDBContract.RegionsEntry.TABLE_NAME);
                    fillTable(regionArray, OFISDBContract.RegionsEntry.TABLE_NAME, OFISDBContract.RegionsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling regions table", e));
                    e.printStackTrace();
                }
                try {
                    districtArray = response.getJSONArray(OFISDBContract.DistrictsEntry.TABLE_NAME);
                    fillTable(districtArray, OFISDBContract.DistrictsEntry.TABLE_NAME, OFISDBContract.DistrictsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling districts table", e));
                    e.printStackTrace();
                }

//              adding subdistrict data into database
                try {
                    subdistrictArray = response.getJSONArray(OFISDBContract.SubDistrictsEntry.TABLE_NAME);
                    fillTable(subdistrictArray, OFISDBContract.SubDistrictsEntry.TABLE_NAME, OFISDBContract.SubDistrictsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling districts table", e));
                    e.printStackTrace();
                }

                try {
                    placeArray = response.getJSONArray(OFISDBContract.PlacesEntry.TABLE_NAME);
                    fillTable(placeArray, OFISDBContract.PlacesEntry.TABLE_NAME, OFISDBContract.PlacesEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling places table", e));
                    e.printStackTrace();
                }
                try {
                    farmergroupArray = response.getJSONArray(OFISDBContract.FarmerGroupsEntry.TABLE_NAME);
                    fillTable(farmergroupArray, OFISDBContract.FarmerGroupsEntry.TABLE_NAME, OFISDBContract.FarmerGroupsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling farmergroups table", e));
                    e.printStackTrace();
                }
                try {
                    sectionArray = response.getJSONArray(OFISDBContract.SectionsEntry.TABLE_NAME);
                    fillTable(sectionArray, OFISDBContract.SectionsEntry.TABLE_NAME, OFISDBContract.SectionsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling sections table", e));
                    e.printStackTrace();
                }
                try {
                    productArray = response.getJSONArray(OFISDBContract.ProductsEntry.TABLE_NAME);
                    fillTable(productArray, OFISDBContract.ProductsEntry.TABLE_NAME, OFISDBContract.ProductsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling products table", e));
                    e.printStackTrace();
                }
                OFISRoomDB roomDB = OFISRoomDB.getDbInstance(context);

                try {
                    Log.e("UpdateSpinnerListsTask:","inside try");
                    Gson gson = new Gson();
                    JSONArray certificationsArray=null;
                    if(!response.isNull("certifications")){
                        certificationsArray = response.getJSONArray("certifications");

                    }
                    roomDB.certificationsDao().deleteAll();
                    if(certificationsArray!=null) {
                        Type type = new TypeToken<List<Certifications>>() {
                        }.getType();
                        certificationsList = gson.fromJson(String.valueOf(certificationsArray), type);
                        Log.e("UpdateSpinnerListsTask:", "certificationsList");
//                    certificationsArray = response.getJSONArray(Certifications.class.getSimpleName());
                        roomDB.certificationsDao().insertAll(certificationsList);
                    }
//                    fillTable(productArray, OFISDBContract.ProductsEntry.TABLE_NAME, OFISDBContract.ProductsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    Log.e("UpdateSpinnerListsTask:","inside catch");
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling Certification table", e));
                    e.printStackTrace();
                }
                try {
                    Gson gson = new Gson();
                    JSONArray programmeArray = response.getJSONArray("programme");
                    Type type = new TypeToken<List<Programme>>() {}.getType();
                    programmeList = gson.fromJson(String.valueOf(programmeArray), type);

                    roomDB.programmeDao().insertAll(programmeList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling Programme table", e));
                    e.printStackTrace();
                }
                try {
                    Gson gson = new Gson();
                    JSONArray partnersArray = response.getJSONArray("partners");
                    Type type = new TypeToken<List<Partners>>() {}.getType();
                    partnersList = gson.fromJson(String.valueOf(partnersArray), type);
                    roomDB.partnersDao().insertAll(partnersList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error filling Partners table", e));
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error getting spinner lists response", e));
            e.printStackTrace();
        }
        return null;
    }
}
