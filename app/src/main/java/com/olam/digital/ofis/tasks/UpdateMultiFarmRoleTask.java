package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("DefaultFileTemplate")
public class UpdateMultiFarmRoleTask  extends DatabaseTask {

        ProgressDialog pd;
        public UpdateMultiFarmRoleTask(Context context) {
            super(context);
            pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);

        }


    @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Utilities.closeProgressDialog(pd, 2);
        }

        @Override
        protected String doInBackground(JSONObject... params) {

            JSONObject multiFarmRoleResponse = params[0];
            JSONArray multiFarmRoleArray;
            if (multiFarmRoleResponse!=null) {
                    // get the various sub lists
                try {
                    multiFarmRoleArray = multiFarmRoleResponse.getJSONArray("roles");
                    fillTable(multiFarmRoleArray, OFISDBContract.MultiFarmRoles.TABLE_NAME, OFISDBContract.MultiFarmRoles.COLUMN_LIST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
