package com.webpoint0.ofiscompanion.tasks;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.webpoint0.ofiscompanion.OFISDBContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateSpinnerListsTask extends DatabaseTask {

    public UpdateSpinnerListsTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {

            JSONObject spinnerResponse = params[0];

            if (spinnerResponse.getInt("resultcount") > 0) {

                JSONObject response = spinnerResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray countryArray, regionArray, districtArray, placeArray, farmergroupArray, sectionArray, productArray;

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
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error getting spinner lists response", e));
            e.printStackTrace();
        }
        return null;
    }
}
