package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.model.NationalIdDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by bengallienne on 29/11/14.
 * Data functions for interacting with the SQLite database
 */
public class DataLayer {

    private static final String TAG = DataLayer.class.getSimpleName();


    private static Resources res;
    private static SQLiteDatabase db;

    // constructor - update
    public DataLayer(Resources res_, Context context_) {
        super();
        res = res_;
        db = Utilities.getWriteableDb(context_);
    }

    // data recording tasks
    boolean isFarmerIdUnique(String olamFarmerId, long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{olamFarmerId};
        if (farmerId != 0) {
            whereClause += " AND " + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " != ?";
            whereArgs = new String[]{
                    olamFarmerId,
                    String.valueOf(farmerId)
            };
        }

        Cursor existingFarmer = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        boolean idIsUnique = (existingFarmer.getCount() == 0);
        existingFarmer.close();

        return idIsUnique;
    }

    boolean placeAlreadyExists(String placeName, long district_id) {
        String[] columns = new String[]{"*"};
        String whereClause = "upper(" + OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME + ") = ? AND " + OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID + " = ?";
        String[] whereArgs = new String[]{
                placeName.toUpperCase(),
                String.valueOf(district_id)
        };

        Cursor existingPlace = db.query(OFISDBContract.PlacesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

        return (existingPlace.getCount() > 0);
    }

    long insertNewPlace(String newPlaceName, long district_id) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME, newPlaceName);
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID, district_id);
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE, "localinsert");

        return db.insert(OFISDBContract.PlacesEntry.TABLE_NAME, null, values);
    }

    Cursor getPlaceById(int placeId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(placeId)};

        return db.query(OFISDBContract.PlacesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public int updateFarmPolygon(long farmerId, String plotJson, String olamFarmerId, String farmerName) {
        int updated;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PLOTS, plotJson);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, values, whereClause, whereArgs);

        if (updated > 0) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            boolean updateWaiting = networkEntryTypeExistsByFarmerId("farmpolygon", farmerId);
            if (updateWaiting) {
                ContentValues nqiValues = new ContentValues();
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_PLOTS, plotJson);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                updateNetworkQueueFarmPolygonEntry(nqiValues, farmerId);
            } else {
                ContentValues nqiValues = new ContentValues();
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmpolygon");
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmerName);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.record_farm_polygon_complete));
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
//                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_PLOTS, plotJson);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                insertNetworkQueueEntry(nqiValues);
            }
        }

        return updated;
    }

    /**
     * store plot geo data with farm and plot id to table
     *
     * @param farmerId    unique farm id
     * @param plotName    plot name (Ex: plot1_farmid, plot2_farmid)
     * @param plotGeoData plot geo data json
     */
    public void updatePlotGeoData(String farmerId, String plotName, String plotGeoData) {

        ContentValues plotGeoValues = new ContentValues();
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME, plotName);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID, farmerId);

        //temp. added server_id to avoid null, later will update server_id with web-services response actual value (at sync with server)
        if (!plotGeoValues.containsKey(OFISDBContract.PlotGeoData.COLUMN_NAME_SERVER_ID)) {
            plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_SERVER_ID, UUID.randomUUID().toString());
        }

        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_JSON, plotGeoData);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SYNC_FLAG, "false"); //default is false, once it will sync with server it will update with true

        long plotGeoDBInsert = db.insert(OFISDBContract.PlotGeoData.TABLE_NAME, null, plotGeoValues);

        if (plotGeoDBInsert == -1) {
            Log.e(TAG, "Error to insert data into the PlotGeoData DB");
        }
    }

    public int fetchPlotData(String id){
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor data = db.query(OFISDBContract.PlotGeoData.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();

    }

    //------------------------Temp Functions for DB data check - Start-----------------------------
    public Cursor getTempData() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.TrainingModule.TABLE_NAME, columns, null, null, null, null, null);
    }

    public Cursor getTempCourseData() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.Course.TABLE_NAME, columns, null, null, null, null, null);
    }

    public Cursor getFarmerData() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, null, null, null, null, null);
    }
    //------------------------Temp Functions for DB data check - End-----------------------------

    public Cursor getPlotGeoData() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.PlotGeoData.TABLE_NAME, columns, null, null, null, null, null);
    }

    /**
     * update plot geo data table after sync success
     *
     * @param plotName unique plot name
     * @param serverId server id (get from server response after sync success)
     */
    public void updatePlotGeoDataAfterSync(String plotName, String serverId) {

        ContentValues updatePlotGeoDataAfterSyncValues = new ContentValues();
        updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_SERVER_ID, serverId);
        updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SYNC_FLAG, "true");

        String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " = ?";
        String[] whereArgs = new String[]{String.valueOf(plotName)};

        int updatedRows = db.update(OFISDBContract.PlotGeoData.TABLE_NAME, updatePlotGeoDataAfterSyncValues, whereClause, whereArgs);
    }

    int updateFarmerLocation(long farmerId, Location location, String olamFarmerId, int farmId, String farmerName) {
        int updated;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT, location.getLatitude());
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG, location.getLongitude());
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, values, whereClause, whereArgs);

        if (updated > 0) {
            ContentValues nqiValues = new ContentValues();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmlocation");
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmerName);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.record_farm_location_complete));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, values.getAsDouble(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, values.getAsDouble(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

            insertNetworkQueueEntry(nqiValues);
        }

        return updated;
    }

    long updateFarmer(ContentValues values, long farmerId) {

        long district_id = values.getAsLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        values.remove(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);

        int updated;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, values, whereClause, whereArgs);

        if (updated > 0) {
            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmer");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.new_farmer));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);

            updateNetworkQueueNewFarmerEntry(networkQueueValues, farmerId);
        }

        return updated;

    }

    long insertFarmer(ContentValues values) {
        long district_id = values.getAsLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        values.remove(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);

        long newRowId;
        newRowId = db.insert(OFISDBContract.FarmerEntry.TABLE_NAME, null, values);
        db.insert(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, null, values);

        if (newRowId != -1) {
            // add a network queue entry
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmer");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.new_farmer));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, newRowId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

            insertNetworkQueueEntry(networkQueueValues);
        }

        return newRowId;
    }

    private static void updateNetworkQueueNewFarmerEntry(ContentValues values, long farmerId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{
                "farmer",
                String.valueOf(farmerId)
        };

        db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

//    public void updateNetworkQueueFarmerModuleEntry(ContentValues values, long farmerModuleId) {
//        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
//        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
//
//        db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
//    }

    public void updateNetworkQueueFarmerAndChildModuleEntry(ContentValues values, long farmerModuleId, String type) {

        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(farmerModuleId),
                type
        };
        db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    int updateNetworkQueueFarmPolygonEntry(ContentValues values, long farmerId) {
        int updated;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        updated = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
        return updated;
    }

    public int updateNetworkQueueEntry(ContentValues values, long networkEntryId) {

        int updated;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkEntryId)};

        updated = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);

        return updated;
    }

    public long insertNetworkQueueEntry(ContentValues values) {
        long newRowId;
        if (!values.containsKey(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)) {
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, UUID.randomUUID().toString());
        }
        newRowId = db.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values);

        return newRowId;
    }

    boolean networkEntryTypeExistsByFarmerId(String submission_type, long farmerId) {
        String[] columns = new String[]{"id"};
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{
                submission_type,
                String.valueOf(farmerId)
        };

//      Changes - For SQLite Android Database Cursor window allocation of 2048 kb failed
        Cursor nqi = null;
        int nqicount = 0;
        try {
            nqi = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            nqicount = nqi.getCount();
        }finally {
            if(nqi != null)
                nqi.close();
        }
        return (nqicount > 0);
    }

    void deleteFmpReviewValues(long fmpId) {
        // delete existing values for this farmer and fmp
        String fmpValuesTable = OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME;
        String whereClause = OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(fmpId),
        };

        db.delete(fmpValuesTable, whereClause, whereArgs);
    }

    void updateFmpReviewValues(ArrayList<JSONObject> valueSet, long farmerId, long fmpId, String olamFarmerId, int fmpStartYear, int isUpdate) {
        try {

            if (isUpdate == 1) {
                deleteFmpReviewValues(fmpId);
            }

            for (int i = 0; i < valueSet.size(); i++) {
                JSONObject currentValue = valueSet.get(i);
                if (currentValue != null) {
                    int currentFmpId = currentValue.optInt("fmp_id");
                    int currentEntryId = currentValue.optInt("fmp_entry_id");
                    String entryValue = currentValue.getString("value");

                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID, currentFmpId);
                    values.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ENTRY_ID, currentEntryId);
                    values.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_YEAR, fmpStartYear);
                    values.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE, entryValue);

                    db.insert(OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME, null, values);

                }
            }

            if (isUpdate == 0) {
                ContentValues nqiValues = new ContentValues();
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "fmpreview");
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.review_farm_management_plan));
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.review_farm_management_plan_in_progress));
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID, fmpId);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED, fmpStartYear);
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

                insertNetworkQueueEntry(nqiValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void addOrUpdateTask(Map taskData, String TaskText) {
        if (taskData.get("task_id") != null && TaskText.length() > 0) {
            ContentValues values = new ContentValues();
            String tasktext = String.valueOf(taskData.get("task_id"));
            values.put(OFISDBContract.Task.COLUMN_NAME_ID, Integer.parseInt(tasktext));
            values.put(OFISDBContract.Task.COLUMN_NAME_TASKTEXT, TaskText);
            values.put(OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR, String.valueOf(taskData.get("task_creator")));
            values.put(OFISDBContract.Task.COLUMN_NAME_CREATED_DATE, String.valueOf(taskData.get("task_created")));
            values.put(OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID, String.valueOf(taskData.get("enumeratorTasksId")));

            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.Task.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{(String) taskData.get("task_id")};

            Cursor existingTask = db.query(OFISDBContract.Task.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

            if (existingTask.getCount() > 0) {
                values.put(OFISDBContract.Task.COLUMN_NAME_DELIVERY_CONFIRMED, 0);
                if (taskData.get("task_action").toString().equalsIgnoreCase("updateandreset")) {
                    values.put(OFISDBContract.Task.COLUMN_NAME_SYNCED, 0);
                    values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED, 0);
                    values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, "");
                }
                db.update(OFISDBContract.Task.TABLE_NAME, values, whereClause, whereArgs);
            } else {
                db.insert(OFISDBContract.Task.TABLE_NAME, null, values);
            }

            existingTask.close();
        }
    }

    void deleteTask(Map taskData) {
        if (taskData.get("task_id") != null) {
            String whereClause = OFISDBContract.Task.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{(String) taskData.get("task_id")};

            db.delete(OFISDBContract.Task.TABLE_NAME, whereClause, whereArgs);
        }
    }

    void deleteOldCompletedTasks() {
        String sql = "DELETE FROM " + OFISDBContract.Task.TABLE_NAME + " WHERE " + OFISDBContract.Task.COLUMN_NAME_COMPLETED
                + " = 1 AND " + OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE + " <= datetime('now','-2 days')";
        db.execSQL(sql);
    }

    Cursor getTasks() {
        String[] columns = new String[]{
                "id AS _id",
                OFISDBContract.Task.COLUMN_NAME_TASKTEXT,
                OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR,
                OFISDBContract.Task.COLUMN_NAME_CREATED_DATE,
                OFISDBContract.Task.COLUMN_NAME_COMPLETED,
                OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE
        };
        String orderBy = /*OFISDBContract.Task.COLUMN_NAME_COMPLETED + " ASC, " +*/ OFISDBContract.Task.COLUMN_NAME_ID + " DESC";
        return db.query(OFISDBContract.Task.TABLE_NAME, columns, null, null, null, null, orderBy);
    }

    int getActiveTaskCount() {
        String[] columns = new String[]{"id"};
        String whereClause = OFISDBContract.Task.COLUMN_NAME_COMPLETED + " = ?";
        String[] whereArgs = new String[]{"0"};

        // Changes - For SQLite Android Database Cursor window allocation of 2048 kb failed issue
        Cursor activeTasks = null;
        int activeTaskCount = 0;
        try {
            activeTasks = db.query(OFISDBContract.Task.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            activeTaskCount = activeTasks.getCount();
        } finally {
            if(activeTasks != null)
                activeTasks.close();
        }

        return activeTaskCount;
    }

    int updateTaskStatus(boolean completed, long taskId) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED, completed);

        if (completed) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, timestamp.toString());
        } else {
            values.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, "");
        }

        String whereClause = OFISDBContract.Task.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(taskId)};

        return db.update(OFISDBContract.Task.TABLE_NAME, values, whereClause, whereArgs);
    }

    Cursor getCompletedUnSyncedTasks() {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Task.COLUMN_NAME_SYNCED + " = ? AND "
                + OFISDBContract.Task.COLUMN_NAME_COMPLETED + " = ? OR "
                + OFISDBContract.Task.COLUMN_NAME_SYNCED + " = ? AND "
                + OFISDBContract.Task.COLUMN_NAME_COMPLETED + " = ?";
        String[] whereArgs = new String[]{
                "0",
                "1",
                "1",
                "0"
        };

        return db.query(OFISDBContract.Task.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getNewlyDeliveredTasks() {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Task.COLUMN_NAME_DELIVERY_CONFIRMED + " = ?";
        String[] whereArgs = new String[]{"0"};

        return db.query(OFISDBContract.Task.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    void markTasksAsSyncedOrDelivered(JSONArray taskIds, String syncedOrDelivered) {
        try {
            ContentValues values = new ContentValues();
            values.put(syncedOrDelivered, 1);

            if (taskIds.length() > 0) {
                String[] whereArgs = new String[taskIds.length()];
                for (int i = 0; i < taskIds.length(); i++) {
                    whereArgs[i] = taskIds.getString(i);
                }
                String placeholders = Utilities.makePlaceholders(taskIds.length());
                String whereClause = OFISDBContract.Task.COLUMN_NAME_ID + " IN (" + placeholders + ")";
                db.update(OFISDBContract.Task.TABLE_NAME, values, whereClause, whereArgs);

                if (syncedOrDelivered.equalsIgnoreCase(OFISDBContract.Task.COLUMN_NAME_SYNCED)) {
                    values.put(syncedOrDelivered, 0);
                    whereClause += " AND " + OFISDBContract.Task.COLUMN_NAME_COMPLETED + " = 0";
                    db.update(OFISDBContract.Task.TABLE_NAME, values, whereClause, whereArgs);
                }
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error marking task as synced or delivered", e));
            e.printStackTrace();
        }
    }

    long getBuyingAgentCount() {
        return DatabaseUtils.queryNumEntries(db, OFISDBContract.BuyingAgent.TABLE_NAME);
    }

    Cursor getBuyingAgentById(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.BuyingAgent.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.BuyingAgent.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getBuyingAgents() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.BuyingAgent.TABLE_NAME, columns, null, null, null, null, null);
    }

    Cursor getTransactionCurrencies(long buyingAgentId, long transacteeId, String transacteeType) {

        String currencyTable = OFISDBContract.Currency.TABLE_NAME;
        String sql;

        if (transacteeType.equalsIgnoreCase("agent")) {

            String buyingAgentCurrencyTable = OFISDBContract.BuyingAgentCurrency.TABLE_NAME;

            sql = "SELECT " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_CODE + ", " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID
                    + " AS _id, " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_SYMBOL
                    + " FROM " + currencyTable + " JOIN " + buyingAgentCurrencyTable + " ON " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID
                    + " = " + buyingAgentCurrencyTable + "." + OFISDBContract.BuyingAgentCurrency.COLUMN_NAME_CURRENCY_ID
                    + " WHERE " + buyingAgentCurrencyTable + "." + OFISDBContract.BuyingAgentCurrency.COLUMN_NAME_CURRENCY_ID + " IN ("
                    + "SELECT " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID + " FROM " + currencyTable
                    + " JOIN " + buyingAgentCurrencyTable + " ON " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID + " = "
                    + buyingAgentCurrencyTable + "." + OFISDBContract.BuyingAgentCurrency.COLUMN_NAME_CURRENCY_ID + " WHERE " + buyingAgentCurrencyTable
                    + "." + OFISDBContract.BuyingAgentCurrency.COLUMN_NAME_BUYINGAGENT_ID + " = " + transacteeId
                    + ") AND " + buyingAgentCurrencyTable + "." + OFISDBContract.BuyingAgentCurrency.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingAgentId
                    + " ORDER BY " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_CODE + " ASC";
        } else {
            String countryCurrencyTable = OFISDBContract.CountryCurrency.TABLE_NAME;
            String regionTable = OFISDBContract.RegionsEntry.TABLE_NAME;
            String districtTable = OFISDBContract.DistrictsEntry.TABLE_NAME;
            String placeTable = OFISDBContract.PlacesEntry.TABLE_NAME;
            String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;

            sql = "SELECT " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_CODE + ", " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID
                    + " AS _id, " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_SYMBOL
                    + " FROM " + currencyTable + " JOIN " + countryCurrencyTable + " ON " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID
                    + " = " + countryCurrencyTable + "." + OFISDBContract.CountryCurrency.COLUMN_CURRENCY_ID + " JOIN " + regionTable
                    + " ON " + countryCurrencyTable + "." + OFISDBContract.CountryCurrency.COLUMN_COUNTRY_ID + " = " + regionTable + "."
                    + OFISDBContract.RegionsEntry.COLUMN_NAME_COUNTRY_ID + " JOIN " + districtTable + " ON " + regionTable + "."
                    + OFISDBContract.RegionsEntry.COLUMN_NAME_ID + " = " + districtTable + "." + OFISDBContract.DistrictsEntry.COLUMN_NAME_REGION_ID
                    + " JOIN " + placeTable + " ON " + districtTable + "." + OFISDBContract.DistrictsEntry.COLUMN_NAME_ID + " = "
                    + placeTable + "." + OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID + " JOIN " + farmerTable + " ON "
                    + placeTable + "." + OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID
                    + " WHERE " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = " + transacteeId
                    + " ORDER BY " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_CODE + " ASC";
        }

        return db.rawQuery(sql, null);
    }

    Cursor getTransactionBalance(long buyingAgentId, long transacteeId, String transacteeType, long currencyId) {
        String balanceTable = OFISDBContract.TransactionBalance.TABLE_NAME;
        String currencyTable = OFISDBContract.Currency.TABLE_NAME;

        String sql = "SELECT " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_ID + ", "
                + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_SYMBOL + ", "
                + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE + ", " + balanceTable + "."
                + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID + ", " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC
                + " FROM " + balanceTable + " JOIN " + currencyTable + " ON " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_CURRENCY_ID
                + " = " + currencyTable + "." + OFISDBContract.Currency.COLUMN_NAME_ID;

        if (transacteeType.equalsIgnoreCase("agent")) {
            sql += " WHERE ((" + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingAgentId
                    + " AND " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID + " = " + transacteeId + ")"
                    + " OR (" + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT_ID + " = " + transacteeId
                    + " AND " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID + " = " + buyingAgentId + "))"
                    + " AND " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_CURRENCY_ID + " = " + currencyId;
        } else {
            sql += " WHERE " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingAgentId
                    + " AND " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_FARMER_ID + " = " + transacteeId
                    + " AND " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_CURRENCY_ID + " = " + currencyId;
        }

        return db.rawQuery(sql, null);
    }

    long updateTransactionBalance(long transactionBalanceId, String amountLoaned, long buyingAgentId, long transacteeId, String transacteeType, long currencyId, boolean is_receiving_direction, boolean is_right_hand_ba, boolean is_server_update, boolean is_rollback) {
        String balanceTable = OFISDBContract.TransactionBalance.TABLE_NAME;
        Double changeValue = Double.parseDouble(amountLoaned);

        if (is_receiving_direction) {
            changeValue *= -1;
        }

        if (is_right_hand_ba) {
            changeValue *= -1;
        }

        if (is_rollback) {
            changeValue *= -1;
        }

        if (transactionBalanceId != 0) {
            String[] whereArgs = new String[]{String.valueOf(transactionBalanceId)};
            String sqlValue = "(" + OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE + " + " + changeValue + "), "
                    + OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC + " = 1";

            if (is_server_update) {
                sqlValue = amountLoaned + ", " + OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC + " = 0";
            }

            String sql = "UPDATE " + balanceTable + " SET " + OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE + " = "
                    + sqlValue
                    + " WHERE " + balanceTable + "." + OFISDBContract.TransactionBalance.COLUMN_NAME_ID + " = ?";

            if (is_server_update) {
                sql += " AND " + OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC + " = 0";
            }

            db.execSQL(sql, whereArgs);
        } else {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT_ID, buyingAgentId);
            if (transacteeType.equalsIgnoreCase("agent")) {
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID, transacteeId);
            } else {
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_FARMER_ID, transacteeId);
            }
            values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_CURRENCY_ID, currencyId);
            if (is_server_update) {
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE, amountLoaned);
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC, 0);
            } else {
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE, String.valueOf(changeValue));
                values.put(OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC, 1);
            }
            transactionBalanceId = db.insert(balanceTable, null, values);
        }
        return transactionBalanceId;
    }

    Cursor getTransacteeByIdAndType(long transacteeId, String transacteeType) {
        String tableName = (transacteeType.equalsIgnoreCase("agent")) ? OFISDBContract.Transactee.TABLE_NAME : OFISDBContract.FarmerEntry.TABLE_NAME;
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Transactee.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transacteeId)};

        return db.query(tableName, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getTransactionItems() {
        String transactionItemsTable = OFISDBContract.TransactionItem.TABLE_NAME;
        String transacteesTable = OFISDBContract.Transactee.TABLE_NAME;
        String buyingAgentTable = OFISDBContract.BuyingAgent.TABLE_NAME;
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;

        String sql = "select " + transactionItemsTable + ".id as _id, "
                + transactionItemsTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_IN_QUEUE + ", "
                + buyingAgentTable + "." + OFISDBContract.BuyingAgent.COLUMN_NAME_NAME + ", "
                + transactionItemsTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID + ", "
                + transactionItemsTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE + ", "
                + "(CASE " + transactionItemsTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE + " "
                + "WHEN 'agent' THEN "
                + "(SELECT " + transacteesTable + "." + OFISDBContract.Transactee.COLUMN_NAME_NAME + " FROM " + transacteesTable
                + " WHERE " + transacteesTable + "." + OFISDBContract.Transactee.COLUMN_NAME_ID + " = " + transactionItemsTable + "."
                + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID + ") "
                + "ELSE "
                + "(SELECT " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " FROM " + farmerTable
                + " WHERE " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = " + transactionItemsTable + "."
                + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID + ") "
                + "END) AS transactee_name"
                + " FROM " + transactionItemsTable + " INNER JOIN " + buyingAgentTable
                + " ON " + transactionItemsTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID
                + " = " + buyingAgentTable + "." + OFISDBContract.BuyingAgent.COLUMN_NAME_ID;

        return db.rawQuery(sql, null);
    }

    long insertTransactionItem(ContentValues values) {
        long newRowId;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_CREATED_AT, timestamp.toString());
        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPDATED_AT, timestamp.toString());
        newRowId = db.insert(OFISDBContract.TransactionItem.TABLE_NAME, null, values);

        return newRowId;
    }

    int updateTransactionItem(ContentValues values, long transactionItemId) {
        String whereClause = OFISDBContract.TransactionItem.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionItemId)};

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPDATED_AT, timestamp.toString());

        return db.update(OFISDBContract.TransactionItem.TABLE_NAME, values, whereClause, whereArgs);
    }

    void deleteMobileMoneyTransactionItem(long transactionItemId) {
        String whereClause = OFISDBContract.TransactionItem.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionItemId)};
        db.delete(OFISDBContract.TransactionItem.TABLE_NAME, whereClause, whereArgs);
    }

    int deleteTransactionItem(long transactionItemId, boolean from_upload) {
        String queueWhereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID + " = ?";
        String[] queueWhereArgs = new String[]{String.valueOf(transactionItemId)};
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, queueWhereClause, queueWhereArgs);

        String transactionItemTable = OFISDBContract.TransactionItem.TABLE_NAME;
        String directionTable = OFISDBContract.Direction.TABLE_NAME;
        String whereClause = OFISDBContract.TransactionItem.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionItemId)};

        // get transaction to reverse
        String sql = "SELECT " + transactionItemTable + ".*, " + directionTable + "." + OFISDBContract.Direction.COLUMN_NAME_IS_RECEIVING_DIRECTION
                + " FROM " + transactionItemTable
                + " JOIN " + directionTable + " ON " + transactionItemTable + "." + OFISDBContract.TransactionItem.COLUMN_NAME_DIRECTION_ID
                + " = " + directionTable + "." + OFISDBContract.Direction.COLUMN_NAME_ID + " WHERE " + transactionItemTable + "."
                + whereClause;

        Cursor transactionItem = db.rawQuery(sql, whereArgs);
        if (transactionItem.moveToFirst()) {
            String amountLoaned = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED));
            boolean is_receiving_direction = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.Direction.COLUMN_NAME_IS_RECEIVING_DIRECTION)).equalsIgnoreCase("1");
            boolean is_right_hand_ba = false;
            long buyingAgentId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID));
            long transacteeId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID));
            String transacteeType = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE));
            long currencyId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_CURRENCY_ID));
            long transactionBalanceId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_BALANCE_ID));

            if (amountLoaned.length() > 0 && transactionBalanceId > 0) {
                String[] tbColumns = new String[]{"*"};
                String tbWhereClause = OFISDBContract.TransactionBalance.COLUMN_NAME_ID + " = ?";
                String[] tbWhereArgs = new String[]{String.valueOf(transactionBalanceId)};

                Cursor transactionBalance = db.query(OFISDBContract.TransactionBalance.TABLE_NAME, tbColumns, tbWhereClause, tbWhereArgs, null, null, null);

                if (transactionBalance.moveToFirst()) {
                    long rightHandId = transactionBalance.getLong(transactionBalance.getColumnIndex(OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID));
                    if (buyingAgentId == rightHandId) {
                        is_right_hand_ba = true;
                    }
                    if (!from_upload) {
                        updateTransactionBalance(transactionBalanceId, amountLoaned, buyingAgentId, transacteeId, transacteeType, currencyId, is_receiving_direction, is_right_hand_ba, false, true);
                    }

                    String otherItemsWhereClause = OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_BALANCE_ID + " = ? AND "
                            + OFISDBContract.TransactionItem.COLUMN_NAME_IN_QUEUE + " = ? AND "
                            + OFISDBContract.TransactionItem.COLUMN_NAME_ID + " != ?";
                    String[] otherItemsWhereArgs = new String[]{
                            String.valueOf(transactionBalanceId),
                            "1",
                            String.valueOf(transactionItemId)
                    };
                    long otherItems = DatabaseUtils.queryNumEntries(db, OFISDBContract.TransactionItem.TABLE_NAME, otherItemsWhereClause, otherItemsWhereArgs);

                    if (otherItems == 0) {
                        ContentValues tbValues = new ContentValues();
                        tbValues.put(OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC, "0");
                        db.update(OFISDBContract.TransactionBalance.TABLE_NAME, tbValues, tbWhereClause, tbWhereArgs);
                    }
                }
                transactionBalance.close();
            }
        }
        transactionItem.close();

        return db.delete(OFISDBContract.TransactionItem.TABLE_NAME, whereClause, whereArgs);
    }

    Cursor getTransactionItemById(long transactionItemId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.TransactionItem.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionItemId)};

        return db.query(OFISDBContract.TransactionItem.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getBuyingRule(long buyingAgentId, long transacteeId, String transacteeType, long selectedItemTypeGroup, long selectedItemType, long selectedDirection) {
        String buyingRuleTable = OFISDBContract.BuyingRule.TABLE_NAME;
        String buyingAgentBuyingRuleTable = OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME;
        String extraJoin = "";
        String extraWhere = "";

        if (transacteeType.equalsIgnoreCase("agent")) {
            extraJoin = " JOIN " + buyingAgentBuyingRuleTable + " ON " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_ID
                    + " = " + buyingAgentBuyingRuleTable + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGRULE_ID;
            extraWhere = " AND " + buyingAgentBuyingRuleTable + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGAGENT_ID
                    + " = " + transacteeId;
        }

        String sql = "SELECT * FROM " + buyingRuleTable + extraJoin + " WHERE " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMGROUP_ID
                + " = " + selectedItemTypeGroup + " AND " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMTYPE_ID
                + " = " + selectedItemType + " AND " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_DIRECTION_ID + " = "
                + selectedDirection + " AND " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingAgentId
                + " AND " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTEE_TYPE + " = '" + transacteeType + "'" + extraWhere
                + " LIMIT 1";

        return db.rawQuery(sql, null);
    }

    Cursor getItemTypeGroups(String textColumnName, long buyingagent_id, long transactee_id, String transactee_type) {
        // based on available buying rules
        String buyingrulesTable = OFISDBContract.BuyingRule.TABLE_NAME;
        String itemTypeGroupTable = OFISDBContract.TransactionItemGroup.TABLE_NAME;
        String extraJoin = "";
        String extraWhere = "";

        if (transactee_type.equalsIgnoreCase("agent")) {
            extraJoin = " JOIN " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_ID
                    + " = " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGRULE_ID;
            extraWhere = " AND " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGAGENT_ID
                    + " = " + transactee_id;
        }

        if (textColumnName == null) {
            textColumnName = "name";
        }

        String sql = "SELECT " + itemTypeGroupTable + "." + textColumnName + ", " + itemTypeGroupTable + "." + OFISDBContract.TransactionItemGroup.COLUMN_NAME_ID + " AS _id, "
                + itemTypeGroupTable + "." + OFISDBContract.TransactionItemGroup.COLUMN_NAME_DISPLAY_CONFIGURATION
                + " FROM " + buyingrulesTable + extraJoin
                + " JOIN " + itemTypeGroupTable + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMGROUP_ID
                + " = " + itemTypeGroupTable + "." + OFISDBContract.TransactionItemGroup.COLUMN_NAME_ID
                + " WHERE " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingagent_id
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTEE_TYPE + " = '" + transactee_type + "'"
                + extraWhere + " GROUP BY _id ORDER BY " + textColumnName + " ASC";

        return db.rawQuery(sql, null);
    }

    Cursor getItemTypesByGroupId(String textColumnName, long buyingagent_id, long transactee_id, String transactee_type, long groupId) {
        String buyingrulesTable = OFISDBContract.BuyingRule.TABLE_NAME;
        String itemTypeTable = OFISDBContract.TransactionItemType.TABLE_NAME;
        String extraJoin = "";
        String extraWhere = "";

        if (transactee_type.equalsIgnoreCase("agent")) {
            extraJoin = " JOIN " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_ID
                    + " = " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGRULE_ID;
            extraWhere = " AND " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGAGENT_ID
                    + " = " + transactee_id;
        }

        if (textColumnName == null) {
            textColumnName = "name";
        }

        String sql = "SELECT " + itemTypeTable + "." + textColumnName + ", " + itemTypeTable + "." + OFISDBContract.TransactionItemType.COLUMN_NAME_ID + " AS _id, "
                + itemTypeTable + "." + OFISDBContract.TransactionItemType.COLUMN_NAME_DISPLAY_CONFIGURATION
                + " FROM " + buyingrulesTable + extraJoin
                + " JOIN " + itemTypeTable + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMTYPE_ID
                + " = " + itemTypeTable + "." + OFISDBContract.TransactionItemType.COLUMN_NAME_ID
                + " WHERE " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingagent_id
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTEE_TYPE + " = '" + transactee_type + "'"
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMGROUP_ID + " = " + groupId
                + extraWhere + " GROUP BY _id ORDER BY " + textColumnName + " ASC";

        return db.rawQuery(sql, null);
    }

    Cursor getDirectionsByItemGroupAndType(String textColumnName, long buyingagent_id, long transactee_id, String transactee_type, long groupId, long typeId) {
        String buyingrulesTable = OFISDBContract.BuyingRule.TABLE_NAME;
        String directionTable = OFISDBContract.Direction.TABLE_NAME;
        String extraJoin = "";
        String extraWhere = "";

        if (transactee_type.equalsIgnoreCase("agent")) {
            extraJoin = " JOIN " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_ID
                    + " = " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGRULE_ID;
            extraWhere = " AND " + OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGAGENT_ID
                    + " = " + transactee_id;
        }

        if (textColumnName == null) {
            textColumnName = "name";
        }

        String sql = "SELECT " + directionTable + "." + textColumnName + ", " + directionTable + "." + OFISDBContract.Direction.COLUMN_NAME_ID + " AS _id, "
                + directionTable + "." + OFISDBContract.Direction.COLUMN_NAME_IS_GIVING_DIRECTION + ", " + directionTable + "."
                + OFISDBContract.Direction.COLUMN_NAME_IS_RECEIVING_DIRECTION + " FROM " + buyingrulesTable + extraJoin
                + " JOIN " + directionTable + " ON " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_DIRECTION_ID
                + " = " + directionTable + "." + OFISDBContract.Direction.COLUMN_NAME_ID
                + " WHERE " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingagent_id
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTEE_TYPE + " = '" + transactee_type + "'"
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMGROUP_ID + " = " + groupId
                + " AND " + buyingrulesTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTIONITEMTYPE_ID + " = " + typeId
                + extraWhere + " GROUP BY _id ORDER BY " + textColumnName + " ASC";

        return db.rawQuery(sql, null);
    }

    int getProductIdByTransactionItemTypeId(long id) {
        Cursor products = getSimpleSpinnerData(OFISDBContract.ProductTransactionItemType.TABLE_NAME, true, OFISDBContract.ProductTransactionItemType.COLUMN_NAME_TRANSACTIONITEMTYPE_ID, (int) id, false, null, OFISDBContract.ProductTransactionItemType.COLUMN_NAME_PRODUCT_ID);

        if (products.moveToFirst()) {
            return products.getInt(products.getColumnIndex(OFISDBContract.ProductTransactionItemType.COLUMN_NAME_PRODUCT_ID));
        } else {
            return 0;
        }
    }

    Cursor getUnitsByTransactionitemtypeId(long id) {
        String unitsTable = OFISDBContract.Unit.TABLE_NAME;
        String transactionItemPivot = OFISDBContract.TransactionItemTypeUnit.TABLE_NAME;

        String sql = "SELECT " + unitsTable + "." + OFISDBContract.Unit.COLUMN_NAME_NAME + ", " + unitsTable + "." + OFISDBContract.Unit.COLUMN_NAME_ID + " AS _id"
                + " FROM " + unitsTable + " INNER JOIN " + transactionItemPivot + " ON " + unitsTable + "." + OFISDBContract.Unit.COLUMN_NAME_ID
                + " = " + transactionItemPivot + "." + OFISDBContract.TransactionItemTypeUnit.COLUMN_NAME_UNIT_ID
                + " WHERE " + transactionItemPivot + "." + OFISDBContract.TransactionItemTypeUnit.COLUMN_NAME_TRANSACTIONITEMTYPE_ID + " = " + id;

        return db.rawQuery(sql, null);
    }

    Cursor getPossiblePrices(int selectedDirection, int selectedProduct, int selectedCurrency) {
        // get possible currencies
        Cursor availableCurrencies = db.query(OFISDBContract.Currency.TABLE_NAME, new String[]{"id"}, null, null, null, null, null);

        String productPriceTable = OFISDBContract.ProductPrice.TABLE_NAME;

        String sql = "SELECT * FROM " + productPriceTable + " WHERE "
                + OFISDBContract.ProductPrice.COLUMN_NAME_PRODUCT_ID + " = " + selectedProduct + " AND "
                + OFISDBContract.ProductPrice.COLUMN_NAME_DIRECTION_ID + " = " + selectedDirection;

        if (selectedCurrency > 0) {
            sql += " AND " + OFISDBContract.ProductPrice.COLUMN_NAME_CURRENCY_ID + " = " + selectedCurrency;

            Cursor currencyQuery = db.rawQuery(sql, null);

            if (currencyQuery.getCount() > 0) {
                return currencyQuery;
            }
        }

        sql += " AND " + OFISDBContract.ProductPrice.COLUMN_NAME_CURRENCY_ID + " IN (";
        String whereInItems = "";
        int size = availableCurrencies.getCount();
        for (int i = 0; i < size; i++) {
            availableCurrencies.moveToNext();
            String addComma = (i == 0) ? "" : ",";
            whereInItems += addComma + availableCurrencies.getString(availableCurrencies.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_ID));
        }
        sql += whereInItems + ")";
        availableCurrencies.close();
        return db.rawQuery(sql, null);
    }

    void recordMmTransactionTimeout(long transactionItemId) {
        String sql = "UPDATE " + OFISDBContract.TransactionItem.TABLE_NAME + " SET " + OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS
                + " = (" + OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS + " + 1) WHERE " + OFISDBContract.TransactionItem.COLUMN_NAME_ID
                + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(transactionItemId)
        };

        db.rawQuery(sql, whereArgs);
    }

    // training
    long insertTrainingRecord(ContentValues values) {
        long newRowId;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        values.put(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME, timestamp.toString());
        newRowId = db.insert(OFISDBContract.TrainingRecord.TABLE_NAME, null, values);

        return newRowId;
    }

    int updateTrainingRecord(ContentValues values, long trainingRecordId) {
        String whereClause = OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};

        return db.update(OFISDBContract.TrainingRecord.TABLE_NAME, values, whereClause, whereArgs);
    }

    int deleteTrainingRecord(long trainingRecordId) {
        String attendeeWhereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] attendeeWhereArgs = new String[]{String.valueOf(trainingRecordId)};
        db.delete(OFISDBContract.TrainingAttendee.TABLE_NAME, attendeeWhereClause, attendeeWhereArgs);

        String queueWhereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] queueWhereArgs = new String[]{String.valueOf(trainingRecordId)};
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, queueWhereClause, queueWhereArgs);

        String whereClause = OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};

        return db.delete(OFISDBContract.TrainingRecord.TABLE_NAME, whereClause, whereArgs);
    }

    Cursor getTrainingRecords() {
        String trainingRecordsTable = OFISDBContract.TrainingRecord.TABLE_NAME;
        String coursesTable = OFISDBContract.Course.TABLE_NAME;
        String trainingModuleTable = OFISDBContract.TrainingModule.TABLE_NAME;
        String trainingAttendeesTable = OFISDBContract.TrainingAttendee.TABLE_NAME;

        String columnSuffix = Utilities.getLocaleSuffix(res);
        if (columnSuffix.equalsIgnoreCase("_en")) {
            columnSuffix = "";
        }

        String sql = "SELECT " + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " AS _id, "
                + "DATE(" + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME + ") AS course_date, "
                + coursesTable + "." + OFISDBContract.Course.COLUMN_NAME_NAME + columnSuffix + " AS course_name, "
                + trainingModuleTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_NAME + columnSuffix + " AS module_name, "
                + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE + ", "
                + "COUNT(" + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_ID + ") AS attendee_count "
                + "FROM " + trainingRecordsTable + " LEFT JOIN " + trainingAttendeesTable + " ON " + trainingRecordsTable + "."
                + OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = " + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID
                + " INNER JOIN " + trainingModuleTable + " ON " + trainingRecordsTable + "."
                + OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID + " = " + trainingModuleTable + "."
                + OFISDBContract.TrainingModule.COLUMN_NAME_ID + " INNER JOIN " + coursesTable + " ON "
                + trainingModuleTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID + " = "
                + coursesTable + "." + OFISDBContract.Course.COLUMN_NAME_ID + " GROUP BY _id";


        return db.rawQuery(sql, null);
    }

    Cursor getTrainingRecordById(long trainingRecordId, String columnSuffix) {
        String trainingRecordsTable = OFISDBContract.TrainingRecord.TABLE_NAME;
        String trainingModulesTable = OFISDBContract.TrainingModule.TABLE_NAME;
        String trainingCoursesTable = OFISDBContract.Course.TABLE_NAME;
        String completionCriteriaTable = OFISDBContract.CompletionCriteria.TABLE_NAME;

        String sql = "SELECT " + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID + ", "
                + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME + ", "
                + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_COURSE_ID + ", "
                + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO + ", "
                + trainingModulesTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID + ", "
                + trainingCoursesTable + "." + OFISDBContract.Course.COLUMN_NAME_NAME + columnSuffix + " AS course_name, "
                + trainingModulesTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_NAME + columnSuffix + " AS module_name, "
                + completionCriteriaTable + "." + OFISDBContract.CompletionCriteria.COLUMN_NAME_SCORING_COLUMNS
                + " FROM " + trainingRecordsTable + " INNER JOIN " + trainingModulesTable + " ON "
                + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID + " = "
                + trainingModulesTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_ID + " INNER JOIN "
                + trainingCoursesTable + " ON " + trainingModulesTable + "." + OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID
                + " = " + trainingCoursesTable + "." + OFISDBContract.Course.COLUMN_NAME_ID + " INNER JOIN "
                + completionCriteriaTable + " ON " + trainingModulesTable + "." + OFISDBContract.TrainingModule.COLUMN_COMPLETIONCRITERION_ID
                + " = " + completionCriteriaTable + "." + OFISDBContract.CompletionCriteria.COLUMN_NAME_ID
                + " WHERE " + trainingRecordsTable + "." + OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = " + trainingRecordId;

        return db.rawQuery(sql, null);
    }

    Cursor getTrainingAttendeesByTrainingRecordId(long trainingRecordId) {
        String trainingAttendeesTable = OFISDBContract.TrainingAttendee.TABLE_NAME;
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;

        String sql = "SELECT " + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_ID + " AS _id, "
                + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE + ", "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE
                + " FROM " + trainingAttendeesTable + " INNER JOIN " + farmerTable + " ON "
                + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID + " = "
                + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID
                + " WHERE " + trainingAttendeesTable + "." + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID
                + " = " + trainingRecordId + " ORDER BY " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " ASC";

        return db.rawQuery(sql, null);
    }

    Cursor getTrainingAttendeeIdsByTrainingRecordId(long trainingRecordId) {
        String[] columns = new String[]{OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID};
        String whereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};
        return db.query(OFISDBContract.TrainingAttendee.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    int updateAttendeeScore(ContentValues values, String attendeeId) {
        String whereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{attendeeId};

        return db.update(OFISDBContract.TrainingAttendee.TABLE_NAME, values, whereClause, whereArgs);
    }

    int updateTrainingModuleAttendees(ArrayList<Long> selectedFarmers, long trainingRecordId) {
        // delete any records for farmers not in the list
        String attendeesTable = OFISDBContract.TrainingAttendee.TABLE_NAME;
        String deleteSql = "DELETE FROM " + attendeesTable + " WHERE " + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID
                + " = " + trainingRecordId + " AND " + OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID + " NOT IN ("
                + selectedFarmers.toString().replaceAll("\\[|\\]", "") + ")";
        db.execSQL(deleteSql);

        // now get all remaining attendees
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};
        Cursor attendees = db.query(attendeesTable, columns, whereClause, whereArgs, null, null, null);

        // remove any farmers already recorded from the array list
        while (attendees.moveToNext()) {
            if (selectedFarmers.contains(attendees.getLong(attendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID)))) {
                selectedFarmers.remove(attendees.getLong(attendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID)));
            }
        }

        // insert any remaining farmers in the array list
        int updatedCount = 0;
        if (selectedFarmers.size() > 0) {
            ContentValues newValues = new ContentValues();
            newValues.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID, trainingRecordId);

            for (Long farmerId : selectedFarmers) {
                newValues.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID, farmerId);
                db.insert(attendeesTable, null, newValues);
                updatedCount++;
            }

        }
        attendees.close();
        return updatedCount;
    }

    int deleteTrainingAttendeeById(int attendeeId) {
        String whereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(attendeeId)};
        return db.delete(OFISDBContract.TrainingAttendee.TABLE_NAME, whereClause, whereArgs);
    }

    void wipeAttendeeScores(long trainingRecordId) {
        ContentValues values = new ContentValues();
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED);
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE);
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE);
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE);
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE);
        values.putNull(OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE);

        String whereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};

        db.update(OFISDBContract.TrainingAttendee.TABLE_NAME, values, whereClause, whereArgs);
    }

    // Phone Number tasks
    Cursor getFarmerPhoneNumbers(long farmerId, boolean primaryOnly) {
        String numbersTable = OFISDBContract.PhoneNumber.TABLE_NAME;
        String countriesTable = OFISDBContract.CountriesEntry.TABLE_NAME;

        String sql = "SELECT " + numbersTable + ".*, " + countriesTable + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE + " "
                + "FROM " + numbersTable + " LEFT JOIN " + countriesTable + " ON " + numbersTable + "." + OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID
                + " = " + countriesTable + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " WHERE " + numbersTable + "." + OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID
                + " = ? ";

        if (primaryOnly) {
            sql += " AND " + numbersTable + "." + OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY + " = 1 LIMIT 1";
        }

        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        return db.rawQuery(sql, whereArgs);
    }

    void addFarmerPhoneNumbers(long farmerId, JSONArray storeableNumbers) {
        int arrayLength = storeableNumbers.length();

        try {
            for (int i = 0; i < arrayLength; i++) {
                JSONObject storeableNumber = storeableNumbers.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID, farmerId);
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID, storeableNumber.getLong("country_id"));
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY, storeableNumber.getBoolean("primary_number"));
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER, storeableNumber.getString("phonenumber"));
                db.insert(OFISDBContract.PhoneNumber.TABLE_NAME, null, values);
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error setting phone number storage values", e));
            e.printStackTrace();
        }

    }

    public boolean addFarmerNationalids(Context context,long farmerId, JSONArray storeableNationalids, String status, String farmername) {
        int arrayLength = storeableNationalids.length();
        boolean moderationstatus = false;
        try {
            for (int i = 0; i < arrayLength; i++) {
                JSONObject storeableNumber = storeableNationalids.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID, farmerId);
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, false);
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE, storeableNumber.getInt("nationalIdType"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER, storeableNumber.getString("nationalIdNumber"));
                if(storeableNumber.has("photo")) {
                    values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO, storeableNumber.getString("photo"));
                }
                if (isUnderModeration(storeableNumber.getString("nationalIdNumber"), String.valueOf(storeableNumber.getInt("nationalIdType")))) {
                    moderationstatus = true;
                }
                if(storeableNumber.getInt("nationalIdType") != 0) {
                    db.insert(OFISDBContract.FarmerNationalIDs.TABLE_NAME, null, values);
                }
                if(status.equalsIgnoreCase("2")){
                    updateNetworkQueueNIDEntry(context, farmerId, storeableNumber.getInt("nationalIdType"),farmername);
                }

            }

            updateModerationStatus(farmerId,moderationstatus);

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error setting phone number storage values", e));
            e.printStackTrace();
        }
        return moderationstatus;
    }


    void addLocalFarmerNationalids(ArrayList<NationalIdDTO> storeableNationalids) {
        int arrayLength = storeableNationalids.size();

        for (int i = 0; i < arrayLength; i++) {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_TYPE, storeableNationalids.get(i).getNationalidtype());
            values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_NUMBER, storeableNationalids.get(i).getNationalidNumber());
            values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_PHOTO, storeableNationalids.get(i).getPhoto());
            db.insert(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, values);

        }

    }

    void addnewLocalFarmerNationalids(NationalIdDTO storeableNationalids) {
//        delete value if exists
        String whereClause = OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(storeableNationalids.getNationalidtype())};
        db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
//        adding new values
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_TYPE, storeableNationalids.getNationalidtype());
        values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_NUMBER, storeableNationalids.getNationalidNumber());
        values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_PHOTO, storeableNationalids.getPhoto());
        db.insert(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, values);

    }
    void updateFarmerPhoneNumbers(long farmerId, JSONArray storeableNumbers) {
        String whereClause = OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        db.delete(OFISDBContract.PhoneNumber.TABLE_NAME, whereClause, whereArgs);
        addFarmerPhoneNumbers(farmerId, storeableNumbers);
    }

    public boolean updateFarmerNationalids(Context context,long farmerId, JSONArray storeableNationalids, String status, String farmername) {
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
        boolean modurationstatus = addFarmerNationalids(context,farmerId, storeableNationalids,status,farmername);
        return modurationstatus;
    }

    public Cursor getNIDPhotostatusByType(String type, long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? AND " +  OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{type, String.valueOf(farmerid)};

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    void updateLocalFarmerNationalids(ArrayList<NationalIdDTO> storeableNationalids) {
        db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, null);
        addLocalFarmerNationalids(storeableNationalids);
    }

    void deletelocalfarmervalues(int id) {
        String whereClause = OFISDBContract.LocalFarmerNationalIDs.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
    }

    void deletelocalfarmervalues() {
        db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, null);
    }

    Cursor getDiallingCodeByPlaceId(long placeId) {
        String placesTable = OFISDBContract.PlacesEntry.TABLE_NAME;
        String districtsTable = OFISDBContract.DistrictsEntry.TABLE_NAME;
        String regionsTable = OFISDBContract.RegionsEntry.TABLE_NAME;
        String countriesTable = OFISDBContract.CountriesEntry.TABLE_NAME;

        String sql = "SELECT " + countriesTable + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE
                + " FROM " + placesTable + " INNER JOIN " + districtsTable + " ON " + placesTable + "." + OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID
                + " = " + districtsTable + "." + OFISDBContract.DistrictsEntry.COLUMN_NAME_ID + " INNER JOIN " + regionsTable + " ON "
                + districtsTable + "." + OFISDBContract.DistrictsEntry.COLUMN_NAME_REGION_ID + " = " + regionsTable + "." + OFISDBContract.RegionsEntry.COLUMN_NAME_ID
                + " INNER JOIN " + countriesTable + " ON " + regionsTable + "." + OFISDBContract.RegionsEntry.COLUMN_NAME_COUNTRY_ID + " = " + countriesTable
                + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " WHERE " + placesTable + "." + OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(placeId)};

        return db.rawQuery(sql, whereArgs);
    }

    // UI tasks
    Cursor getNetworkQueue() {
        List<String> columnList = OFISDBContract.NetworkQueueEntry.COLUMN_LIST;
        String[] columns = new String[columnList.size()];
        for (int i = 0; i < columnList.size(); i++) {
            columns[i] = (columnList.get(i).equals("id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
        }
        String orderByColumn = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " DESC, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT + " ASC";
        return db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, null, null, null, null, orderByColumn);
    }

    Cursor getSpinnerData(String table, Boolean getByRelationship, String getByColumnName, Integer getByColumnValue, @SuppressWarnings("SameParameterValue") Boolean orderByColumn, String orderByColumnName) {
        if (OFISDBContract.ALL_TABLES.containsKey(table)) {
            List<String> columnList = OFISDBContract.ALL_TABLES.get(table);
            String[] tableColumns = new String[columnList.size()];
            if(OFISDBContract.countryLookUpValuesEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())){
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("country_id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
                }
            }
            else if(OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())){
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("productId")) ? columnList.get(i) + " AS _id" : columnList.get(i);
//                    tableColumns[i] = columnList.get(i);
                }
            }
            else {
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
                }
            }
            String querySelection = (getByRelationship) ? getByColumnName + " = ?" : null;
            String[] querySelectionValue = (getByRelationship) ? new String[]{String.valueOf(getByColumnValue)} : null;
            String orderByValue = (orderByColumn) ? orderByColumnName + " " + "ASC" : null;
            if(OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())){
                orderByValue = OFISDBContract.productSipEntry.COLUMN_NAME_PRODUCT_NAME + " " + "ASC";
            }else{
                orderByValue = (orderByColumn) ? orderByColumnName + " " + "ASC" : null;
            }
            return db.query(table, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
        }

        return null;
    }

    void bindDataDrivenSpinner(Spinner spinner, Cursor cursor, Context context, int placeholderTextResId, String textColumnName, boolean showAsWhite) {
        try {
            if (textColumnName == null) {
                textColumnName = "name";
            }
            String[] adapterCols = new String[]{textColumnName, "_id", "code", "dialling_code"};
            MatrixCursor mc = new MatrixCursor(adapterCols);
            mc.addRow(new String[]{res.getString(placeholderTextResId), "0", "", ""});
            if (spinner.getId() == R.id.spinner_place ) {
                mc.addRow(new String[]{res.getString(R.string.new_place_name_placeholder), "-1", "", ""});

            }
            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);

            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0)
            {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v=super.getDropDownView(position,convertView,parent);
                    v.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ((TextView)v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
                        }
                    });
                    return v;
                }
            };

            simpleCursorAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

            spinner.setAdapter(simpleCursorAdapter);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void bindDataDrivenSpinnerSIP(Spinner spinner, Cursor cursor, Context context, int placeholderTextResId, String textColumnName, boolean showAsWhite) {
        try {
            if (textColumnName == null) {
                textColumnName = "name";
            }
            String[] adapterCols = new String[]{textColumnName, "_id", "code", "dialling_code"};
            MatrixCursor mc = new MatrixCursor(adapterCols);
            mc.addRow(new String[]{res.getString(placeholderTextResId), "0", "", ""});
//            if (spinner.getId() == R.id.spinner_place ) {
////                mc.addRow(new String[]{res.getString(R.string.new_place_name_placeholder), "-1", "", ""});
////
////            }
            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);

            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0)
            {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v=super.getDropDownView(position,convertView,parent);
                    v.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ((TextView)v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
                        }
                    });
                    return v;
                }
            };

            simpleCursorAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

            spinner.setAdapter(simpleCursorAdapter);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    Cursor getSimpleSpinnerData(String table, Boolean getByRelationship, String getByColumnName, Integer getByColumnValue, @SuppressWarnings("SameParameterValue") Boolean orderByColumn, String orderByColumnName, String textColumnName) {
        if (OFISDBContract.ALL_TABLES.containsKey(table)) {
            List<String> columnList = OFISDBContract.ALL_TABLES.get(table);
            String[] tableColumns = new String[2];
            boolean hasIdColumn = false;
            for (int i = 0; i < columnList.size(); i++) {
                if (columnList.get(i).equals("id")) {
                    hasIdColumn = true;
                }
                if (columnList.get(i).equals(textColumnName)) {
                    tableColumns[0] = columnList.get(i);
                }
            }
            if (hasIdColumn) {
                tableColumns[1] = "id AS _id";
            } else {
                return null;
            }

            String querySelection = (getByRelationship) ? getByColumnName + " = ?" : null;
            String[] querySelectionValue = (getByRelationship) ? new String[]{String.valueOf(getByColumnValue)} : null;
            String orderByValue = (orderByColumn) ? orderByColumnName + " " + "ASC" : null;

            return db.query(table, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
        }

        return null;
    }

    void bindDirectionDataDrivenSpinner(Spinner spinner, Cursor cursor, Context context, String textColumnName) {
        if (textColumnName == null) {
            textColumnName = "name";
        }
        String[] adapterCols = new String[]{textColumnName, "_id", "is_giving_direction", "is_receiving_direction"};
        MatrixCursor mc = new MatrixCursor(adapterCols);
        mc.addRow(new String[]{res.getString(R.string.please_select), "0", "0", "0"});

        Cursor[] cursors = {mc, cursor};
        Cursor output = new MergeCursor(cursors);

        int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
        int spinnerItemLayout = R.layout.spinner_item;
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0);
        simpleCursorAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(simpleCursorAdapter);
    }

    void bindSimpleDataDrivenSpinner(Spinner spinner, Cursor cursor, Context context, String textColumnName) {
        if (textColumnName == null) {
            textColumnName = "name";
        }
        String[] adapterCols = new String[]{textColumnName, "_id", "display_configuration"};
        MatrixCursor mc = new MatrixCursor(adapterCols);
        mc.addRow(new String[]{res.getString(R.string.please_select), "0", "{}"});

        Cursor[] cursors = {mc, cursor};
        Cursor output = new MergeCursor(cursors);

        int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
        int spinnerItemLayout = R.layout.spinner_item;
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0);
        simpleCursorAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(simpleCursorAdapter);
    }

    void createBusinessHierarchySpinners(AppCompatActivity activity, final Context context, final EditText farmerIdPrefix, final int farmerGroupSelection, final int sectionSelection) {
        final Spinner farmerGroupSpinner = (Spinner) activity.findViewById(R.id.spinner_farmerGroup);
        final Spinner sectionSpinner = (Spinner) activity.findViewById(R.id.spinner_section);

        Cursor farmerGroups = getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        bindDataDrivenSpinner(farmerGroupSpinner, farmerGroups, context, R.string.spinner_farmergroup_default, null, false);

        farmerGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Cursor sections = getSpinnerData(OFISDBContract.SectionsEntry.TABLE_NAME, true, OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID, ((int) id), true, OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME);
                bindDataDrivenSpinner(sectionSpinner, sections, context, R.string.spinner_section_default, null, false);

                if (farmerIdPrefix != null && id > 0) {
                    Cursor selectedFarmerGroup = (Cursor) farmerGroupSpinner.getSelectedItem();
                    String code = selectedFarmerGroup.getString(selectedFarmerGroup.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_CODE));
                    code = String.format("%4s", code).replace(' ', '0');
                    String currentPrefix = farmerIdPrefix.getText().toString();
                    String newPrefix = currentPrefix.substring(0, 3) + code + currentPrefix.substring(7);
                    farmerIdPrefix.setText(newPrefix);
                }

                if (sectionSelection != 0) {
                    setSpinnerSelectionById(sectionSelection, sectionSpinner);
                    if (sectionSpinner.getCount() == 1) {
                        sectionSpinner.setEnabled(false);
                    } else {
                        sectionSpinner.setEnabled(true);
                    }
                } else {
                    if (sections.getCount() == 1) {
                        sectionSpinner.setSelection(1, false);
                        sectionSpinner.setEnabled(false);
                    } else {
                        sectionSpinner.setEnabled(true);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (farmerIdPrefix != null && id > 0) {
                    Cursor selectedSection = (Cursor) sectionSpinner.getSelectedItem();
                    String code = selectedSection.getString(selectedSection.getColumnIndex(OFISDBContract.SectionsEntry.COLUMN_NAME_CODE));
                    code = String.format("%4s", code).replace(' ', '0');
                    String currentPrefix = farmerIdPrefix.getText().toString();
                    String newPrefix = currentPrefix.substring(0, 8) + code + "-";
                    farmerIdPrefix.setText(newPrefix);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (farmerGroupSelection != 0) {
            setSpinnerSelectionById(farmerGroupSelection, farmerGroupSpinner);
            if (farmerGroups.getCount() == 1) {
                farmerGroupSpinner.setEnabled(false);
            }
        } else {
            if (farmerGroups.getCount() == 1) {
                farmerGroupSpinner.setSelection(1, false);
                farmerGroupSpinner.setEnabled(false);
            }
        }

    }

    void createGeographyHierarchySpinners(final AppCompatActivity activity, final Context context, final EditText farmerIdPrefix, int countrySelection, final int regionSelection, final int districtSelection, final int placeSelection, final LinearLayout phonenumbers, final boolean newPlace) {
        final Spinner countrySpinner = (Spinner) activity.findViewById(R.id.spinner_country);
        final Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_region);
        final Spinner districtSpinner = (Spinner) activity.findViewById(R.id.spinner_district);
        final Spinner placeSpinner = (Spinner) activity.findViewById(R.id.spinner_place);

        final EditText newplace =(EditText) activity.findViewById(R.id.newPlaceName);

        Cursor countries = getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        bindDataDrivenSpinner(countrySpinner, countries, context, R.string.spinner_country_default, null, false);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                Cursor regions = getSpinnerData(OFISDBContract.RegionsEntry.TABLE_NAME, true, OFISDBContract.RegionsEntry.COLUMN_NAME_COUNTRY_ID, ((int) id), true, OFISDBContract.RegionsEntry.COLUMN_NAME_REGION_NAME);
                bindDataDrivenSpinner(regionSpinner, regions, context, R.string.spinner_region_default, null, false);
                Cursor selectedCountry = (Cursor) countrySpinner.getSelectedItem();

                if (farmerIdPrefix != null && id > 0) {
                    String code = selectedCountry.getString(selectedCountry.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_CODE));
                    String currentPrefix = farmerIdPrefix.getText().toString();
                    String newPrefix = code + currentPrefix.substring(2);
                    farmerIdPrefix.setText(newPrefix);
                }

                if (phonenumbers != null && id > 0) {
                    for (int z = 0; z < phonenumbers.getChildCount(); z++) {
                        LinearLayout phonenumber = (LinearLayout) phonenumbers.getChildAt(z);
                        EditText diallingcode = (EditText) phonenumber.findViewById(R.id.dialling_code);
                        String dialling_code = selectedCountry.getString(selectedCountry.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE));
                        diallingcode.setText(dialling_code);
                    }
                }

                if (regionSelection != 0) {
                    setSpinnerSelectionById(regionSelection, regionSpinner);
                    if (regions.getCount() == 1) {
                        regionSpinner.setEnabled(false);
                    } else {
                        regionSpinner.setEnabled(true);
                    }
                } else {
                    if (regions.getCount() == 1) {
                        regionSpinner.setSelection(1, false);
                        regionSpinner.setEnabled(false);
                    } else {
                        regionSpinner.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                Cursor districts = getSpinnerData(OFISDBContract.DistrictsEntry.TABLE_NAME, true, OFISDBContract.DistrictsEntry.COLUMN_NAME_REGION_ID, ((int) id), true, OFISDBContract.DistrictsEntry.COLUMN_NAME_DISTRICT_NAME);
                bindDataDrivenSpinner(districtSpinner, districts, context, R.string.spinner_district_default, null, false);

                if (districtSelection != 0) {
                    setSpinnerSelectionById(districtSelection, districtSpinner);
                    if (districts.getCount() == 1) {
                        districtSpinner.setEnabled(false);
                    } else {
                        districtSpinner.setEnabled(true);
                    }
                } else {
                    if (districts.getCount() == 1) {
                        districtSpinner.setSelection(1, false);
                        districtSpinner.setEnabled(false);
                    } else {
                        districtSpinner.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (placeSpinner != null) {
            districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                    Cursor places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    if (!newPlace) {
                        bindDataDrivenSpinnerSIP(placeSpinner, places, context, R.string.spinner_place_default, null, false);
                    }else{
                        bindDataDrivenSpinner(placeSpinner, places, context, R.string.spinner_place_default, null, false);
                    }

                    if (placeSelection != 0) {
                        setSpinnerSelectionById(placeSelection, placeSpinner);
                    } else {
                        if (newPlace) {
                            if (places.getCount() == 1) {
                                try {
                                    placeSpinner.setSelection(2, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if (activity.findViewById(R.id.newPlaceName) != null) {
                final EditText newPlaceName = (EditText) activity.findViewById(R.id.newPlaceName);
                placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                        try {
                            if (id == -1) {
                                newPlaceName.setVisibility(View.VISIBLE);
                            } else {
                                newPlaceName.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        }

        if (countrySelection != 0) {
            setSpinnerSelectionById(countrySelection, countrySpinner);
            if (countries.getCount() == 1) {
                countrySpinner.setEnabled(false);
            }
        } else {
            if (countries.getCount() == 1) {
                countrySpinner.setSelection(1, false);
                countrySpinner.setEnabled(false);
            }
        }
    }

    void setSpinnerSelectionById(int selectedId, Spinner spinner) {
        int spinnerCount = spinner.getCount();
        for (int i = 0; i < spinnerCount; i++) {
            Cursor value = (Cursor) spinner.getItemAtPosition(i);
            int positionId = value.getInt(value.getColumnIndex("_id"));
            if (selectedId == positionId) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    void createProductSpinner(AppCompatActivity activity, final Context context, int productSelection) {
        final Spinner productSpinner = (Spinner) activity.findViewById(R.id.spinner_product);

        Cursor products = getSpinnerData(OFISDBContract.productSipEntry.TABLE_NAME, false, null, null, true, OFISDBContract.productSipEntry.COLUMN_NAME_PRODUCT_NAME);
        bindDataDrivenSpinner(productSpinner, products, context, R.string.spinner_product_default, null, false);

        if (productSelection != 0) {
            setSpinnerSelectionById(productSelection, productSpinner);
        }
    }

    Cursor getFindFarmerOrBuyingAgentList(long buyingAgentId, String query, long selectedFarmerGroup, long selectedSection) {
        String farmerRulesWhereClause = OFISDBContract.BuyingRule.COLUMN_NAME_TRANSACTEE_TYPE + " = ? AND " + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = ?";
        String[] farmerRulesWhereArgs = new String[]{"farmer", String.valueOf(buyingAgentId)};
        long farmerRules = DatabaseUtils.queryNumEntries(db, OFISDBContract.BuyingRule.TABLE_NAME, farmerRulesWhereClause, farmerRulesWhereArgs);

        Cursor farmerlist = null;
        if (farmerRules > 0) {
            String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;
            String farmerGroupsTable = OFISDBContract.FarmerGroupsEntry.TABLE_NAME;
            String sectionsTable = OFISDBContract.SectionsEntry.TABLE_NAME;

            String whereClause = "";

            if (query.length() > 0) {
                whereClause += " WHERE (" + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " LIKE '%" + query + "%' OR " +
                        farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " LIKE '%" + query + "')";
            }
            if (selectedFarmerGroup > 0) {
                if (whereClause.length() == 0) {
                    whereClause += " WHERE ";
                } else {
                    whereClause += " AND ";
                }
                whereClause += farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " = " + selectedFarmerGroup;
            }
            if (selectedSection > 0) {
                if (whereClause.length() == 0) {
                    whereClause += " WHERE ";
                } else {
                    whereClause += " AND ";
                }
                whereClause += sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " = " + selectedSection;
            }
            String farmerSql = "SELECT " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " AS _id, " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + ", " +
                    " 'farmer' AS transactee_type FROM " +
                    farmerTable + " INNER JOIN " + sectionsTable + " ON " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID +
                    " = " + sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " INNER JOIN " + farmerGroupsTable + " ON " +
                    sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID + " = " + farmerGroupsTable + "." +
                    OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID +
                    whereClause + " ORDER BY " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " ASC";

            farmerlist = db.rawQuery(farmerSql, null);

            if (selectedFarmerGroup > 0 || selectedSection > 0) {
                return farmerlist;
            }
        }

        String transacteeTable = OFISDBContract.Transactee.TABLE_NAME;
        String buyingAgentBuyingRuleTable = OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME;
        String buyingRuleTable = OFISDBContract.BuyingRule.TABLE_NAME;
        String transacteeWhereClause = " WHERE " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_BUYINGAGENT_ID + " = " + buyingAgentId;

        if (query.length() > 0) {
            transacteeWhereClause = " AND " + OFISDBContract.Transactee.COLUMN_NAME_NAME + " LIKE '%" + query + "%'";
        }

        String transacteeSql = "SELECT " + transacteeTable + "." + OFISDBContract.Transactee.COLUMN_NAME_ID + " AS _id, " +
                transacteeTable + "." + OFISDBContract.Transactee.COLUMN_NAME_NAME + ", " +
                " 'buyingagentphoto' AS " + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " +
                " '" + res.getString(R.string.buyingagent) + "' AS " + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " +
                " 0 AS " + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + ", " +
                " 'agent' AS transactee_type FROM " + transacteeTable + " JOIN " + buyingAgentBuyingRuleTable + " ON " + transacteeTable
                + "." + OFISDBContract.Transactee.COLUMN_NAME_ID + " = " + buyingAgentBuyingRuleTable + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGAGENT_ID
                + " JOIN " + buyingRuleTable + " ON " + buyingAgentBuyingRuleTable + "." + OFISDBContract.BuyingAgentBuyingRule.COLUMN_NAME_BUYINGRULE_ID
                + " = " + buyingRuleTable + "." + OFISDBContract.BuyingRule.COLUMN_NAME_ID + transacteeWhereClause + " GROUP BY _id" + " ORDER BY "
                + transacteeTable + "." + OFISDBContract.Transactee.COLUMN_NAME_NAME + " ASC";

        Cursor buyingAgentList = db.rawQuery(transacteeSql, null);

        if (farmerRules > 0) {
            Cursor[] cursors = {buyingAgentList, farmerlist};
            return new MergeCursor(cursors);
        } else {
            return buyingAgentList;
        }

    }

    Cursor getTrainingAttendeeList(String query, long selectedFarmerGroup, long selectedSection, long courseId) {
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;
        String farmerGroupsTable = OFISDBContract.FarmerGroupsEntry.TABLE_NAME;
        String sectionsTable = OFISDBContract.SectionsEntry.TABLE_NAME;
        String courseAssignmentTable = OFISDBContract.CourseAssignment.TABLE_NAME;
        String subSelect = "SELECT " + courseAssignmentTable + "." + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ASSIGNMENT_ID + " FROM " +
                courseAssignmentTable + " WHERE " + courseAssignmentTable + "." + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ASSIGNMENT_TYPE +
                " = '%1$s'" + " AND " + courseAssignmentTable + "." + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ID + " = " + courseId;

        String whereClause = " WHERE (" +
                " (" + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " IN (" + String.format(subSelect, "FarmerGroup") + "))" +
                " OR (" + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PROGRAMME_ID + " IN (" + String.format(subSelect, "Programme") + "))" +
                " OR (" + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PARTNER_ID + " IN (" + String.format(subSelect, "Partner") + "))" +
                " )";

        if (query.length() > 0) {
            whereClause += " AND " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " LIKE '%" + query + "%' OR " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " LIKE '%" + query + "'";
        }
        if (selectedFarmerGroup > 0) {
            whereClause += " AND " + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " = " + selectedFarmerGroup;
        }
        if (selectedSection > 0) {
            whereClause += " AND " + sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " = " + selectedSection;
        }
        String sql = "SELECT " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " AS _id, " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + " FROM " +
                farmerTable + " INNER JOIN " + sectionsTable + " ON " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID +
                " = " + sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " INNER JOIN " + farmerGroupsTable + " ON " +
                sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID + " = " + farmerGroupsTable + "." +
                OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + whereClause + " order by " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " ASC";

        // code hardcorded for get some farmers - Remove at last minute
//       sql="SELECT farmers.id AS _id, farmers.name, farmers.photo, farmers.olam_farmer_id, farmers.created_on_this_phone FROM farmers";// LIMIT 5";
        return db.rawQuery(sql, null);
    }

    Cursor getFindFarmerList(String query, long selectedFarmerGroup, long selectedSection) {
        // Query:
        // select farmers.id as _id, farmers.name, farmers.photo, farmers.olam_farmer_id, farmer_modules.id as farmer_module_id, farmer_modules.status_started
        // from farmers LEFT OUTER JOIN farmer_modules ON (farmers.id = farmer_modules.farmer_id AND farmer_modules.status_started = 1 AND farmer_modules.status_finished = 0)
        // order by farmer_modules.status_started DESC, farmers.name ASC
        String whereClause = "";
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;
        String farmerModulesTable = OFISDBContract.FarmerModulesEntry.TABLE_NAME;
        String farmerGroupsTable = OFISDBContract.FarmerGroupsEntry.TABLE_NAME;
        String sectionsTable = OFISDBContract.SectionsEntry.TABLE_NAME;
        if (query.length() > 0) {
            whereClause = "WHERE " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " LIKE '%" + query + "%' OR " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " LIKE '%" + query + "'";
        }
        if (selectedFarmerGroup > 0) {
            if (whereClause.length() == 0) {
                whereClause += "WHERE ";
            } else {
                whereClause += " AND ";
            }
            whereClause += farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " = " + selectedFarmerGroup;
        }
        if (selectedSection > 0) {
            if (whereClause.length() == 0) {
                whereClause += "WHERE ";
            } else {
                whereClause += " AND ";
            }
            whereClause += sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " = " + selectedSection;
        }
        String sql = "select distinct(farmers.id) as _id, " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + ", " +
                farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " as farmer_module_id, " +
                farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED + " from " +
                farmerTable + " INNER JOIN " + sectionsTable + " ON " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID +
                " = " + sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " INNER JOIN " + farmerGroupsTable + " ON " +
                sectionsTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID + " = " + farmerGroupsTable + "." +
                OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " LEFT OUTER JOIN " + farmerModulesTable + " ON (" + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID +
                " = " + farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " AND " +
                farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED + " = 1 AND " +
                farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED + " = 0) " +
                whereClause + " group by _id order by " + farmerModulesTable + "." + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED +
                " DESC, " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + " DESC, "
                + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " ASC";

        return db.rawQuery(sql, null);
    }

    Cursor getFarmerAndGroupByFarmerId(long id) {
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;
        String sectionTable = OFISDBContract.SectionsEntry.TABLE_NAME;
        String farmerGroupsTable = OFISDBContract.FarmerGroupsEntry.TABLE_NAME;

        String sql = "SELECT " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + ", " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", "
                + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " + farmerGroupsTable
                + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " as farmer_group_id, " + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME + " as farmer_group_name "
                + "FROM " + farmerTable + " INNER JOIN " + sectionTable + " ON " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID + " = " + sectionTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_ID
                + " INNER JOIN " + farmerGroupsTable + " ON " + sectionTable + "." + OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID + " = " + farmerGroupsTable + "." + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID
                + " WHERE " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = " + id;

        return db.rawQuery(sql, null);
    }

    public Cursor getFarmerById(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getNIDByName(String type) {

        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " = ? ";
        String language = Locale.getDefault().getLanguage();
        if(language.equalsIgnoreCase(Utilities.FR)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.ES)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.PT)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.ID)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.TR)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.LO)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.VI)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.TH)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH + " = ? ";
        }


        String[] columns = new String[]{"*"};

        String[] whereArgs = new String[]{type};

        return db.query(OFISDBContract.NationalIDEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getNIDById(int id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_ID + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.NationalIDEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getNIDPhotostatusByName(String type) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " = ? ";
        String language = Locale.getDefault().getLanguage();
        if(language.equalsIgnoreCase(Utilities.FR)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.ES)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.PT)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.ID)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.TR)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.LO)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.VI)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " = ? ";
        }else if(language.equalsIgnoreCase(Utilities.TH)){
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH + " = ? ";
        }
        String[] whereArgs = new String[]{type};

        return db.query(OFISDBContract.NationalIDEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getPhotostatusByType(String type) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ? ";
        String[] whereArgs = new String[]{type};

        return db.query(OFISDBContract.LocalNationalIDImages.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getModerationStatusbyNID(String nationalid, String type) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER + " = ? AND "+ OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? ";
        String[] whereArgs = new String[]{nationalid,type};

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getAllPhotos() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.LocalNationalIDImages.TABLE_NAME, columns, null, null, null, null, null);
    }

    public int getFarmId(int farmer){
        int FarmID = 0;
        try{
            Cursor farmCursor  = db.rawQuery("Select farm_id from farmers where id =" + farmer,null);
            if(farmCursor.getCount() > 0){
                farmCursor.moveToNext();
                FarmID = farmCursor.getInt(farmCursor.getColumnIndex("farm_id"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return FarmID;
    }
    Cursor getFlattenedFarmer(long id) {
        String farmerQuery = "SELECT farmers.*, districts.id as district_id, regions.id as region_id, countries.id as country_id, farmergroups.id as farmergroup_id, farmergroups.programme_id as programme_id, farmergroups.product_id as product_id, farmergroups.partner_id as partner_id " +
                "FROM farmers " +
                "INNER JOIN places on farmers.place_id = places.id " +
                "INNER JOIN districts on places.district_id = districts.id " +
                "INNER JOIN regions on districts.region_id = regions.id " +
                "INNER JOIN countries on regions.country_id = countries.id " +
                "INNER JOIN sections on farmers.section_id = sections.id " +
                "INNER JOIN farmergroups on sections.farmergroup_id = farmergroups.id " +
                "WHERE farmers.id = ?";
        String[] idToFind = new String[]{String.valueOf(id)};

        return db.rawQuery(farmerQuery, idToFind);
    }


    public Cursor getChildModulesByFarmer(long id, String child_id) {
        Cursor farmer = null;
        Cursor farmerModules=null;

        try {
            farmer = getFlattenedFarmer(id);
            Cursor output = null;
            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                String modulesQuery = "SELECT modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                        "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                        "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type,moduleassignments.surveyCode,moduleassignments.childLabour, moduleassignments.disabled_months,moduleassignments.startDate, " +
                        " farmer_child_modules.id as farmer_module_id, farmer_child_modules.status_started, farmer_child_modules.status_finished, farmer_child_modules.current_submodule_id, farmer_child_modules.recorded_lat, farmer_child_modules.recorded_lng,modules.moduleCode FROM moduleassignments" +
                        " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                        " LEFT OUTER JOIN farmer_child_modules ON modules.id = farmer_child_modules.module_id AND " +
                        "(farmer_child_modules.farmer_id = ? OR farmer_child_modules.olam_farmer_id = ?) AND " +
                        "(farmer_child_modules.child_id = ?)"+
                        " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_child_modules.current_submodule_id = submodules.id WHERE " +
                        "(moduleassignment_type = 'Country' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)";

                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        child_id,
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("region_id")),
                        farmer.getString(farmer.getColumnIndex("district_id")),
                        farmer.getString(farmer.getColumnIndex("place_id")),
                        farmer.getString(farmer.getColumnIndex("farmergroup_id")),
                        farmer.getString(farmer.getColumnIndex("section_id")),
                        farmer.getString(farmer.getColumnIndex("programme_id")),
                        farmer.getString(farmer.getColumnIndex("product_id")),
                        farmer.getString(farmer.getColumnIndex("partner_id"))
                };

                farmerModules = db.rawQuery(modulesQuery, modulesQueryValues);

                String[] mcColumns = new String[]{
                        "_id",
                        "name",
                        "name_fr",
                        "name_es",
                        "name_id",
                        "name_pt",
                        "name_tr",
                        "name_lo",
                        "name_vi",
                        "name_th",
                        "annual_survey",
                        "submodule_name",
                        "submodule_name_fr",
                        "submodule_name_es",
                        "submodule_name_id",
                        "submodule_name_pt",
                        "submodule_name_tr",
                        "submodule_name_lo",
                        "submodule_name_vi",
                        "submodule_name_th",
                        "expires",
                        "recurrence_frequency",
                        "recurrence_type",
                        "disabled_months",
                        "farmer_module_id",
                        "status_started",
                        "status_finished",
                        "current_submodule_id",
                        "recorded_lat",
                        "recorded_lng",
                        "moduleCode"
                };
                MatrixCursor mc = new MatrixCursor(mcColumns);
                Cursor[] cursors = {mc, farmerModules};
                output = new MergeCursor(cursors);
            }
            return output;
        }finally {
            farmer.close();
        }
    }


    // TODO remove annual survey and expires once no longer required.
    Cursor getFarmerModules(long id, Context context) {
        Cursor farmer = null;

        try {
            farmer = getFlattenedFarmer(id);
            Cursor output = null;

            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                String modulesQuery = "SELECT modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                        "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                        "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type, moduleassignments.disabled_months, " +
                        " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng FROM moduleassignments " +
                        " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                        " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                        "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                        " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                        "moduleassignments.childLabour != 'true' AND" +
                        "((moduleassignment_type = 'Country' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?))";
                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("region_id")),
                        farmer.getString(farmer.getColumnIndex("district_id")),
                        farmer.getString(farmer.getColumnIndex("place_id")),
                        farmer.getString(farmer.getColumnIndex("farmergroup_id")),
                        farmer.getString(farmer.getColumnIndex("section_id")),
                        farmer.getString(farmer.getColumnIndex("programme_id")),
                        farmer.getString(farmer.getColumnIndex("product_id")),
                        farmer.getString(farmer.getColumnIndex("partner_id"))
                };
                @SuppressLint("Recycle") Cursor farmerModules = db.rawQuery(modulesQuery, modulesQueryValues);

                String[] mcColumns = new String[]{
                        "_id",
                        "name",
                        "name_fr",
                        "name_es",
                        "name_id",
                        "name_pt",
                        "name_tr",
                        "name_lo",
                        "name_vi",
                        "name_th",
                        "annual_survey",
                        "submodule_name",
                        "submodule_name_fr",
                        "submodule_name_es",
                        "submodule_name_id",
                        "submodule_name_pt",
                        "submodule_name_tr",
                        "submodule_name_lo",
                        "submodule_name_vi",
                        "submodule_name_th",
                        "expires",
                        "recurrence_frequency",
                        "recurrence_type",
                        "disabled_months",
                        "farmer_module_id",
                        "status_started",
                        "status_finished",
                        "current_submodule_id",
                        "recorded_lat",
                        "recorded_lng"
                };
                MatrixCursor mc = new MatrixCursor(mcColumns);
                // add new record farm location task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                });

                // add new record farm polygon task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "farmpolygon"
                });

                // add new phone number updating task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "Phone"
                });

// add e-signature for existing farmers
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "Signature"
                });

                boolean isNIDAvailable = Utilities.getBoolFromSharedPrefs(context, "NationalID", false);
                if(isNIDAvailable) {
//         add national id information
                    mc.addRow(new String[]{"0", res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), "0", "", "", "", res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), "", "", "", "", "", "", "National_ID"});
                }
                Cursor fmp = getFmpByFarmerId(id);
                if (fmp.getCount() > 0) {
                    fmp.moveToFirst();
                    int fmpStarted = 0;
                    String fmpMode = "summary";
                    if (hasNqi("fmpreview", fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)))) {
                        fmpStarted = 1;
                    } else if (hasNqi("fullfmpreview", fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)))) {
                        fmpStarted = 1;
                        fmpMode = "full";
                    }

                    mc.addRow(new String[]{
                            "-" + fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            "",
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID))),
                            String.valueOf(fmpStarted),
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_HAS_FEEDBACK))),
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_STARTING_YEAR))),
                            fmpMode,
                            ""
                    });
                }

                MatrixCursor childOptionsCursor = new MatrixCursor(mcColumns);

                // add register child
                if (ChildLabourUtils.isChildEnumerator) {
                    childOptionsCursor.addRow(new String[]{
                            "0",
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ChildLabourUtils.REG_CHILD
                    });

                    // child labour survey
                    childOptionsCursor.addRow(new String[]{
                            "0",
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ChildLabourUtils.CHILDLABOUR_SURVEY
                    });
                }
                Cursor[] cursors = {mc, farmerModules,childOptionsCursor};
                output = new MergeCursor(cursors);
            }
            return output;
        }finally {
            farmer.close();
        }

    }

    // modules tasks
    Cursor getLookupValuesForQuestion(int questionId, String questionColumn) {
        String[] tableColumns = new String[]{questionColumn, "id as _id", OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION};

        String querySelection = OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] querySelectionValue = new String[]{String.valueOf(questionId)};
        String orderByValue = OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION + " ASC";

        return db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
    }
    //check for lookupvalue for the question id
    boolean getLookupValuesForQuestion(int questionId) {
        String querySelection = OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] querySelectionValue = new String[]{String.valueOf(questionId)};
        int count = db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME, null, querySelection, querySelectionValue, null, null, null).getCount();
        if(count >0){
            return true;
        }else
            return false;
    }

    // SIP Modules tasks
    Cursor getSipLookupValuesForQuestion(int questionId, String questionColumn) {
        String[] tableColumns = new String[]{questionColumn, "id as _id", OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_POSITION};

        String querySelection = OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] querySelectionValue = new String[]{String.valueOf(questionId)};
        String orderByValue = OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_POSITION + " ASC";

        return db.query(OFISDBContract.LookupValuesSipEntry.TABLE_NAME, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
    }

    public Cursor getSubmodulesByModuleId(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.SubmoduleEntry.COLUMN_NAME_POSITION + " ASC";

        return db.query(OFISDBContract.SubmoduleEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    //    get nationalids list
    public Cursor getnationalids() {
        String[] columns = new String[]{"*"};
//        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " = ?";
//        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.NationalIDEntry.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.NationalIDEntry.TABLE_NAME, columns, null, null, null, null, orderBy);
    }

    public void updateSyncedStatus(long farmerid){
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, true);
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);
    }


    public void updateModerationStatus(long farmerid, boolean moderationstatus){
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_MODERATION_STATUS, moderationstatus);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    public Cursor getnationalidsbyfarmerid(long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public boolean getSyncedStatus(long farmerId){
        boolean status = false;
        Cursor nids = getnationalidsbyfarmerid(farmerId);
        try {
            while (nids.moveToNext()) {
                String isSynced = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED));
                if(isSynced.equalsIgnoreCase("1")){
                    status = true;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            nids.close();
        }

        return status;
    }

    public boolean isValueavailable(long farmerId){
        boolean status = false;
        Cursor nids = getnationalidsbyfarmerid(farmerId);

        try {
            if(nids.getCount() > 0){
                status = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            nids.close();
        }

        return status;
    }

    public boolean isSignatureCaptured(long farmerId){
        boolean status = false;
        Cursor farmerdata = getFarmerById(farmerId);

        try {
            if(farmerdata.getCount() > 0){
                farmerdata.moveToFirst();
                String sign = farmerdata.getString(farmerdata.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
                if(! TextUtils.isEmpty(sign)){
                    status = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            farmerdata.close();
        }

        return status;
    }

    public boolean isUnderModeration(String nationalid, String type){
        boolean status = false;
        Cursor nids = getModerationStatusbyNID(nationalid, type);

        try {
            if(nids.getCount() > 0){
                status = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            nids.close();
        }
        return status;
    }

    boolean isFarmerEntryUnique(ContentValues values) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_DOB + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID + " = ? AND "
                + OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID + " = ?";
        String[] whereArgs = new String[]{values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID),
                values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)};

        Cursor existingFarmer = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        boolean idIsUnique = (existingFarmer.getCount() == 0);
        existingFarmer.close();

        return idIsUnique;
    }



    public Cursor getnidsfromlocaldb() {
        String[] columns = new String[]{"*"};
        String orderBy = OFISDBContract.LocalFarmerNationalIDs.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, columns, null, null, null, null, orderBy);
    }

    Cursor getRequiredQuestionIds(int moduleId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String submoduleTable = OFISDBContract.SubmoduleEntry.TABLE_NAME;
        String moduleTable = OFISDBContract.ModuleEntry.TABLE_NAME;
        String whereClause = moduleTable + "." + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " LIKE ?";
        String[] whereArgs = new String[]{
                String.valueOf(moduleId),
                "%\"requiredfield\":true%"
        };
        String sql = "SELECT " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID
                + ", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION
                + ", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE
                + " FROM " + questionTable + " INNER JOIN " + submoduleTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = " + submoduleTable + "." + OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID
                + " INNER JOIN " + moduleTable + " ON " + submoduleTable + "." + OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " = " + moduleTable + "."
                + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " WHERE " + whereClause;

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getRequiredSurveyRangeQuestionIds(int moduleId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String submoduleTable = OFISDBContract.SubmoduleEntry.TABLE_NAME;
        String moduleTable = OFISDBContract.ModuleEntry.TABLE_NAME;
        String whereClause = moduleTable + "." + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " LIKE ?";
        String[] whereArgs = new String[]{
                String.valueOf(moduleId),
                "%surveyrange%"
        };
        String sql = "SELECT " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID +", " +  OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION+", " +  OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " FROM " + questionTable + " INNER JOIN " + submoduleTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = " + submoduleTable + "." + OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID
                + " INNER JOIN " + moduleTable + " ON " + submoduleTable + "." + OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " = " + moduleTable + "."
                + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " WHERE " + whereClause;

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getQuestionsBySubModuleId(int id) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String sql = "SELECT " + questionTable + ".*, " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name FROM " + questionTable
                + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + whereClause + " ORDER BY " + orderBy;

        return db.rawQuery(sql, whereArgs);
    }


    Cursor getQuestionsBySubModuleId1(int id) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String whereClause = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ? ";

        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String sql = "SELECT " + questionTable + ".*, " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name FROM " + questionTable
                + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + whereClause + " ORDER BY " + orderBy;

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getQuestionsAndAnswersBySubModuleId(int submoduleId, long farmerId, long farmerModuleId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;
        String farmerQuery = "" + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                farmerQueryValue,
                String.valueOf(farmerModuleId),
                String.valueOf(submoduleId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getChildQuestionsAndAnswersBySubModuleId(int submoduleId, long farmerId, long farmerModuleId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerChildEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;
        String farmerQuery = "" + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                farmerQueryValue,
                String.valueOf(farmerModuleId),
                String.valueOf(submoduleId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getQuestionsAndAnswersBySubModuleId1(int submoduleId, long farmerId, long farmerModuleId, int question_id) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;
        String farmerQuery = "" + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?  GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                farmerQueryValue,
                String.valueOf(farmerModuleId),
                String.valueOf(submoduleId),
                String.valueOf(question_id)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersForQuestionByFarmer(int questionId, long farmerId, long farmerModuleId) {
        String[] columns = new String[]{"*"};
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery + " AND "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(questionId),
                farmerQueryValue,
                String.valueOf(farmerModuleId)
        };

        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    // get the child answers from dropdown
    Cursor getAnswersForQuestionByChild(int questionId, long farmerId, long farmerModuleId) {
        String[] columns = new String[]{"*"};
        String farmerQuery = OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery + " AND "
                + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(questionId),
                farmerQueryValue,
                String.valueOf(farmerModuleId)
        };

        return db.query(OFISDBContract.AnswerChildEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    public long insertFarmerModule(long moduleId, long farmerId, String olamFarmerId, int farmId, int subModuleId, int statusStarted, String recordedLat, String recordedLng) {
        ContentValues farmerModuleValues = new ContentValues();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID, moduleId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID, farmerId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID, farmId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED, 0);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LAT, recordedLat);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LNG, recordedLng);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STARTED_DATE, timestamp.toString());
        return db.insert(OFISDBContract.FarmerModulesEntry.TABLE_NAME, null, farmerModuleValues);
    }

    public long insertFarmerChildModule(long moduleId, long farmerId, String olamFarmerId, int farmId, int subModuleId, int statusStarted, String recordedLat, String recordedLng,String childId) {
        ContentValues farmerChildModuleValues = new ContentValues();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_MODULE_ID, moduleId);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARMER_ID, farmerId);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARM_ID, farmId);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_FINISHED, 0);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_RECORDED_LAT, recordedLat);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_RECORDED_LNG, recordedLng);
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STARTED_DATE, timestamp.toString());
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CHILD_ID,childId);
        return db.insert(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, null, farmerChildModuleValues);
    }

    Cursor getRepeaterItemsForQuestion(long id) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION;
        String sql = "SELECT " + columns + " FROM " + questionTable + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE replace(" + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ",' ','') like ? ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                "%repeater_id_:" + id + ",%"
        };

        return db.rawQuery(sql, whereArgs);
    }
    // select the sub question and data types
    Cursor getRepeaterItemsForQuestion1(long id) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION;
        String sql = "SELECT questions.id, datatypes.name as datatype_name, questions.position, questions.display_type, questions.question, " +
                "questions.question_fr, questions.question_es, questions.question_id, questions.question_pt, questions.question_tr, questions.question_lo," +
                " questions.question_vi, questions.question_th, questions.display_configuration FROM questions INNER JOIN datatypes ON " +
                "questions.datatype_id = datatypes.id WHERE  questions.id=" + id;


        return db.rawQuery(sql, null);
    }

    Cursor getRepeaterAnswersForQuestionByPosition(int questionId, int position, long farmerId, long farmerModuleId) {
        String[] columns = new String[]{"*"};

        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION
                + " = ? AND " + farmerQuery + " AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(questionId),
                String.valueOf(position),
                farmerQueryValue,
                String.valueOf(farmerModuleId)
        };

        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public void updateSubModuleAnswers(ArrayList<JSONObject> answerSet, long farmerModuleId, int subModuleId, long farmerId, String olamFarmerId, int farmId, String type) {
        try {
            // delete existing answers for this farmer and submodule
            String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
            String idColumn = (type.equalsIgnoreCase("fmp")) ? OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID : OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID;
            String farmerQuery = "" + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?";
            String farmerQueryValue = String.valueOf(farmerId);
            String[] whereArgs = new String[]{
                    String.valueOf(subModuleId),
                    farmerQueryValue,
                    String.valueOf(farmerModuleId)
            };
            String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " + farmerQuery + " AND " + idColumn + " = ? ";

            db.delete(answerTable, whereClause, whereArgs);

            // now add in answers
            for (int i = 0; i < answerSet.size(); i++) {
                JSONObject currentAnswer = answerSet.get(i);
                if (currentAnswer != null) {
                    int currentQuestionId = currentAnswer.optInt("question_id");
                    int parentQuestionId = currentAnswer.optInt("parent_question_id");
                    int repeaterPosition = currentAnswer.optInt("repeater_item_position", -1);
                    String datatype = currentAnswer.getString("datatype");
                    String answer = currentAnswer.getString("answer");
                    String sub_answers = currentAnswer.optString("sub_answers");
                    int reportingYear = currentAnswer.getInt("reporting_year");

                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, currentQuestionId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID, parentQuestionId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION, repeaterPosition);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID, subModuleId);
                    values.put(idColumn, farmerModuleId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARM_ID, farmId);
                    values.put("answer_" + datatype, answer);
                    if (sub_answers.length() > 0 && (!datatype.equalsIgnoreCase("text"))) {
                        values.put("answer_text", sub_answers);
                    }
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, reportingYear);

                    db.insert(OFISDBContract.AnswerEntry.TABLE_NAME, null, values);

                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error updating submodule answers", e));
            e.printStackTrace();
        }
    }

    public void updateSubModuleChildAnswers(ArrayList<JSONObject> answerSet, long farmerModuleId, int subModuleId, long farmerId, String olamFarmerId, int farmId, String type) {
        try {
            // delete existing answers for this farmer and submodule
            String answerTable = OFISDBContract.AnswerChildEntry.TABLE_NAME;
            String idColumn = (type.equalsIgnoreCase("fmp")) ? OFISDBContract.AnswerChildEntry.COLUMN_NAME_FMP_ID : OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID;
            String farmerQuery = "" + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID + " = ?";
            String farmerQueryValue = String.valueOf(farmerId);
            String[] whereArgs = new String[]{
                    String.valueOf(subModuleId),
                    farmerQueryValue,
                    String.valueOf(farmerModuleId)
            };
            String whereClause = OFISDBContract.AnswerChildEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " + farmerQuery + " AND " + idColumn + " = ? ";

            db.delete(answerTable, whereClause, whereArgs);

            // now add in answers
            for (int i = 0; i < answerSet.size(); i++) {
                JSONObject currentAnswer = answerSet.get(i);
                if (currentAnswer != null) {
                    int currentQuestionId = currentAnswer.optInt("question_id");
                    int parentQuestionId = currentAnswer.optInt("parent_question_id");
                    int repeaterPosition = currentAnswer.optInt("repeater_item_position", -1);
                    String datatype = currentAnswer.getString("datatype");
                    String answer = currentAnswer.getString("answer");
                    String sub_answers = currentAnswer.optString("sub_answers");
                    int reportingYear = currentAnswer.getInt("reporting_year");

                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID, currentQuestionId);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_PARENT_QUESTION_ID, parentQuestionId);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_REPEATER_ITEM_POSITION, repeaterPosition);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_SUBMODULE_ID, subModuleId);
                    values.put(idColumn, farmerModuleId);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARM_ID, farmId);
                    values.put("answer_" + datatype, answer);
                    if (sub_answers.length() > 0 && (!datatype.equalsIgnoreCase("text"))) {
                        values.put("answer_text", sub_answers);
                    }
                    values.put(OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, reportingYear);

                    db.insert(OFISDBContract.AnswerChildEntry.TABLE_NAME, null, values);

                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error updating submodule answers", e));
            e.printStackTrace();
        }
    }


    public void updateFarmerModule(long farmerModuleId, int subModuleId, int statusStarted, int statusFinished) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED, statusFinished);

        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        db.update(OFISDBContract.FarmerModulesEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    public void updateFarmerChildModule(long farmerModuleId, int subModuleId, int statusStarted, int statusFinished) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_FINISHED, statusFinished);

        String whereClause = OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        db.update(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, values, whereClause, whereArgs);
    }


    Cursor getNetworkQueueItem(Integer id) {
        String[] columns = new String[]{"*"};
        String whereClause = null;
        String[] whereArgs = null;
        String orderBy = null;
        if (id != null) {
            whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
            whereArgs = new String[]{String.valueOf(id)};
            return db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
        } else {
//            orderBy = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " ASC";
            return db.rawQuery("select * from " + OFISDBContract.NetworkQueueEntry.TABLE_NAME +
                            " order by case when " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE +" = 'farmer' " +
                            "THEN 1 when " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE +" = 'children' " +
                            "THEN 2 when " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE +" = 'Polygon recorded' " +
                            "THEN 3 when " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE +" = 'Location recorded' " +
                            "THEN 4 ELSE 5 END, "+ OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE ,
                    null);
        }

    }

    void deleteNetworkQueueItem(int networkQueueItemId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkQueueItemId)};
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    void deleteSipNetworkQueueItem(int networkQueueItemId) {
        String whereClause = OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkQueueItemId)};
        db.delete(OFISDBContract.AnswerSipEntry.TABLE_NAME, whereClause, whereArgs);
        deleteNetworkQueueItem(networkQueueItemId);
    }
    public void deleteNetworkQueueItemByFarmerModuleId(int farmerModuleId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    public long deleteNetworkQueueItemByFarmerId(int farmerId, String type) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId), type};
        return db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    public void deleteNetworkQueueItemByFarmerChildModuleId(int farmerModuleId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isFarmerSynced(long farmerId) {
        Cursor farmer = null;
        try {
            farmer = getFarmerById(farmerId);
            return farmer.moveToNext() && (farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 1);
        }finally {
            farmer.close();
        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isChildSynced(long childId) {
        Cursor child = null;
        try {
            child = getChildById(childId);
            return child.moveToNext() && (child.getInt(child.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED)) == 1);
        }finally {
            child.close();
        }

    }

    void markFarmerAsSynced(int localFarmerId, int id, int farmId, int localPlaceId, int newPlaceId,String olamId) {
        // check to see if new id is already existing
        Cursor newFarmer = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, new String[]{"id"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        // if not, update the farmer with the new id, and also update any other entries he might have anywhere else
//        if (newFarmer.getCount() == 0) {

        try {
            ContentValues newValues = new ContentValues();
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ID, id);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID, farmId);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, olamId);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED, 1);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_FARMER_ID, id);
            if (newPlaceId > 0) {
                newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, newPlaceId);
            }

            String[] whereArgs = new String[]{String.valueOf(localFarmerId)};

            if (db.update(OFISDBContract.FarmerEntry.TABLE_NAME, newValues, "id = ?", whereArgs) > 0) {
                // successfully updated farmer, now do the other things.
                db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, newValues, "id = ?", whereArgs);
                // update any saved answers for this farmer with the new id
                ContentValues farmerValues = new ContentValues();
                farmerValues.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID, id);
                farmerValues.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARM_ID, farmId);

                // Answer ket
                db.update(OFISDBContract.AnswerEntry.TABLE_NAME, farmerValues, OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                // update any saved modules
                db.update(OFISDBContract.FarmerModulesEntry.TABLE_NAME, farmerValues, OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);

                // Child labour update starts
                // child module Update values

                try {
                    db.update(OFISDBContract.AnswerChildEntry.TABLE_NAME, farmerValues, OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                }catch (Exception e){}

                try {
                    db.update(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, farmerValues, OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                }catch (Exception e){}

                try {
                    ContentValues farmerChildVisitValues = new ContentValues();
                    farmerChildVisitValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID, id);
                    db.update(OFISDBContract.ChildVisitDetails.TABLE_NAME, farmerChildVisitValues, OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                }catch (Exception e){}

                try {
                    ContentValues farmerChildValues = new ContentValues();
                    farmerChildValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, id);
                    db.update(OFISDBContract.ChildEntry.TABLE_NAME, farmerChildValues, OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                }catch (Exception e){}


                // Child labour update Ends



                ContentValues transacteeIdValue = new ContentValues();
                transacteeIdValue.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID, id);

                // update any saved transactions
                db.update(OFISDBContract.TransactionItem.TABLE_NAME, transacteeIdValue, OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID + " = ? AND " + OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE + " = 'farmer'", whereArgs);

                ContentValues farmerIdValue = new ContentValues();
                farmerIdValue.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID, id);

                // update any saved training attendees
                db.update(OFISDBContract.TrainingAttendee.TABLE_NAME, farmerIdValue, OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);

                // update any saved phone numbers
                db.update(OFISDBContract.PhoneNumber.TABLE_NAME, farmerIdValue, OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);

                // update any network queue items
                db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, farmerValues, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
            }

            // handle a new place being generated
            if (localPlaceId != newPlaceId && newPlaceId > 0) {
                ContentValues placeValues = new ContentValues();
                placeValues.put(OFISDBContract.PlacesEntry.COLUMN_NAME_ID, newPlaceId);
                placeValues.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE, "Town");
                db.update(OFISDBContract.PlacesEntry.TABLE_NAME, placeValues, OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(localPlaceId)});

                ContentValues placeRelatedValues = new ContentValues();
                placeRelatedValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, newPlaceId);
                // update any other farmers using this place id
                db.update(OFISDBContract.FarmerEntry.TABLE_NAME, placeRelatedValues, OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID + " = ?", new String[]{String.valueOf(localPlaceId)});
                db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, placeRelatedValues, OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID + " = ?", new String[]{String.valueOf(localPlaceId)});
                // update any network queue entries using this place id
                db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, placeRelatedValues, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID + " = ?", new String[]{String.valueOf(localPlaceId)});
            }
//        }
        }catch (Exception e){
            e.printStackTrace();
        }
        newFarmer.close();
    }

    Cursor getFarmerModuleById(int itemId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(itemId)};

        return db.query(OFISDBContract.FarmerModulesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getFarmerChildModuleById(int itemId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(itemId)};

        return db.query(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getAllAnswersByFarmerModuleId(int farmerModuleId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAllAnswersByFarmerChildModuleId(int farmerModuleId) {
        String answerTable = OFISDBContract.AnswerChildEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersByFarmerModuleId(int farmerModuleId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersByFarmerChildModuleId(int farmerModuleId) {
        String answerTable = OFISDBContract.AnswerChildEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersByFmpId(int fmpId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(fmpId)};

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersByModuleIdAndParentQuestionId(int farmerModuleId, int parentQuestionId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " ASC, " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{
                String.valueOf(parentQuestionId),
                String.valueOf(farmerModuleId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getChildAnswersByModuleIdAndParentQuestionId(int farmerModuleId, int parentQuestionId) {
        String answerTable = OFISDBContract.AnswerChildEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = ? AND " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " ASC, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{
                String.valueOf(parentQuestionId),
                String.valueOf(farmerModuleId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersByFmpIdAndParentQuestionId(int fmpId, int parentQuestionId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " ASC, " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{
                String.valueOf(parentQuestionId),
                String.valueOf(fmpId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Boolean hasNqi(String itemType, int itemId) {
        String[] columns = new String[]{"*"};
        String idColumn = (itemType.equalsIgnoreCase("fmpreview") || itemType.equalsIgnoreCase("fullfmpreview")) ? OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID : OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + idColumn + " = ?";
        String[] whereArgs = new String[]{
                itemType,
                String.valueOf(itemId)
        };
        @SuppressLint("Recycle") Cursor nqis = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return (nqis.getCount() > 0);
    }

    private Cursor getFmpByFarmerId(long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(farmerId)
        };
        return db.query(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getFmpSections() {
        return db.query(OFISDBContract.FarmManagementPlanSectionsEntry.TABLE_NAME, new String[]{"*"}, null, null, null, null, null);
    }

    Cursor getFmpEntriesBySectionId(long fmpId, int fmpSectionId) {
        String fe = OFISDBContract.FarmManagementPlanEntriesEntry.TABLE_NAME;
        String fv = OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME;
        String sql = "SELECT " + fe + ".*, " + fv + "." + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_ID + " AS value_id, " + fv + "." + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE + " FROM " + fe
                + " LEFT JOIN " + fv + " ON (" + fe + "." + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_ID + " = " + fv + "." + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ENTRY_ID + " AND "
                + fv + "." + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID + " = ?) " + "WHERE " + fe + "." + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_SECTION_ID + " = ? ORDER BY "
                + fe + "." + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_POSITION + " ASC";

        String[] whereArgs = new String[]{
                String.valueOf(fmpId),
                String.valueOf(fmpSectionId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getFmpValuesByFmpId(long fmpId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(fmpId)
        };
        String orderBy = OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    Cursor getCheckVisitEntry(long fmpId, String surveyCode) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ?  AND " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE + " = ?";

        String[] whereArgs = new String[]{
                String.valueOf(fmpId),
                surveyCode
        };
        String orderBy = OFISDBContract.ChildVisitDetails.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.ChildVisitDetails.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    Cursor getCurrentValuesByFmpId(long fmpId) {
        String fe = OFISDBContract.FarmManagementPlanEntriesEntry.TABLE_NAME;
        String fv = OFISDBContract.FarmManagementPlanCurrentValuesEntry.TABLE_NAME;
        String sql = "SELECT " + fv + "." + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_ID + ", " + fe + "." + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_NAME + ", "
                + fv + "." + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_VALUE + " FROM " + fv + " JOIN " + fe + " ON " + fv + "." + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_FMP_ENTRY_ID
                + " = " + fe + "." + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_ID + " WHERE " + fv + "." + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_FMP_ID + " = ? ";

        String[] whereArgs = new String[]{
                String.valueOf(fmpId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    void deleteFmpNetworkQueueItem(String submissionType, long farmerId, long fmpId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID + " = ? ";
        String[] whereArgs = new String[]{
                submissionType,
                String.valueOf(farmerId),
                String.valueOf(fmpId)
        };
        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    Cursor getFmpBaselineQuestions(long farmerId, long fmpId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?" + " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID + " = ? INNER JOIN "
                + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID
                + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " LIKE ? GROUP BY " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID
                + " ORDER BY " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " ASC, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                String.valueOf(farmerId),
                String.valueOf(fmpId),
                "%\"fmpreviewable\":true%"
        };

        return db.rawQuery(sql, whereArgs);
    }

    void syncFmp(int fmpId) {
        String delWhereClause = OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID + " = ?";
        String[] delWhereArgs = new String[]{String.valueOf(fmpId)};

        db.delete(OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME, delWhereClause, delWhereArgs);

        String updWhereClause = OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID + " = ?";
        String[] updWhereArgs = new String[]{String.valueOf(fmpId)};
        ContentValues updateValues = new ContentValues();
        updateValues.put(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_HAS_FEEDBACK, 1);

        db.update(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, updateValues, updWhereClause, updWhereArgs);
    }

    void syncFarmerModule(int farmerModuleId, long farmerId, int moduleId, String submissionDate) {
        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        db.delete(OFISDBContract.AnswerEntry.TABLE_NAME, whereClause, whereArgs);

        whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " = ?";
        db.delete(OFISDBContract.FarmerModulesEntry.TABLE_NAME, whereClause, whereArgs);

        Cursor farmer = getFarmerById(farmerId);

        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
            try {
                JSONObject newModuleEntry = new JSONObject();
                newModuleEntry.put("id", moduleId);
                newModuleEntry.put("created_at", submissionDate);
                JSONObject newModule = new JSONObject();
                newModule.put("module", newModuleEntry);

                String submittedModulesJSON = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES));
                String Samplestr = String.valueOf("{\"modules\":null}");
                if(Samplestr.equals(submittedModulesJSON)){
                    submittedModulesJSON =submittedModulesJSON.replaceAll("\"modules\":null","");
                }
                JSONObject submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();
                if (submittedModules.has("modules")) {
                    JSONArray modules = submittedModules.getJSONArray("modules");
                    modules.put(newModule);
                } else {
                    JSONArray newModules = new JSONArray();
                    newModules.put(newModule);
                    submittedModules.put("modules", newModules);
                }

                ContentValues updateValues = new ContentValues();
                updateValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES, submittedModules.toString());

                db.update(OFISDBContract.FarmerEntry.TABLE_NAME, updateValues, "id = ?", new String[]{String.valueOf(farmerId)});
                db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, updateValues, "id = ?", new String[]{String.valueOf(farmerId)});

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DataLayer: Error creating farmer submitted module", e));
                e.printStackTrace();
            }

        }
    }


    // Remove and update after sync
    void syncChildFarmerModule(int farmerModuleId, long childId, int moduleId, String submissionDate,String surveyCode) {
        String whereClause = OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        db.delete(OFISDBContract.AnswerChildEntry.TABLE_NAME, whereClause, whereArgs);

        whereClause = OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " = ?";
        db.delete(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, whereClause, whereArgs);


        // Instead of farmer, remove from child submitted modules thing

        Cursor child = getChildById(childId);
        if (child.getCount() > 0) {
            child.moveToFirst();
            try {


                JSONObject newModuleEntry = new JSONObject();
                newModuleEntry.put("id", moduleId);
                newModuleEntry.put("surveyCode",surveyCode);
                newModuleEntry.put("created_at", submissionDate);

//                JSONObject newModule = new JSONObject();
//                newModule.put("module", newModuleEntry);

                String submittedModulesJSON = child.getString(child.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES));
                // New Approach
                JSONObject submittedModules=null;


                if(submittedModulesJSON==null || submittedModulesJSON.equalsIgnoreCase("null")){
                    submittedModules=new JSONObject();


                    JSONArray moduleArray=new JSONArray();
                    moduleArray.put(newModuleEntry);
                    submittedModules.put("modules", moduleArray);

                }else {
                    submittedModules=new JSONObject(submittedModulesJSON);
                    JSONArray modulesArray= submittedModules.getJSONArray("modules");
                    modulesArray.put(newModuleEntry);
                }

//                if(submittedModulesJSON==null){
//                     submittedModules=new JSONObject();
//                    JSONArray newModules = new JSONArray();
//                    newModules.put(newModule);
//                    submittedModules.put("modules", newModules);
//
//                }else {
//                    // this section we need to work for multiple survey entries.
//                    String Samplestr = String.valueOf("{\"modules\":null}");
//                    if(Samplestr.equals(submittedModulesJSON)){
//                        submittedModulesJSON =submittedModulesJSON.replaceAll("\"modules\":null","");
//                    }
//                     submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();
//                    if (submittedModules.has("modules")) {
//                        JSONArray modules = submittedModules.getJSONArray("modules");
//                        modules.put(newModule);
//                    } else {
//                        JSONArray newModules = new JSONArray();
//                        newModules.put(newModule);
//                        submittedModules.put("modules", newModules);
//                    }
//                }



                ContentValues updateValues = new ContentValues();
                updateValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES, submittedModules.toString());

                db.update(OFISDBContract.ChildEntry.TABLE_NAME, updateValues, "tmpId = ?", new String[]{String.valueOf(childId)});

                // no backup table for child
//                db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, updateValues, "id = ?", new String[]{String.valueOf(farmerId)});

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DataLayer: Error creating farmer submitted module", e));
                e.printStackTrace();
            }

        }
    }

    void removeNIDPhotostatusByType(String type, long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? AND " +  OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{type, String.valueOf(farmerid)};

        db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
    }

    void updateNIDPhoto(int id, String photo) {
//        delete photo if exists
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, whereClause, whereArgs);

        ContentValues values = new ContentValues();
        values.put(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO, photo);
        values.put(OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE, id);
        db.insert(OFISDBContract.LocalNationalIDImages.TABLE_NAME,null, values);


    }

    void clearlocalniddbvalue(){
        db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, null, null);
        db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, null);
    }

    void removeNIDPhoto(int id) {
//        delete photo if exists
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, whereClause, whereArgs);


    }

    public Cursor getPhotoByType(int id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.LocalNationalIDImages.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    //JIRA req OW-505
    int updateFarmerPhoto(long farmerId, String farmerPhoto, String olamFarmerId, String farmerName) {

        int updated;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, farmerPhoto);
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY, 1);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, values, whereClause, whereArgs);

        if (updated > 0) {
            ContentValues nqiValues = new ContentValues();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmer_photo");
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmerName);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.Farmer_Photo));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_PHOTO, values.getAsDouble(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            if (networkEntryTypeExistsByFarmerId("farmer_photo", farmerId)) {
                updateNetworkQueueFarmerPhotoEntry(nqiValues, farmerId);
            } else {
                insertNetworkQueueEntry(nqiValues);
            }
        }

        return updated;
    }

    private static int updateNetworkQueueFarmerPhotoEntry(ContentValues nqiValues, long farmerId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{
                "farmer_photo",
                String.valueOf(farmerId)
        };

        return db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, nqiValues, whereClause, whereArgs);
    }

    Cursor getParentSubquestionAnswer(int submoduleid, int parentid, int questionId) {

        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Subquestionanswer.COLUMN_NAME_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(submoduleid), String.valueOf(parentid), String.valueOf(questionId)};


        return db.query(OFISDBContract.Subquestionanswer.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

    }



    //selcet the answer for sub question

    Cursor getParentSubquestionSelect(int submoduleid, int parentid) {

        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Subquestionanswer.COLUMN_NAME_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(submoduleid), String.valueOf(parentid)};


        return db.query(OFISDBContract.Subquestionanswer.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

    }

    //select the subquestion answers

    Cursor getSubquestionAnswerSelect(int submoduleid, int parentid, String farmer_id) {

        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.Subquestionanswer.COLUMN_NAME_MODULE_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(submoduleid), String.valueOf(parentid), String.valueOf(farmer_id)};


        String sql = "SELECT  * FROM " + OFISDBContract.Subquestionanswer.TABLE_NAME + " where " + whereClause;

        return db.rawQuery(sql, whereArgs);

    }

    //save subquestion answer

    long prentidAnswer(int id, int parentId, int questionId, String answerid, String position, String farmer_id, String module_id) {
        ContentValues values = new ContentValues();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_ID, id);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID, parentId);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID, questionId);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER, answerid + "");
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_POSITION, position + "");
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_FARMER_ID, farmer_id);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_MODULE_ID, module_id);
        values.put(OFISDBContract.Subquestionanswer.COLUMN_NAME_REPORT_YEAR, year + "");

        return db.insert(OFISDBContract.Subquestionanswer.TABLE_NAME, null, values);
    }

    //delete the subquestion answer

    int prentidAnswerDelete(int parentId, int questionId) {
        String whereClause = OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(parentId), String.valueOf(questionId)};
        return db.delete(OFISDBContract.Subquestionanswer.TABLE_NAME, whereClause, whereArgs);

    }


    int deleteDataYesOrONSelect(int parentId, int subModuleid) {
        String whereClause = OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " = ? and " + OFISDBContract.Subquestionanswer.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(parentId), String.valueOf(subModuleid)};
        return db.delete(OFISDBContract.Subquestionanswer.TABLE_NAME, whereClause, whereArgs);

    }


    public Cursor getExtraInfoByFarmerId(long farmerId, int moduleId) {
        String sql_query="select farmers.name as 'farmer.name', "
                + "farmers.olam_farmer_id, countries.id as 'country.id', "
                + "countries.name as 'country.name', modules.name as 'module.name',"
                + "farmergroups.name as 'farmergroup.name', farmergroups.id as 'farmergroup.id',"
                +" farmergroups.product_id as 'farmergroup.product_id', farmergroups.partner_id as 'farmergroup.partner_id' "
                + "from farmers "
                + "inner join places inner join districts inner join regions inner join countries "
                + "inner join modules inner join sections inner join farmergroups On "
                + "farmers.place_id=places.id  and farmers.section_id = sections.id and "
                + "sections.farmergroup_id=farmergroups.id and places.district_id = districts.id and "
                + "districts.region_id= regions.id and regions.country_id = countries.id "
                + "where farmers.id =" + farmerId +" and modules.id = " + moduleId;

        return db.rawQuery(sql_query, null);

    }

    public void updateSIPSubModuleAnswers(ArrayList<JSONObject> answerSet, int subModuleId, int farmId, String uploadId) {
        try {
            // delete existing answers_sip for this uploadId and submoduleid
            String answerTable = OFISDBContract.AnswerSipEntry.TABLE_NAME;
            String whereClause = OFISDBContract.AnswerSipEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " +
                    answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ?";
            String[] whereArgs = new String[]{
                    String.valueOf(subModuleId),
                    uploadId,
            };
            db.delete(answerTable, whereClause, whereArgs);

            // now add in answers_sip
            for (int i = 0; i < answerSet.size(); i++) {
                JSONObject currentAnswer = answerSet.get(i);
                if (currentAnswer != null) {
                    int currentQuestionId = currentAnswer.optInt("question_id");
                    int parentQuestionId = currentAnswer.optInt("parent_question_id");
                    int repeaterPosition = currentAnswer.optInt("repeater_item_position", -1);
                    String datatype = currentAnswer.getString("datatype");
                    String answer = currentAnswer.getString("answer");
                    String sub_answers = currentAnswer.optString("sub_answers");
//                    int reportingYear = currentAnswer.getInt("reporting_year");

                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID, uploadId);
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID, currentQuestionId);
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_PARENT_QUESTION_ID, parentQuestionId);
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_REPEATER_ITEM_POSITION, repeaterPosition);
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_SUBMODULE_ID, subModuleId);
                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_FARM_ID, farmId);
                    values.put("answer_" + datatype, answer);
                    if (sub_answers.length() > 0 && (!datatype.equalsIgnoreCase("text"))) {
                        values.put("answer_text", sub_answers);
                    }

                    Date today = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String reportingYear = format.format(today);

                    values.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, reportingYear);

                    db.insert(OFISDBContract.AnswerSipEntry.TABLE_NAME, null, values);

                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error updating submodule answers", e));
            e.printStackTrace();
        }
    }

    public Cursor getAllAnswersSipByUploadId(String UploadId) {
        String answerTable = OFISDBContract.AnswerSipEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ? ORDER BY " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(UploadId)};

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getAnswersSipByUploadId(String UploadId) {
        String answerTable = OFISDBContract.AnswerSipEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesSipEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ? ORDER BY " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(UploadId)};

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getSipRequiredQuestionIds(int moduleId, int subModuleId) {
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String submoduleTable = OFISDBContract.SipSubGroupEntry.TABLE_NAME;
        String moduleTable = OFISDBContract.SipGroupEntry.TABLE_NAME;
        String whereClause = moduleTable + "." + OFISDBContract.SipGroupEntry.COLUMN_NAME_ID + " = ? " +
                "AND " +submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " LIKE ?";
        String[] whereArgs = new String[]{
                String.valueOf(moduleId),
                String.valueOf(subModuleId),
                "%\"requiredfield\":true%"
        };
        String sql = "SELECT " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " FROM " + questionTable + " INNER JOIN " + submoduleTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_SUBMODULE_ID + " = " + submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID
                + " INNER JOIN " + moduleTable + " ON " + submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " = " + moduleTable + "."
                + OFISDBContract.SipGroupEntry.COLUMN_NAME_ID + " WHERE " + whereClause;

        return db.rawQuery(sql, whereArgs);
    }

    public long insertNetworkQueueSipEntry(ContentValues values, String randomId) {
        long newRowId;
        if (!values.containsKey(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)) {
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, randomId);
        }
        newRowId = db.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values);
        return newRowId;
    }

    public Cursor getSIPModules() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.SipGroupEntry.TABLE_NAME, columns, null, null, null, null, null);
    }

    public Cursor getSipSubmodulesByModuleId(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " ASC";//OFISDBContract.SipSubGroupEntry.COLUMN_NAME_POSITION + " ASC";

        return db.query(OFISDBContract.SipSubGroupEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getQuestionsSipBySubModuleId(int id) {
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesSipEntry.TABLE_NAME;
        String whereClause = OFISDBContract.QuestionSipEntry.COLUMN_NAME_SUBMODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + " ASC";
        String sql = "SELECT " + questionTable + ".*, " + datatypeTable + "."
                + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name FROM " + questionTable
                + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_ID + " WHERE "
                + whereClause + " ORDER BY " + orderBy;

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getSIPQuestionsAndAnswersByUploadId(String ulpoadID, int submoduleId/* long farmerId, long farmerModuleId*/) {
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesSipEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerSipEntry.TABLE_NAME;

        String columns = questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;
        String Query = "" + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ?"
                + " AND " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_SUBMODULE_ID + " =?";

        String sql = "SELECT " + columns + " FROM " + answerTable + " LEFT OUTER JOIN " + questionTable+" INNER JOIN datatypes_sip "
                + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_ID
                + " AND " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID
                + " WHERE " + Query +" GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                ulpoadID,
                String.valueOf(submoduleId)
        };

        return db.rawQuery(sql, whereArgs);
    }

    public boolean isIdAvailablePhoneDB(long farmerLong) {
        boolean available=false;
        String sql="select * from phonenumbers where farmer_id ="+farmerLong;
        if(db.rawQuery(sql,null).getCount()>0){
            available=true;
        }
        return available;
    }

    public Cursor getEnumeratorCountries() {
        return db.rawQuery("SELECT * FROM countries", null);
    }

    public Cursor getAllPlaces(){return db.rawQuery("SELECT * FROM places", null);}

    public long insertChild(Context context,ContentValues contentValues,String name) {
        long newChildId;
        newChildId = db.insert(OFISDBContract.ChildEntry.TABLE_NAME, null, contentValues);

        if (newChildId != -1) {
            // add a network queue entry
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "children");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, context.getResources().getString(R.string.new_child));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID, newChildId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID,name);
            insertNetworkQueueEntry(networkQueueValues);
        }

        return newChildId;
    }

    public long updateChild(Context context,ContentValues contentValues, String id,String name,String childName) {
        long newChildId;
        long update_nq = 0;
        String whereClause = OFISDBContract.ChildEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_ID))};
        newChildId = db.update(OFISDBContract.ChildEntry.TABLE_NAME, contentValues,whereClause, whereArgs);



        if (newChildId != -1) {
            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "children");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE,context.getResources().getString(R.string.new_child));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID,name);

            // update data from network queue entry
            String d_whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";;
            String[] d_whereArgs = new String[]{String.valueOf(id),"children"};
            update_nq = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, networkQueueValues,d_whereClause, d_whereArgs);

           /* long d_status = db.delete(nqValuesTable, d_whereClause, d_whereArgs);
            // update a network queue entry
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if (d_status != -1) {
                ContentValues networkQueueValues = new ContentValues();
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "children");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "Child Registered");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID, newChildId);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
                insertNetworkQueueEntry(networkQueueValues);
            }*/
        }
        try {
            ContentValues networkQueueValuesChildModule=new ContentValues();
            networkQueueValuesChildModule.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE,context.getResources().getString(R.string.child) + ": "+childName);
            String d_whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";;
            String[] d_whereArgs = new String[]{String.valueOf(id),"childmodule"};
            update_nq = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, networkQueueValuesChildModule,d_whereClause, d_whereArgs);
        }catch (Exception e){}



        return update_nq;
    }

    public long insertChildVisitDetails(ContentValues contentValues) {
        long newRowId;
        newRowId = db.insert(OFISDBContract.ChildVisitDetailsEntry.TABLE_NAME, null, contentValues);
        if (newRowId != -1) {
            // add a network queue entry
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "child_visit_details");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "Child Visit Detail Registered");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, "Child Name");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            insertNetworkQueueEntry(networkQueueValues);
        }
        return  newRowId;
    }

    public long insertChildVisitSurveyDetails(ContentValues contentValues,String name,String type) {
        long newRowId;
        newRowId = db.insert(OFISDBContract.ChildVisitDetails.TABLE_NAME, null, contentValues);
        if (newRowId != -1) {
            // add a network queue entry
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "child_visit");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, name);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_SURVEY_CODE, contentValues.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, type);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, "");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            insertNetworkQueueEntry(networkQueueValues);
        }
        return  newRowId;
    }

    public Cursor getChildVisitDetailsById(Long farmerId) {
        return db.rawQuery("SELECT * FROM child_visit where farmerId="+farmerId, null);
    }

    public long deleteChildVisitDetailsById(Long farmerId) {
        String whereClause =  OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(farmerId),"0"};
        return db.delete(OFISDBContract.ChildVisitDetails.TABLE_NAME, whereClause, whereArgs);
    }

    public Cursor getChildDetailsById(Long farmerId) {
        return db.rawQuery("SELECT * FROM children where farmerId="+farmerId, null);
    }


    public Cursor getChildById(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.ChildEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.query(OFISDBContract.ChildEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public void updateChildId(int tempChildId, int generatedChildId) {

        ContentValues cv = new ContentValues();
        cv.put(OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID,generatedChildId); //These Fields should be your String values of actual column names
        cv.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED,1);
        db.update(OFISDBContract.ChildEntry.TABLE_NAME, cv, "tmpId="+tempChildId, null);



//        String[] whereArgs = new String[]{String.valueOf(tempChildId)};
//        try {
//            ContentValues farmerValuesCv=new ContentValues();
//            farmerValuesCv.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CHILD_ID,generatedChildId);
//            db.update(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, farmerValuesCv, OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CHILD_ID + " = ?", whereArgs);
//        }catch (Exception e){}
//
//         try {
//             ContentValues childNetworkCV=new ContentValues();
//             childNetworkCV.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID,generatedChildId);
//             db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, childNetworkCV, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " = ?", new String[]{String.valueOf(tempChildId)});
//         }catch (Exception e){}


    }

    public void updateCheckdetails(int fmpId, int surveyCode) {


        ContentValues cv = new ContentValues();
        cv.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED,1);
        String whereClause = OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ?  AND " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE + " = ?";

        String[] whereArgs = new String[]{
                String.valueOf(fmpId),
                String.valueOf(surveyCode)
        };


        db.update(OFISDBContract.ChildVisitDetails.TABLE_NAME, cv, whereClause, whereArgs);

    }

    public Long getChildServerId(long childNwId) {
        Long serverId = null;
        Cursor cursor = db.rawQuery("SELECT * FROM children where tmpId=" + childNwId, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                serverId = Long.valueOf(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID)));
            }
        }
        return  serverId;
    }

    public long insertGeoData(Context context, ContentValues contentValues) {
        long newGeoId;
        newGeoId = db.insert(OFISDBContract.GPSDetails.TABLE_NAME, null, contentValues);
        return newGeoId;
    }

    public Cursor getGeoTrackList() {
        return db.rawQuery("SELECT * FROM "+OFISDBContract.GPSDetails.TABLE_NAME, null);
    }

    public long insertGPSDetailsNetworkQueue(Context context,ContentValues contentValues) {

        long newGpsEntry;
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "gps_track");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, Utilities.getEnumeratorNameSharedPrefs(context));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, context.getResources().getString(R.string.enumerator_location_capture));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, Utilities.getFromSharedPrefs(context));


        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, contentValues.getAsString(OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, contentValues.getAsString(OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT,timestamp.toString());

        newGpsEntry= insertNetworkQueueEntry(networkQueueValues);

        return newGpsEntry;

    }


    public void deleteGeoPoints() {
        db.delete(OFISDBContract.GPSDetails.TABLE_NAME, null, null);
    }

    public long updateSignature(Context context, long farmerId, String signature, String farmername) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.SignatureEntry.COLUMN_NAME_FAMRERID, farmerId);
        values.put(OFISDBContract.SignatureEntry.COLUMN_NAME_SIGNATURE, signature);
        updateNetworkQueueSignatureEntry(context, farmerId, signature,farmername);
        return db.insert(OFISDBContract.SignatureEntry.TABLE_NAME, null, values);

    }

    private long updateNetworkQueueSignatureEntry(Context context, long farmerId, String signature, String name) {
        long newEntry;
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "signature");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "Signature");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, name.trim());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, " ");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SIGNATURE, signature);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT,timestamp.toString());

        newEntry= insertNetworkQueueEntry(networkQueueValues);

        return newEntry;
    }

    // getting nationalids type id values
    public String getNidTypebyid(int id) {
        String type=null;
        Cursor  nid=getNIDById(id);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE));
            String language = Locale.getDefault().getLanguage();
            if(language.equalsIgnoreCase(Utilities.FR)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR));
            }else if(language.equalsIgnoreCase(Utilities.ES)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES));
            }else if(language.equalsIgnoreCase(Utilities.PT)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT));
            }else if(language.equalsIgnoreCase(Utilities.ID)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID));
            }else if(language.equalsIgnoreCase(Utilities.TR)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR));
            }else if(language.equalsIgnoreCase(Utilities.LO)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO));
            }else if(language.equalsIgnoreCase(Utilities.VI)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI));
            }else if(language.equalsIgnoreCase(Utilities.TH)){
                type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH));
            }
        }
        return  type;
    }

    private long updateNetworkQueueNIDEntry(Context context, long farmerId, int nationalidtype, String farmername) {
        long newEntry;
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "National_ID");
        /*networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, Utilities.getEnumeratorNameSharedPrefs(context));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, context.getResources().getString(R.string.add_nationalid));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, Utilities.getFromSharedPrefs(context));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);*/
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, context.getResources().getString(R.string.nationalid));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmername);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, getNidTypebyid(nationalidtype));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT,timestamp.toString());

        newEntry= insertNetworkQueueEntry(networkQueueValues);

        return newEntry;
    }
}
