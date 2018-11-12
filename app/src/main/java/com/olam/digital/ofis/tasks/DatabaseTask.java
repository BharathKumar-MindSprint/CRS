package com.olam.digital.ofis.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
abstract class DatabaseTask extends AsyncTask<JSONObject, Integer, String> {

    private SQLiteDatabase db;

    DatabaseTask() {
    }

    DatabaseTask(Context context) {
        db = Utilities.getWriteableDb(context);
    }

    void updateTasks(JSONArray jsonArray) {
        db.beginTransaction();
        db.delete("tasks", null, null);
        JSONObject item;
        int length = jsonArray.length();

        publishProgress(0, length);

        for (int j = 0; j < length; j++) {
            try {
                item = jsonArray.getJSONObject(j);
                int itemId = item.optInt(OFISDBContract.Task.COLUMN_NAME_ID, 0);
                String itemText = item.optString(OFISDBContract.Task.COLUMN_NAME_TASKTEXT, "");

                if (itemId > 0 && itemText.length() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.Task.COLUMN_NAME_ID, itemId);
                    values.put(OFISDBContract.Task.COLUMN_NAME_TASKTEXT, itemText);

                    String[] columns = new String[]{"*"};
                    String whereClause = OFISDBContract.Task.COLUMN_NAME_ID + " = ?";
                    String[] whereArgs = new String[]{String.valueOf(itemId)};

                    Cursor existingTask = db.query(OFISDBContract.Task.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

                    if (existingTask.getCount() > 0) {
                        db.update(OFISDBContract.Task.TABLE_NAME, values, whereClause, whereArgs);
                    } else {
                        values.put(OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR, item.optString(OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR, ""));
                        values.put(OFISDBContract.Task.COLUMN_NAME_CREATED_DATE, item.optString(OFISDBContract.Task.COLUMN_NAME_CREATED_DATE, null));
                        values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED, item.optInt(OFISDBContract.Task.COLUMN_NAME_COMPLETED, 0));
                        values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, item.optString(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, null));
                        values.put(OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID, item.optInt(OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID, 0));
                        db.insert(OFISDBContract.Task.TABLE_NAME, null, values);
                    }
                    existingTask.close();
                }

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error updating tasks", e));
                e.printStackTrace();
            }

            publishProgress(j, length);

        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    void fillTable(JSONArray jsonArray, String tableName, List columnList) {
        db.delete(tableName, null, null);

        @SuppressWarnings("deprecation") DatabaseUtils.InsertHelper insertHelper = new DatabaseUtils.InsertHelper(db, tableName);

        // generate array of table column ids
        ArrayList<Integer> columnKeys = new ArrayList<>();
        for (int i = 0; i < columnList.size(); i++) {
            columnKeys.add(insertHelper.getColumnIndex(columnList.get(i).toString()));
        }

        // now loop through array and insert in to table
        db.beginTransaction();
        JSONObject item = new JSONObject();
        int length = jsonArray.length();
        publishProgress(0, length);

        for (int j = 0; j < length; j++) {
            try {
                item = jsonArray.getJSONObject(j);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error getting JSON object for table filling", e));
                e.printStackTrace();
            }
            insertHelper.prepareForInsert();

            // bind the columns we are after
            for (int k = 0; k < columnKeys.size(); k++) {
//                try {
                    if(columnList.get(k).toString() != null)
                        insertHelper.bind(columnKeys.get(k), item.optString(columnList.get(k).toString()));

//                } catch (JSONException e) {
//                    FirebaseCrash.report(new Exception("DatabaseTask: Error binding column for table filling", e));
//                    e.printStackTrace();
//                }
            }

            insertHelper.execute();

            publishProgress(j, length);

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        if (tableName.equals(OFISDBContract.FarmerEntry.TABLE_NAME)) {
            int lastItemId;
            try {
                lastItemId = item.getInt("id");
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ID, (lastItemId + 100000));
                long newSeedId = db.insert(OFISDBContract.FarmerEntry.TABLE_NAME, null, values);
                if (newSeedId != 0) {
                    db.delete(OFISDBContract.FarmerEntry.TABLE_NAME, OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(newSeedId)});
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error creating seed for farmer table", e));
                e.printStackTrace();
            }
        }

        if (tableName.equals(OFISDBContract.PlacesEntry.TABLE_NAME)) {
            int lastPlaceId;
            try {
                lastPlaceId = item.getInt("id");
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_ID, (lastPlaceId + 100000));
                long newPlaceSeedId = db.insert(OFISDBContract.PlacesEntry.TABLE_NAME, null, values);
                if (newPlaceSeedId != 0) {
                    db.delete(OFISDBContract.PlacesEntry.TABLE_NAME, OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(newPlaceSeedId)});
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error creating seed for places table", e));
                e.printStackTrace();
            }
        }
    }

    void fillTableConditinalOptional(JSONArray jsonArray, String tableName, List columnList) {
        db.delete(tableName, null, null);

        @SuppressWarnings("deprecation") DatabaseUtils.InsertHelper insertHelper = new DatabaseUtils.InsertHelper(db, tableName);

        // generate array of table column ids
        ArrayList<Integer> columnKeys = new ArrayList<>();
        for (int i = 0; i < columnList.size(); i++) {
            columnKeys.add(insertHelper.getColumnIndex(columnList.get(i).toString()));
        }

        // now loop through array and insert in to table
        db.beginTransaction();
        JSONObject item = new JSONObject();
        int length = jsonArray.length();
        publishProgress(0, length);

        for (int j = 0; j < length; j++) {
            try {
                if (item.getString("is_subset_questions_required").equalsIgnoreCase("true")) {
                    JSONObject object = jsonArray.getJSONObject(j);
                    JSONArray SubQuestion = object.getJSONArray("conditional_questions");
                    for (int lengtharray = 0; lengtharray > object.length(); lengtharray++) {

                      //  item =jsonArray.getJSONObject(j)
                    }


                } else {
                    item = jsonArray.getJSONObject(j);

                }


            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error getting JSON object for table filling", e));
                e.printStackTrace();
            }
            insertHelper.prepareForInsert();

            // bind the columns we are after
            for (int k = 0; k < columnKeys.size(); k++) {
                try {
                    if(columnList.get(k).toString()!= null)
                        insertHelper.bind(columnKeys.get(k), item.getString(columnList.get(k).toString()));

                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("DatabaseTask: Error binding column for table filling", e));
                    e.printStackTrace();
                }
            }

            insertHelper.execute();

            publishProgress(j, length);

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        if (tableName.equals(OFISDBContract.FarmerEntry.TABLE_NAME)) {
            int lastItemId;
            try {
                lastItemId = item.getInt("id");
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ID, (lastItemId + 100000));
                long newSeedId = db.insert(OFISDBContract.FarmerEntry.TABLE_NAME, null, values);
                if (newSeedId != 0) {
                    db.delete(OFISDBContract.FarmerEntry.TABLE_NAME, OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(newSeedId)});
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error creating seed for farmer table", e));
                e.printStackTrace();
            }
        }

        if (tableName.equals(OFISDBContract.PlacesEntry.TABLE_NAME)) {
            int lastPlaceId;
            try {
                lastPlaceId = item.getInt("id");
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_ID, (lastPlaceId + 100000));
                long newPlaceSeedId = db.insert(OFISDBContract.PlacesEntry.TABLE_NAME, null, values);
                if (newPlaceSeedId != 0) {
                    db.delete(OFISDBContract.PlacesEntry.TABLE_NAME, OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(newPlaceSeedId)});
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DatabaseTask: Error creating seed for places table", e));
                e.printStackTrace();
            }
        }
    }
}