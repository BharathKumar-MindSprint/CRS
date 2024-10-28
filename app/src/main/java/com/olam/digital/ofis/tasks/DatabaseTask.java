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
    Context context;

    DatabaseTask(Context context) {
        db = Utilities.getWriteableDb(context);
        this.context = context;
    }

    void updateTasks(JSONArray jsonArray) {
        db.beginTransaction();
        db.delete("tasks", null, null);
        if(jsonArray != null) {
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
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    void updateNotification(JSONArray jsonArray) throws JSONException {
        DataLayer dl = new DataLayer(context.getResources(),context);
        ArrayList<Integer> existingFarmerList = dl.getNotificationFarmers();
        ArrayList<Integer> currentFarmerList = new ArrayList<>();
        db.beginTransaction();
        if(jsonArray != null) {
            JSONObject item;
            int length = jsonArray.length();

            publishProgress(0, length);
            for (int k = 0; k < length; k++) {
                JSONObject data = jsonArray.getJSONObject(k);
                int farmerId = data.optInt(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID, 0);
                currentFarmerList.add(farmerId);
            }
            ArrayList<Integer> removableList = new ArrayList<Integer>(existingFarmerList);
            removableList.removeAll(currentFarmerList);
            dl.deleteNotificationByFarmerId(removableList);
            for (int j = 0; j < length; j++) {
                try {
                    item = jsonArray.getJSONObject(j);
                    int farmerId = item.optInt(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID, 0);
                    int moduleId = item.optInt(OFISDBContract.NotificationAssignments.COLUMN_MODULEID, 0);
                    int childId = item.optInt(OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID, 0);
                    String createdAt = item.optString(OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT, "");

                    if (farmerId > 0 && createdAt.length() > 0) {
                        ContentValues values = new ContentValues();

                        String[] columns = new String[]{"*"};
                        String whereClause = OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID + " = ? AND "
                                            + OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID + " = ? AND "
                                            + OFISDBContract.NotificationAssignments.COLUMN_MODULEID + " = ? AND "
                                            + OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT + " = ?";
                        String[] whereArgs = new String[]{String.valueOf(farmerId),String.valueOf(childId),
                                String.valueOf(moduleId),createdAt};

                        Cursor existingNotification = db.query(OFISDBContract.NotificationAssignments.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

                        if ( existingNotification.getCount() == 0) {
                            values.put(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID, item.optInt(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID, 0));
                            values.put(OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID, item.optInt(OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID, 0));
                            values.put(OFISDBContract.NotificationAssignments.COLUMN_MODULEID, item.optInt(OFISDBContract.NotificationAssignments.COLUMN_MODULEID, 0));
                            values.put(OFISDBContract.NotificationAssignments.COLUMN_STATUS, item.optString(OFISDBContract.NotificationAssignments.COLUMN_STATUS, null));
                            values.put(OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT, item.optString(OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT, ""));
                            db.insert(OFISDBContract.NotificationAssignments.TABLE_NAME, null, values);
                        }
                        existingNotification.close();
                    }

                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("DatabaseTask: Error updating tasks", e));
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
                publishProgress(j, length);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    void fillTable(JSONArray jsonArray, String tableName, List columnList) {
      if ((! tableName.equalsIgnoreCase("stakeHolders"))
              && (tableName != OFISDBContract.SavedAnswerEntry.TABLE_NAME)
              && (tableName != OFISDBContract.DownloadedSurveyList.TABLE_NAME)){
              db.delete(tableName, null, null);
      }


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
                if (columnList.get(k).toString() != null) {
                    if (tableName.equals(OFISDBContract.FarmerEntry.TABLE_NAME)) {
                        if (columnList.get(k).toString() == OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID) {
                            insertHelper.bind(columnKeys.get(k), item.optString("partnerId"));
                        } else if (columnList.get(k).toString() == OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID) {
                            insertHelper.bind(columnKeys.get(k), item.optString("programId"));
                        } else {
                            insertHelper.bind(columnKeys.get(k), item.optString(columnList.get(k).toString()));
                        }
                    } else {
                        insertHelper.bind(columnKeys.get(k), item.optString(columnList.get(k).toString()));
                    }
                }
            }

            insertHelper.execute();

            publishProgress(j, length);

        }



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
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_MODERATION_STATUS, false);
            db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, null, null);
        }

        if(tableName.equals(OFISDBContract.FarmerNationalIDs.TABLE_NAME)){
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, true);
            db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, null, null);
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
        db.setTransactionSuccessful();
        db.endTransaction();
    }


}