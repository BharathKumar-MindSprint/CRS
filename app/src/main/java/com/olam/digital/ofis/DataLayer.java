package com.olam.digital.ofis;

import static com.olam.digital.ofis.Utilities.datatypeIdCLMRS;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.StakeHolderDto;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstants;
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.downloadsurvey.dto.FarmerDTO;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.dto.PlotDTO;
import com.olam.digital.ofis.farmstakeholder.FarmerRoleDto;
import com.olam.digital.ofis.model.Attendee;
import com.olam.digital.ofis.model.AttendeeRelations;
import com.olam.digital.ofis.model.FarmerLeadDTO;
import com.olam.digital.ofis.model.GeoDTO;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.FarmerGeo;
import com.olam.digital.ofis.multifarm.MultiFarmRoleDTO;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.OnGroundSurveyNotification;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Created by bengallienne on 29/11/14.
 * Data functions for interacting with the SQLite database
 */
public class DataLayer {

    private static final String TAG = DataLayer.class.getSimpleName();


    private static Resources res;
    private static SQLiteDatabase db;
    private static OFISRoomDB ofisRoomDB;


    // constructor - update
    public DataLayer(Resources res_, Context context_) {
        super();
        res = res_;
        db = Utilities.getWriteableDb(context_);
        ofisRoomDB = OFISRoomDB.getDbInstance(context_);
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

    long insertNewPlace(String newPlaceName, long district_id, long subdistrict_id) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME, newPlaceName);
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID, district_id);
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID, subdistrict_id);
        values.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE, "localinsert");

        return db.insert(OFISDBContract.PlacesEntry.TABLE_NAME, null, values);
    }

    Cursor getPlaceById(int placeId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(placeId)};

        return db.query(OFISDBContract.PlacesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getPlaceBytype(String type) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(type)};

        return db.query(OFISDBContract.PlacesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getFGById(int Id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Id)};

        return db.query(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getSectionById(int Id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Id)};
        return db.query(OFISDBContract.SectionsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public int updateFarmPolygon(long farmerId, String plotJson, String olamFarmerId, String farmerName, String plotId) {
        int updated;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PLOTS, plotJson);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, values, whereClause, whereArgs);

        if (updated > 0) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            boolean updateWaiting = networkEntryTypeExistsByFarmerId("farmpolygon", farmerId, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID);
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
                nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_NAME, plotId);
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
    public void updatePlotGeoData(String mOlamFarmerId, String plotName, String plotGeoData, String enteredPlotName,long farmerId, String plotGpx, double plotArea) {
        String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " = ?";
        String[] whereArgs = new String[]{String.valueOf(plotName)};
        db.delete(OFISDBContract.PlotGeoData.TABLE_NAME, whereClause, whereArgs);

        ContentValues plotGeoValues = new ContentValues();
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME, plotName);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID, mOlamFarmerId);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_FARMER_ID, farmerId);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_ENTEREDPLOT_NAME, enteredPlotName);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_CENTER_PLOT, plotGpx);
        plotGeoValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SIZE, plotArea);

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

    public void updateSipPlotGeoData(int category_sip_id, String category_name, String plotGeoData, String sip_name, String uploadId) {
        ContentValues plotGeoValues = new ContentValues();
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_CATEGORY_ID, category_sip_id);
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_CATEGORY, category_name);
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_NAME, sip_name);
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID, uploadId);
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_PLOT, plotGeoData);
        plotGeoValues.put(OFISDBContract.SipPlot.COLUMN_NAME_IS_SYNCED, "false"); //default is false, once it will sync with server it will update with true
        long plotGeoDBInsert = db.insert(OFISDBContract.SipPlot.TABLE_NAME, null, plotGeoValues);
        if (plotGeoDBInsert == -1) {
            Log.e(TAG, "Error to insert data into the PlotGeoData DB");
        }
    }

    public int getSipPlot(String id) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + OFISDBContract.SipPlot.TABLE_NAME + " WHERE " + OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void deletePlotDataFromNetworkQueue(String plotName){
        try {
            String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(plotName)};
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updatePlottedName(int selectedPlotId, String plotName, long mFarmerId , String plotGpx, double plotArea) {
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOTTED_NAME, plotName);
            values.put(OFISDBContract.PlotEntry.COLUMN_NAME_CENTER_PLOT, plotGpx);
            values.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_SIZE, plotArea);
            String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND " +
                    OFISDBContract.PlotEntry.COLUMN_NAME_ID + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(mFarmerId),String.valueOf(selectedPlotId)};
            db.update(OFISDBContract.PlotEntry.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
        }
    }

    public int fetchPlotData(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor data = db.query(OFISDBContract.PlotGeoData.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();

    }

    public Cursor fetchAllPlotIds(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data;
    }

    public Cursor syncedPlotIds(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " == ? ";
        String[] whereArgs = new String[]{String.valueOf(id),"1"};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data;
    }

    public String fetchCenterPlotById(String id) {
        String centerPlot = "";
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_ID + " = ? AND " + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(id),"0"};
            Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (data != null) {
                while(data.moveToNext()) {
                    centerPlot = data.getString(data.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_CENTER_PLOT));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return centerPlot;
    }

    public ArrayList<PlotDTO> fetchSyncedPlotIds(String id) {
        ArrayList<PlotDTO> plotDTOArrayList = new ArrayList<>();
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id),"1"};
        Cursor plotIds = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        int i = 0;
        while (plotIds.moveToNext()) {
            i++;
            PlotDTO plotDTO = new PlotDTO();
            int plotId = plotIds.getInt(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_ID));
            String plotName = "Plot " + i;
            plotDTO.setPlotId(plotId);
            plotDTO.setPlotName(plotName);
            plotDTO.setPlotActualName(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME)));
            plotDTO.setDeforestationPlot(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_DEFORESTATION_PLOT)).equalsIgnoreCase("true"));
            plotDTO.setPALegalityPlot(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_PLOT)).equalsIgnoreCase("true"));
            plotDTO.setCleansedStatus(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_CLEANSED_STATUS)));
            plotDTO.setEUDRSurveyCompleted(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED)).equalsIgnoreCase("true"));
            plotDTO.setPALegalitySurveyCompleted(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED)).equalsIgnoreCase("true"));
            plotDTO.setGeoDbFlagStatus(plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_GEO_DB_FLAG_STATUS)));
            plotDTOArrayList.add(plotDTO);
        }

        return plotDTOArrayList;
    }

    public int fetchPlotCountByFarmerID(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id),"0"};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();
    }

    public int fetchSyncedPlotCountByFarmerID(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id),"1"};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();

    }

    public int fetchSyncedSipPlotCountById(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SipPlot.COLUMN_NAME_SIP_CATEGORY_ID + " = ? AND "
                + OFISDBContract.SipPlot.COLUMN_NAME_IS_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id),"1"};
        Cursor data = db.query(OFISDBContract.SipPlot.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();
    }

    public int fetchPlotTotalCount(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return data.getCount();

    }

    public String getPlottedNameFromPlotGeoData(long farmerId, String plotName){
        String plottedName = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.PlotGeoData.COLUMN_NAME_ENTEREDPLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId), plotName};
            cursor = db.query(OFISDBContract.PlotGeoData.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    plottedName = cursor.getString(cursor.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            plottedName = null;
        }finally {
            cursor.close();
        }
        return plottedName;
    }
    public String getCenterPlotFromPlotGeoData(long farmerId, String plotName, boolean isPlotArea){
        String centerPlot = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.PlotGeoData.COLUMN_NAME_ENTEREDPLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId), plotName};
            cursor = db.query(OFISDBContract.PlotGeoData.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    if (isPlotArea) {
                        centerPlot = cursor.getString(cursor.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SIZE));
                    }
                    else {
                        centerPlot = cursor.getString(cursor.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_CENTER_PLOT));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            centerPlot = null;
        }finally {
            cursor.close();
        }
        return centerPlot;
    }

    public void insertNewPlot(String farmerId, ArrayList<String> allPlotValues) {
        Cursor plotIdsSynced = syncedPlotIds(farmerId);
        int existingPlots = 0;
        if (plotIdsSynced.getCount() > 0) {
            existingPlots = plotIdsSynced.getCount();
        }
        if(allPlotValues.size() > 0){
            String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                    + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(farmerId),"0"};

            db.delete(OFISDBContract.PlotEntry.TABLE_NAME, whereClause, whereArgs);
        }
        for(int i=0; i<allPlotValues.size(); i++) {
            if(i >= existingPlots) {
                Long tsLong = System.currentTimeMillis() / 1000;
                int tempval = (int) (tsLong + allPlotValues.size() + i);
                ContentValues plotValues = new ContentValues();
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID, farmerId);
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED, "0");
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME, allPlotValues.get(i));
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOTTED_NAME, getPlottedNameFromPlotGeoData(Long.parseLong(farmerId), allPlotValues.get(i)));
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_CENTER_PLOT, getCenterPlotFromPlotGeoData(Long.parseLong(farmerId), allPlotValues.get(i), false));
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_SIZE, getCenterPlotFromPlotGeoData(Long.parseLong(farmerId), allPlotValues.get(i), true));
                plotValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_ID, tempval);
                long plotGeoDBInsert = db.insert(OFISDBContract.PlotEntry.TABLE_NAME, null, plotValues);

                if (plotGeoDBInsert == -1) {
                    Log.e(TAG, "Error to insert data into the PlotEntry DB");
                }
            }
        }
    }

    public FarmerDTO getFarmerDTO(String farmerId) {
        Cursor cursor = getFarmerById(Long.parseLong(farmerId));
        FarmerDTO farmerDTO = new FarmerDTO();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            farmerDTO = new FarmerDTO();
            farmerDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
            farmerDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
            farmerDTO.setSynced(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)));
            farmerDTO.setCreatedAt(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGISTER_DATE)));
            farmerDTO.setSubmittedModules(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES)));

        }

        return farmerDTO;
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

    //------------------------Temp Functions for DB data check - End-----------------------------

    public Cursor getPlotGeoData() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.PlotGeoData.TABLE_NAME, columns, null, null, null, null, null);
    }

    public Cursor getPNamePlotGeoData(String mOlamFarmerid) {
        String[] columns = new String[]{OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME};
        String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(mOlamFarmerid)};
        return db.query(OFISDBContract.PlotGeoData.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    public void updatePlotGeoDataAfterSync(String plotId, String plotName) {
        try {
            ContentValues updatePlotGeoDataAfterSyncValues = new ContentValues();
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_SERVER_ID, plotId);
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SYNC_FLAG, "true");
            String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(plotName)};
            db.delete(OFISDBContract.PlotGeoData.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateGeoPlotDataAfterDelete(String pName , int plotId , String mOlamFarmerId){
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME, "plot"+ plotId +"_" + mOlamFarmerId);
            String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(pName)};
            db.update(OFISDBContract.PlotGeoData.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updatePlotDataAfterSync(String plotId, String plotName, String farmerId, String plottedName, String created_date) {
        try {
            String plotTable = OFISDBContract.PlotEntry.TABLE_NAME;
            String where = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                    + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ? AND "
                    + OFISDBContract.PlotEntry.COLUMN_NAME_PLOTTED_NAME + " = ? ";
            String[] args = new String[]{String.valueOf(farmerId),"0",plottedName};
            db.delete(plotTable, where, args);
            ContentValues updatePlotGeoDataAfterSyncValues = new ContentValues();
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_ID, plotId);
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED, "1");
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID, farmerId);
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME, plotName);
            updatePlotGeoDataAfterSyncValues.put(OFISDBContract.PlotEntry.COLUMN_NAME_CREATED_AT, created_date);
            db.insert(OFISDBContract.PlotEntry.TABLE_NAME, null, updatePlotGeoDataAfterSyncValues);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    int updateHouseholdLocation(long farmerId, Location location, String olamFarmerId, int farmId, String farmerName) {
        int updated;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LAT, location.getLatitude());
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LNG, location.getLongitude());
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_ID, 0);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        if (updated > 0) {
            ContentValues nqiValues = new ContentValues();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "Household");
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmerName);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.record_household_location_complete));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, values.getAsDouble(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LAT));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, values.getAsDouble(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LNG));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            insertNetworkQueueEntry(nqiValues);
        }

        return updated;
    }

    long updateFarmer(ContentValues values, long farmerId) {
        long district_id = values.getAsLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        values.remove(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        int updated = 0;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        try {
            updated = db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
            if (updated > 0) {
                ContentValues networkQueueValues = new ContentValues();
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "farmer");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.new_farmer));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
                updateNetworkQueueNewFarmerEntry(networkQueueValues, farmerId, "farmer");
            }
        }catch (Exception e){e.printStackTrace();}
        return updated;
    }

    void updateFarmerMs(MultiStakeHolder multiStakeHolder, long stakeHolderId) {
        try {
            multiStakeHolder.msId = stakeHolderId;
            if (ofisRoomDB.multiStakeHolderDao().updateStakeholder(multiStakeHolder) > 0 ) {
                ContentValues networkQueueValues = new ContentValues();
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "stakeholder");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, multiStakeHolder.msName);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.new_farmer));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, multiStakeHolder.farmerId);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeHolderId);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, multiStakeHolder.olamMultiStakeholderId);
                updateNetworkQueueNewFarmerEntry(networkQueueValues, stakeHolderId, "stakeholder");
            }
        }catch (Exception e){e.printStackTrace();}
    }

    long insertFarmer(ContentValues values) {
        long district_id = values.getAsLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        values.remove(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID);
        long newRowId;
        newRowId = db.insert(OFISDBContract.FarmerEntry.TABLE_NAME, null, values);
        try {
            db.insert(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, null, values);
        }catch (Exception e){}
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

    long insertStakeHolder(MultiStakeHolder multiStakeHolder) {
        long stakeHolderId = multiStakeHolder.msId = ofisRoomDB.multiStakeHolderDao().insertAndReturnId(multiStakeHolder);
        if (stakeHolderId != -1) {
            // add a network queue entry
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "stakeholder");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, multiStakeHolder.msName);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.new_stakeholder));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeHolderId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, multiStakeHolder.farmerId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, multiStakeHolder.olamMultiStakeholderId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            insertNetworkQueueEntry(networkQueueValues);
        }
        return stakeHolderId;
    }

    private static void updateNetworkQueueNewFarmerEntry(ContentValues values, long farmerId, String submissionType) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{
                submissionType,
                String.valueOf(farmerId)
        };
        try {
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
    }

    public void updateNetworkQueueFarmerAndChildModuleEntry(ContentValues values, long farmerModuleId, String type, int plotId) {
        try {
            String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND "
                    + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND"
                    + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " = ?";
            String[] whereArgs = new String[]{
                    String.valueOf(farmerModuleId),
                    type,
                    String.valueOf(plotId)
            };
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
    }

    int updateNetworkQueueFarmPolygonEntry(ContentValues values, long farmerId) {
        int updated = 0;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        try {
            updated = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
        return updated;
    }

    public int updateNetworkQueueEntry(ContentValues values, long networkEntryId) {
        int updated=0;
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkEntryId)};
        try {
            updated = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
        return updated;
    }

    public boolean isPlotSavedinNetworkLayer(long farmerId, int plotId, long farmerModuleId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                +OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " = ? AND "
                + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId), String.valueOf(plotId), String.valueOf(farmerModuleId)};
        Cursor cursor = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public long insertNetworkQueueEntry(ContentValues values) {
        long newRowId=0;
        if (!values.containsKey(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)) {
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, UUID.randomUUID().toString());
        }
        try {
            newRowId = db.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values);
        }catch (Exception e){}
        return newRowId;
    }

    boolean networkEntryTypeExistsByFarmerId(String submission_type, long farmerId, String columnName) {
        String[] columns = new String[]{"id"};
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + columnName + " = ?";
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
        } finally {
            if (nqi != null)
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
            if (activeTasks != null)
                activeTasks.close();
        }

        return activeTaskCount;
    }

    void deleteNetworkQueueItemByfarmerId(long farmerId , int id_type) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " +
                OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " +
                OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(farmerId),"National_ID", String.valueOf(id_type)};
        try {
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

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
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            e.printStackTrace();
        }
    }

    // training
    long insertTrainingRecord(ContentValues values) {
        long newRowId = 0;
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            values.put(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME, timestamp.toString());
            newRowId = db.insert(OFISDBContract.TrainingRecord.TABLE_NAME, null, values);
        }catch (Exception e){}
        return newRowId;
    }

    int updateTrainingRecord(ContentValues values, long trainingRecordId) {
        String whereClause = OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};

        return db.update(OFISDBContract.TrainingRecord.TABLE_NAME, values, whereClause, whereArgs);
    }

    int deleteTrainingRecord(long trainingRecordId) {
        int rowId=0;
        try {
            String attendeeWhereClause = OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
            String[] attendeeWhereArgs = new String[]{String.valueOf(trainingRecordId)};
            db.delete(OFISDBContract.TrainingAttendee.TABLE_NAME, attendeeWhereClause, attendeeWhereArgs);

            String queueWhereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
            String[] queueWhereArgs = new String[]{String.valueOf(trainingRecordId)};
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, queueWhereClause, queueWhereArgs);

            String whereClause = OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};

            rowId= db.delete(OFISDBContract.TrainingRecord.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
        return  rowId;
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
    public Cursor getFarmerPhoneNumbers(long id, boolean primaryOnly, String columnName) {
        String numbersTable = OFISDBContract.PhoneNumber.TABLE_NAME;
        String countriesTable = OFISDBContract.CountriesEntry.TABLE_NAME;

        String sql = "SELECT " + numbersTable + ".*, " + countriesTable + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE + " "
                + "FROM " + numbersTable + " LEFT JOIN " + countriesTable + " ON " + numbersTable + "." + OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID
                + " = " + countriesTable + "." + OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " WHERE " + numbersTable + "." + columnName
                + " = ? ";

        if (primaryOnly) {
            sql += " AND " + numbersTable + "." + OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY + " = 1 LIMIT 1";
        }

        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.rawQuery(sql, whereArgs);
    }

    void addFarmerPhoneNumbers(Long farmerId, String stakeHolder, JSONArray storeableNumbers) {
        int arrayLength = storeableNumbers.length();

        try {
            for (int i = 0; i < arrayLength; i++) {
                JSONObject storeableNumber = storeableNumbers.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID, farmerId);
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID, stakeHolder);
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID, storeableNumber.getLong("country_id"));
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY, storeableNumber.getBoolean("primary_number"));
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER, storeableNumber.getString("phonenumber"));
                values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DUPLICATE, storeableNumber.optString(OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DUPLICATE, null));
                db.insert(OFISDBContract.PhoneNumber.TABLE_NAME, null, values);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean addFarmerNationalids(Context context, Long farmerId, String stakeholderId, JSONArray storeableNationalids, String status, String farmername, String columnName) {
        int arrayLength = storeableNationalids.length();
        if (status.equalsIgnoreCase("2") || status.equals("updateNationalId")) {
            deleteExistingNationalIdFromNq(context, farmerId);
        }
        boolean moderationstatus = false;
        try {
            for (int i = 0; i < arrayLength; i++) {
                JSONObject storeableNumber = storeableNationalids.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID, farmerId);
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID, stakeholderId);
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, false);
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE, storeableNumber.getInt("nationalIdType"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER, storeableNumber.getString("nationalIdNumber"));
                if (storeableNumber.has("expiryDate")) {
                    values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_EXPIRY_DATE, storeableNumber.getString("expiryDate"));
                }
                if (storeableNumber.has("photo")) {
                    values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO, storeableNumber.getString("photo"));
                }
                if (isUnderModeration(storeableNumber.getString("nationalIdNumber"), String.valueOf(storeableNumber.getInt("nationalIdType")))) {
                    moderationstatus = true;
                }
                db.insert(OFISDBContract.FarmerNationalIDs.TABLE_NAME, null, values);

                if (status.equals("updateNationalId")) {
                    ContentValues values1 = new ContentValues();
                    if (storeableNumber.has("photo")) {
                        values1.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO, storeableNumber.getString("photo"));
                    }
                    moderationstatus = false;
                    String whereClause = columnName + " = ? AND " +
                            OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ?";


                    String[] whereArgs;

                    if (Objects.equals(columnName, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID)) {
                        whereArgs = new String[]{String.valueOf(stakeholderId), String.valueOf(storeableNumber.getInt("nationalIdType"))};
                    } else {
                        whereArgs = new String[]{String.valueOf(farmerId), String.valueOf(storeableNumber.getInt("nationalIdType"))};
                    }

                    db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);
                }

                if (status.equalsIgnoreCase("2") || status.equals("updateNationalId")) {
                    updateNetworkQueueNIDEntry(context, farmerId, stakeholderId, storeableNumber.getInt("nationalIdType"), farmername, status);
                }

            }

            if (!Objects.equals(columnName, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID)) {
                updateModerationStatus(farmerId, moderationstatus);
            }

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("DataLayer: Error setting phone number storage values", e));
            e.printStackTrace();
        }
        return moderationstatus;
    }

    public void addFarmerNationalids(JSONArray storeableNationalids, long farmerid, long nationalId) {

        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " +
                OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid), String.valueOf(nationalId)};

        try {
            db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

        int arrayLength = storeableNationalids.length();
        try {
            for (int i = 0; i < arrayLength; i++) {
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER, storeableNationalids.getJSONObject(i).getString("id_number"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID, storeableNationalids.getJSONObject(i).getString("farmer_id"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_EXPIRY_DATE, storeableNationalids.getJSONObject(i).getString("expiry_date"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE, storeableNationalids.getJSONObject(i).getString("id_type"));
                values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, 1);
                db.insert(OFISDBContract.FarmerNationalIDs.TABLE_NAME, null, values);

            }
        }catch (Exception e){}


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
        values.put(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_EXPIRY_DATE, storeableNationalids.getExpiryDate());
        try {
            db.insert(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, values);
        }catch (Exception e){}
    }

    void updateFarmerPhoneNumbers(Long farmerId, String stakeHolderId, JSONArray storeableNumbers, String columnName) {
        String whereClause = columnName + " = ?";
        String[] whereArgs;
        if (Objects.equals(columnName, OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID)){
            whereArgs = new String[]{String.valueOf(stakeHolderId)};
        } else {
            whereArgs = new String[]{String.valueOf(farmerId)};
        }

        try {
            db.delete(OFISDBContract.PhoneNumber.TABLE_NAME, whereClause, whereArgs);
            addFarmerPhoneNumbers(farmerId, stakeHolderId, storeableNumbers);
        }catch (Exception e){}

    }

    public boolean updateFarmerNationalids(Context context, Long id, String stakeholderId, JSONArray storeableNationalids, String status, String farmername, String columnName) {
        String whereClause = columnName + " = ?";
        String[] whereArgs;
        if (Objects.equals(columnName, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID)){
            whereArgs = new String[]{String.valueOf(stakeholderId)};
        } else {
            whereArgs = new String[]{String.valueOf(id)};
        }

        db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
        return addFarmerNationalids(context, id, stakeholderId, storeableNationalids, status, farmername, columnName);
    }

    void updateCertificationsByFarmerId(long farmerId, String certificationIds, String certificationText, String farmerName) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID, certificationIds);
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT, certificationText);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        try {
            db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
            updateNetworkQueueCertificationEntry(farmerId,certificationIds,certificationText,farmerName);
        }catch (Exception e){}

    }

    private long updateNetworkQueueCertificationEntry(long farmerId, String certificationIds, String certificationText, String name) {
        long newEntry;
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "certification");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "certification");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, name.trim());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, " ");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CERTIFICATION_ID, certificationIds);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CERTIFICATION_TEXT, certificationText);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());

        newEntry = insertNetworkQueueEntry(networkQueueValues);

        return newEntry;
    }

    public boolean getSipPolygonById(int id){
        boolean captureSipPolygon = false;
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.SipSubGroupEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    captureSipPolygon = Objects.equals(cursor.getString(cursor.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_CAPTURE_SIP_POLYGON)), "true");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return captureSipPolygon;
    }

    public boolean getIfSipAlreadyRecorded(String id){
        boolean rowExists = false;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    OFISDBContract.SipPlot.TABLE_NAME,
                    null,
                    OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?",
                    new String[]{id},
                    null,
                    null,
                    null
            );

            rowExists = cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return rowExists;
    }

    public String getSipNameByUploadId(String id){
        String sip_name = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.SipPlot.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    sip_name = cursor.getString(cursor.getColumnIndex(OFISDBContract.SipPlot.COLUMN_NAME_SIP_NAME));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return sip_name;
    }

    public int updateSipNameByUploadId(String id, String newName) {
        int rowsUpdated = 0;
        try {
            String whereClause = OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            ContentValues contentValues = new ContentValues();
            contentValues.put(OFISDBContract.SipPlot.COLUMN_NAME_SIP_NAME, newName);
            rowsUpdated = db.update(OFISDBContract.SipPlot.TABLE_NAME, contentValues, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsUpdated;
    }

    public String getGlobalIdByQuestionId(int questionId){
        String metricId = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(questionId)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    metricId = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_METRIC));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return metricId;
    }

    public String getAssignmentByFarmerTypeID(long farmerId , String assignmentTypeColumn ){
        String assignmentField="";
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    assignmentField = farmerCursor.getString(farmerCursor.getColumnIndex(assignmentTypeColumn));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return  assignmentField;
    }

    public String getGlobalIdByAnswerID(int answerId){
        String metricId = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(answerId)};
            cursor = db.query(OFISDBContract.AnswerEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    metricId = cursor.getString(cursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_GLOBAL_ID));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return metricId;
    }

    public String getLocalIdByAnswerID(int answerId){
        String metricId = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(answerId)};
            cursor = db.query(OFISDBContract.AnswerEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    metricId = cursor.getString(cursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_LOCAL_ID));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return metricId;
    }

    public String getCreatedPlotDateByID(int plotId){
        String createdDate = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(plotId)};
            cursor = db.query(OFISDBContract.PlotEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    createdDate = cursor.getString(cursor.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_CREATED_AT));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return createdDate;
    }

    public String getPLotDataFromCategoryId(String id){
        String plotdata="";
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            farmerCursor = db.query(OFISDBContract.SipPlot.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    plotdata = farmerCursor.getString(farmerCursor.getColumnIndex(OFISDBContract.SipPlot.COLUMN_NAME_SIP_PLOT));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return plotdata;
    }

    public Cursor getNIDPhotostatusByType(String type, long farmerid, String columnName) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? AND " +
                columnName + " = ? ";
        String[] whereArgs = new String[]{type, String.valueOf(farmerid)};

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getUpdateNIDPhotoStatusByType(String type, long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? AND " +
                OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " +
                OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO + " != ? ";
        String[] whereArgs = new String[]{type, String.valueOf(farmerId), "null"};

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
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

    public Cursor getSpinnerData(String table, Boolean getByRelationship, String getByColumnName, Integer getByColumnValue, @SuppressWarnings("SameParameterValue") Boolean orderByColumn, String orderByColumnName) {
        if (OFISDBContract.ALL_TABLES.containsKey(table)) {
            List<String> columnList = OFISDBContract.ALL_TABLES.get(table);
            String[] tableColumns = new String[columnList.size()];
            if (OFISDBContract.countryLookUpValuesEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())) {
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("country_id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
                }
            } else if (OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())) {
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("productId")) ? columnList.get(i) + " AS _id" : columnList.get(i);
//                    tableColumns[i] = columnList.get(i);
                }
            } else {
                for (int i = 0; i < columnList.size(); i++) {
                    tableColumns[i] = (columnList.get(i).equals("id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
                }
            }
            String type = Utilities.type;
            String countryId = Utilities.countryId;
            String productId =Utilities.productId;
            String querySelection = (getByRelationship) ? getByColumnName + " = ?" : null;
            String[] querySelectionValue = (getByRelationship) ? new String[]{String.valueOf(getByColumnValue)} : null;
            try {
                if (type.equalsIgnoreCase("CHILD_PRODUCT") &&  datatypeIdCLMRS != 6 &&
                        table.equalsIgnoreCase(OFISDBContract.LookupValuesEntry.TABLE_NAME)) {
                    String value = "%\"country_id\":" +
                            Long.parseLong(countryId) +
                            ",\"product_id\":" +
                            Long.parseLong(productId) + "%";
                    querySelection = (getByRelationship) ? getByColumnName + " = ? AND "
                            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_COUNTRYLOOKUPVALUE + " LIKE ? " : null;
                    querySelectionValue = (getByRelationship) ? new String[]{String.valueOf(getByColumnValue), value} : null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            String orderByValue = (orderByColumn) ? orderByColumnName + " " + "ASC" : null;
            if (OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(table.trim().toLowerCase())) {
                orderByValue = OFISDBContract.productSipEntry.COLUMN_NAME_PRODUCT_NAME + " " + "ASC";
            } else {
                orderByValue = (orderByColumn) ? orderByColumnName + " " + "ASC" : null;
            }
            return db.query(table, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
        }

        return null;
    }

    public Cursor getFGSpinnerData(ArrayList<Integer> fgList) {
        String result = null;
        if (fgList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int s : fgList) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        List<String> columnList = OFISDBContract.ALL_TABLES.get(OFISDBContract.FarmerGroupsEntry.TABLE_NAME);
        String[] tableColumns = new String[columnList.size()];
        for (int i = 0; i < columnList.size(); i++) {
            tableColumns[i] = (columnList.get(i).equals("id")) ? columnList.get(i) + " AS _id" : columnList.get(i);
        }
        StringBuilder sb1 = new StringBuilder();
        for (String s : tableColumns) {
            sb1.append(s).append(",");
        }
        String columns = sb1.deleteCharAt(sb1.length() - 1).toString();
        Cursor cursor=null;
        try {
            cursor = db.rawQuery("SELECT "+ columns +" FROM " + OFISDBContract.FarmerGroupsEntry.TABLE_NAME +
                    " WHERE " + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID
                    + " IN (" + result + ")"  ,null);
        }catch (Exception e){
            e.printStackTrace();
        }

        return cursor;
    }

    public void bindDataDrivenSpinner(Spinner spinner, Cursor cursor, Context context, int placeholderTextResId, String textColumnName, boolean showAsWhite) {
        try {
            if (textColumnName == null) {
                textColumnName = "name";
            }
            String[] adapterCols = new String[]{textColumnName, "_id", "code", "dialling_code"};
            MatrixCursor mc = new MatrixCursor(adapterCols);
            mc.addRow(new String[]{res.getString(placeholderTextResId), "0", "", ""});
            if (spinner.getId() == R.id.spinner_place) {
                mc.addRow(new String[]{res.getString(R.string.new_place_name_placeholder), "-1", "", ""});

            }
            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);

            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v = super.getDropDownView(position, convertView, parent);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
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

    void bindDataDrivenSpinner1(Spinner spinner, Cursor cursor, Context context, int placeholderTextResId, String textColumnName, boolean showAsWhite) {
        try {
            if (textColumnName == null) {
                textColumnName = "name";
            }
            String[] adapterCols = new String[]{textColumnName, "_id", "code", "dialling_code"};
            MatrixCursor mc = new MatrixCursor(adapterCols);
            mc.addRow(new String[]{res.getString(placeholderTextResId), "0", "", ""});
            if (spinner.getId() == R.id.spinner_place) {
                mc.addRow(new String[]{res.getString(R.string.new_place_name_placeholder), "-1", "", ""});

            }
            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);

            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item_white;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v = super.getDropDownView(position, convertView, parent);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
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

            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);

            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v = super.getDropDownView(position, convertView, parent);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
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

    void bindDataDrivenSpinnerSIP1(Spinner spinner, Cursor cursor, Context context, int placeholderTextResId, String textColumnName, boolean showAsWhite) {
        try {
            if (textColumnName == null) {
                textColumnName = "name";
            }
            String[] adapterCols = new String[]{textColumnName, "_id", "code", "dialling_code"};
            MatrixCursor mc = new MatrixCursor(adapterCols);
            mc.addRow(new String[]{res.getString(placeholderTextResId), "0", "", ""});
            Cursor[] cursors = {mc, cursor};
            Cursor output = new MergeCursor(cursors);
            int[] adapterRowViews = new int[]{R.id.ofis_spinner_text_view};
            int spinnerItemLayout = (showAsWhite) ? R.layout.spinner_item_white : R.layout.spinner_item_white;
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, spinnerItemLayout, output, adapterCols, adapterRowViews, 0) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    final View v = super.getDropDownView(position, convertView, parent);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) v.findViewById(R.id.ofis_spinner_text_view)).setSingleLine(false);
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

    void createGeographyHierarchySpinners(final AppCompatActivity activity, final Context context, final EditText farmerIdPrefix, int countrySelection, final int regionSelection, final int districtSelection, final int subdistrictSelection, final int placeSelection, final LinearLayout phonenumbers, final boolean newPlace, int item) {
        final Spinner countrySpinner = (Spinner) activity.findViewById(R.id.spinner_country);
        final Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_region);
        final Spinner districtSpinner = (Spinner) activity.findViewById(R.id.spinner_district);
        final Spinner subdistrictSpinner = (Spinner) activity.findViewById(R.id.spinner_subdistrict);
        final Spinner placeSpinner = (Spinner) activity.findViewById(R.id.spinner_place);
        final ImageView haDistrict = (ImageView) activity.findViewById(R.id.haDistrict);


        final EditText newplace = (EditText) activity.findViewById(R.id.newPlaceName);

        Cursor countries = getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        bindDataDrivenSpinner(countrySpinner, countries, context, R.string.spinner_country_default, null, false);

        if(item == 3) {
            countrySpinner.setEnabled(false);
        }

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
                if (id > 0 && item == 1) {
                    LinearLayout farmerLeadDisplayLayout = activity.findViewById(R.id.farmerLeadDisplayLayout);
                    boolean isODOrigin = isODOrigin((int) id);
                    boolean isFLEnumerator = Utilities.getBoolFromSharedPrefs(context, Utilities.FARMERLEAD_ENUMERATOR,false);
                    if (!isODOrigin) {
                        farmerLeadDisplayLayout.setVisibility(View.GONE);
                    } else {
                        if(isFLEnumerator) {
                            farmerLeadDisplayLayout.setVisibility(View.VISIBLE);
                        }else {
                            farmerLeadDisplayLayout.setVisibility(View.GONE);
                        }
                    }
                }

                if (id > 0) {
                    boolean isODOrigin = isSubdistrictCapture((int) id);
                    if (!isODOrigin) {
                        subdistrictSpinner.setVisibility(View.GONE);
                        haDistrict.setVisibility(View.GONE);
                    } else {
                        subdistrictSpinner.setVisibility(View.VISIBLE);

                        String environment=BuildConfig.FLAVOR;
                        if (environment.equalsIgnoreCase("devoga") || environment.equalsIgnoreCase("sitoga") ||
                                environment.equalsIgnoreCase("uatoga") || environment.equalsIgnoreCase("prodoga") ||
                                environment.equalsIgnoreCase("devofi") || environment.equalsIgnoreCase("sitofi") ||
                                environment.equalsIgnoreCase("uatofi") || environment.equalsIgnoreCase("prodofi") || environment.equalsIgnoreCase("preprodofi")) {

                            haDistrict.setVisibility(View.INVISIBLE);
                        }else {

                            haDistrict.setVisibility(View.VISIBLE);
                        }
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
                if(item == 3){
                    regionSpinner.setEnabled(false);
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
                if(item == 3){
                    districtSpinner.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(item == 3){
            regionSpinner.setEnabled(false);
            districtSpinner.setEnabled(false);
        }
        if (placeSpinner != null && subdistrictSpinner != null) {
            districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                    try {
                        Cursor subdistricts = getSpinnerData(OFISDBContract.SubDistrictsEntry.TABLE_NAME, true, OFISDBContract.SubDistrictsEntry.COLUMN_NAME_DISTRICT_ID, ((int) id), true, OFISDBContract.SubDistrictsEntry.COLUMN_NAME_SUBDISTRICT_NAME);
                        bindDataDrivenSpinner(subdistrictSpinner, subdistricts, context, R.string.spinner_subdistrict_default, null, false);

                        if (subdistrictSelection != 0) {
                            setSpinnerSelectionById(subdistrictSelection, subdistrictSpinner);
                            if (subdistricts.getCount() == 1) {
                                subdistrictSpinner.setSelection(1, false);
                                subdistrictSpinner.setEnabled(false);
                            } else {
                                subdistrictSpinner.setEnabled(true);
                            }
                        } else {
                            if (subdistricts.getCount() == 1) {
                                subdistrictSpinner.setSelection(1, false);
                                subdistrictSpinner.setEnabled(false);
                            } else {
                                subdistrictSpinner.setEnabled(true);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    boolean isODOrigin = isSubdistrictCapture((int) id);
                    Cursor places;
                    if (!isODOrigin) {
                        places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    }else {
                        places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    }
                    if (!newPlace) {
                        bindDataDrivenSpinnerSIP(placeSpinner, places, context, R.string.spinner_place_default, null, false);
                    } else {
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


            subdistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                    Cursor places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    if (!newPlace) {
                        bindDataDrivenSpinnerSIP(placeSpinner, places, context, R.string.spinner_place_default, null, false);
                    } else {
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

    void createGeographyHierarchySpinners1(final AppCompatActivity activity, final Context context, final EditText farmerIdPrefix, int countrySelection, final int regionSelection, final int districtSelection, final int subdistrictSelection, final int placeSelection, final LinearLayout phonenumbers, final boolean newPlace, int item) {
        final Spinner countrySpinner = (Spinner) activity.findViewById(R.id.spinner_country1);
        final Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_region1);
        final Spinner districtSpinner = (Spinner) activity.findViewById(R.id.spinner_district1);
        final Spinner subdistrictSpinner = (Spinner) activity.findViewById(R.id.spinner_subdistrict1);
        final Spinner placeSpinner = (Spinner) activity.findViewById(R.id.spinner_place1);
        final ImageView haDistrict = (ImageView) activity.findViewById(R.id.haDistrict1);

        Cursor countries = getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        bindDataDrivenSpinner1(countrySpinner, countries, context, R.string.spinner_country_default, null, false);
        if(item == 3){
            countrySpinner.setEnabled(false);
        }

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                Cursor regions = getSpinnerData(OFISDBContract.RegionsEntry.TABLE_NAME, true, OFISDBContract.RegionsEntry.COLUMN_NAME_COUNTRY_ID, ((int) id), true, OFISDBContract.RegionsEntry.COLUMN_NAME_REGION_NAME);
                bindDataDrivenSpinner1(regionSpinner, regions, context, R.string.spinner_region_default, null, false);
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
                if (id > 0 && item == 1) {
                    LinearLayout farmerLeadDisplayLayout = activity.findViewById(R.id.farmerLeadDisplayLayout);
                    boolean isODOrigin = isODOrigin((int) id);
                    boolean isFLEnumerator = Utilities.getBoolFromSharedPrefs(context, Utilities.FARMERLEAD_ENUMERATOR,false);
                    if (!isODOrigin) {
                        farmerLeadDisplayLayout.setVisibility(View.GONE);
                    } else {
                        if(isFLEnumerator) {
                            farmerLeadDisplayLayout.setVisibility(View.VISIBLE);
                        }else {
                            farmerLeadDisplayLayout.setVisibility(View.GONE);
                        }
                    }
                }

                if (id > 0) {
                    boolean isODOrigin = isSubdistrictCapture((int) id);
                    if (!isODOrigin) {
                        subdistrictSpinner.setVisibility(View.GONE);
                        haDistrict.setVisibility(View.GONE);
                    } else {
                        subdistrictSpinner.setVisibility(View.VISIBLE);
                        String environment=BuildConfig.FLAVOR;
                        if (environment.equalsIgnoreCase("devoga") || environment.equalsIgnoreCase("sitoga") ||
                                environment.equalsIgnoreCase("uatoga") || environment.equalsIgnoreCase("prodoga") ||
                                environment.equalsIgnoreCase("devofi") || environment.equalsIgnoreCase("sitofi") ||
                                environment.equalsIgnoreCase("uatofi") || environment.equalsIgnoreCase("prodofi") || environment.equalsIgnoreCase("preprodofi")) {

                            haDistrict.setVisibility(View.INVISIBLE);
                        }else {
                            haDistrict.setVisibility(View.VISIBLE);
                        }
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
                if(item == 3){
                    regionSpinner.setEnabled(false);
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
                bindDataDrivenSpinner1(districtSpinner, districts, context, R.string.spinner_district_default, null, false);

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
                if(item == 3){
                    districtSpinner.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(item == 3){
            regionSpinner.setEnabled(false);
            districtSpinner.setEnabled(false);
        }
        if (placeSpinner != null && subdistrictSpinner != null) {
            districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                    try {
                        Cursor subdistricts = getSpinnerData(OFISDBContract.SubDistrictsEntry.TABLE_NAME, true, OFISDBContract.SubDistrictsEntry.COLUMN_NAME_DISTRICT_ID, ((int) id), true, OFISDBContract.SubDistrictsEntry.COLUMN_NAME_SUBDISTRICT_NAME);
                        bindDataDrivenSpinner1(subdistrictSpinner, subdistricts, context, R.string.spinner_subdistrict_default, null, false);

                        if (subdistrictSelection != 0) {
                            setSpinnerSelectionById(subdistrictSelection, subdistrictSpinner);
                            if (subdistricts.getCount() == 1) {
                                subdistrictSpinner.setSelection(1, false);
                                subdistrictSpinner.setEnabled(false);
                            } else {
                                subdistrictSpinner.setEnabled(true);
                            }
                        } else {
                            if (subdistricts.getCount() == 1) {
                                subdistrictSpinner.setSelection(1, false);
                                subdistrictSpinner.setEnabled(false);
                            } else {
                                subdistrictSpinner.setEnabled(true);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    boolean isODOrigin = isSubdistrictCapture((int) id);
                    Cursor places;
                    if (!isODOrigin) {
                        places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    }else {
                        places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    }
                    if (!newPlace) {
                        bindDataDrivenSpinnerSIP1(placeSpinner, places, context, R.string.Name, null, false);
                    } else {
                        bindDataDrivenSpinner1(placeSpinner, places, context, R.string.Name, null, false);
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


            subdistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                    Cursor places = getSpinnerData(OFISDBContract.PlacesEntry.TABLE_NAME, true, OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID, ((int) id), true, OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME);
                    if (!newPlace) {
                        bindDataDrivenSpinnerSIP1(placeSpinner, places, context,  R.string.Name, null, false);
                    } else {
                        bindDataDrivenSpinner1(placeSpinner, places, context,  R.string.Name, null, false);
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
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG + ", " +
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
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + " LIKE '%" + query + "%' OR " +
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
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG + ", " +
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

    public Cursor getFarmerById(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getNIDByName(String type) {

        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " = ? ";
        String language = Locale.getDefault().getLanguage();
        if (language.equalsIgnoreCase(Utilities.FR)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.ES)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.PT)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.ID)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.TR)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.LO)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.VI)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.TH)) {
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
        if (language.equalsIgnoreCase(Utilities.FR)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.ES)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.PT)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.ID)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.TR)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.LO)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.VI)) {
            whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " = ? ";
        } else if (language.equalsIgnoreCase(Utilities.TH)) {
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
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER + " = ? AND " + OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? ";
        String[] whereArgs = new String[]{nationalid, type};

        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getAllPhotos() {
        String[] columns = new String[]{"*"};
        return db.query(OFISDBContract.LocalNationalIDImages.TABLE_NAME, columns, null, null, null, null, null);
    }

    public int getFarmId(int farmer) {
        int FarmID = 0;
        try {
            Cursor farmCursor = db.rawQuery("Select farm_id from farmers where id =" + farmer, null);
            if (farmCursor.getCount() > 0) {
                farmCursor.moveToNext();
                FarmID = farmCursor.getInt(farmCursor.getColumnIndex("farm_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return FarmID;
    }

    public Cursor getFlattenedFarmer(long id) {
        String farmerQuery = "SELECT farmers.*, districts.id as district_id, regions.id as region_id, countries.id as country_id, farmergroups.id as farmergroup_id, farmergroups.programme_id as programme_id, farmergroups.product_id as product_id, farmergroups.partner_id as partner_id " +
                "FROM farmers " +
                "INNER JOIN places on farmers.place_id = places.id " +
//                "INNER JOIN subdistricts on farmers.subdistrict_id = subdistricts.id " +
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
        Cursor farmerModules = null;

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
                        "(farmer_child_modules.child_id = ?)" +
                        " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_child_modules.current_submodule_id = submodules.id WHERE " +
                        "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                        "((moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?))" + " group by _id";

                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        child_id,
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("product_id")),
                        farmer.getString(farmer.getColumnIndex("region_id")),
                        farmer.getString(farmer.getColumnIndex("district_id")),
                        farmer.getString(farmer.getColumnIndex("place_id")),
                        farmer.getString(farmer.getColumnIndex("farmergroup_id")),
                        farmer.getString(farmer.getColumnIndex("section_id")),
                        farmer.getString(farmer.getColumnIndex("programme_id")),
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
        } finally {
            farmer.close();
        }
    }

    public Cursor getFarmerModules(long id, boolean farmSurvey) {
        Cursor farmer = null;

        try {
            farmer = getFlattenedFarmer(id);
            Cursor output = null;

            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                String modulesQuery="";
                if (farmSurvey){
                    modulesQuery = "SELECT modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                            "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                            "modules.expires, " + "moduleassignments.recurrence_frequency,moduleassignments.startDate, moduleassignments.recurrence_type, moduleassignments.disabled_months,moduleassignments.isFarmSurvey,moduleassignments.isPlotSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId, " +
                            " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration FROM moduleassignments " +
                            " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                            " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                            "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                            " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                            "moduleassignments.isPlotSurvey == 'true' AND " + "moduleassignments.childLabour != 'true' AND " + "modules.surveyType != '8' AND " +
                            "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                            "((moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) GROUP BY modules.id";
                }else {
                    modulesQuery = "SELECT modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                            "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                            "modules.expires, " + "moduleassignments.recurrence_frequency,moduleassignments.startDate, moduleassignments.recurrence_type, moduleassignments.disabled_months,moduleassignments.isFarmSurvey,moduleassignments.isPlotSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId, " +
                            " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration FROM moduleassignments " +
                            " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                            " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                            "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                            " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                            "moduleassignments.childLabour != 'true' AND " +
                            "moduleassignments.isPlotSurvey == 'true' AND "+
                            "moduleassignments.isFarmSurvey == 'false' AND " +
                            "modules.surveyType != '8' AND " +
                            "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                            "((moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) GROUP BY modules.id";
                }
                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("product_id")),
                        farmer.getString(farmer.getColumnIndex("region_id")),
                        farmer.getString(farmer.getColumnIndex("district_id")),
                        farmer.getString(farmer.getColumnIndex("place_id")),
                        farmer.getString(farmer.getColumnIndex("farmergroup_id")),
                        farmer.getString(farmer.getColumnIndex("section_id")),
                        farmer.getString(farmer.getColumnIndex("programme_id")),
                        farmer.getString(farmer.getColumnIndex("partner_id"))
                };
//                @SuppressLint("Recycle") Cursor farmerModules = db.rawQuery(modulesQuery, modulesQueryValues);
                @SuppressLint("Recycle") Cursor farmerModules = getfarmerModules(modulesQuery, modulesQueryValues);

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
                        "isFarmSurvey",
                        "isPlotSurvey",
                        "view_module",
                        "fkFarmerRoleId",
                        "farmer_module_id",
                        "status_started",
                        "status_finished",
                        "current_submodule_id",
                        "recorded_lat",
                        "recorded_lng",
                        "surveyDuration"
                };
                MatrixCursor mc = new MatrixCursor(mcColumns);
                Cursor[] cursors = {mc, farmerModules};
                output = new MergeCursor(cursors);
            }
            return output;
        } finally {
            farmer.close();
        }
    }

    // modules tasks
    Cursor getLookupValuesForQuestion(int questionId, String questionColumn, int dataypeId) {
        String type = Utilities.type;
        String productId = Utilities.productId;
        String countryId = Utilities.countryId;
        String querySelection = OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] querySelectionValue = new String[]{String.valueOf(questionId)};
        try {
            if (type.equalsIgnoreCase("CHILD_PRODUCT") && dataypeId != 6) {
                String value = "%\"country_id\":" +
                        Long.parseLong(countryId) +
                        ",\"product_id\":" +
                        Long.parseLong(productId) + "%";
                querySelection = OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " = ? AND "
                        + OFISDBContract.LookupValuesEntry.COLUMN_NAME_COUNTRYLOOKUPVALUE + " LIKE ? ";
                querySelectionValue = new String[]{String.valueOf(questionId), value};
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String[] tableColumns = new String[]{questionColumn, "id as _id", OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION};


        String orderByValue = OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION + " ASC";

        return db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME, tableColumns, querySelection, querySelectionValue, null, null, orderByValue);
    }

    //check for lookupvalue for the question id
    boolean getLookupValuesForQuestion(int questionId) {
        String querySelection = OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] querySelectionValue = new String[]{String.valueOf(questionId)};
        int count = db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME, null, querySelection, querySelectionValue, null, null, null).getCount();
        if (count > 0) {
            return true;
        } else
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

    public void updateSyncedStatus(long farmerid, long nIdType) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, true);
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " +
                OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid),String.valueOf(nIdType)};
        try {
            db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}

    }

    public void deleteSipPlotItem(String id) {
        String whereClause = OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        try {
            db.delete(OFISDBContract.SipPlot.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
    }

    public void updateSyncedStatusFixed(long farmerid, long updatedFarmerId) {
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, true);
            values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID, updatedFarmerId);
            String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID + " IS NULL";
            String[] whereArgs = new String[]{String.valueOf(farmerid)};
            db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);

            ContentValues values2 = new ContentValues();
            values2.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID, updatedFarmerId);

            String whereClause2 = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID + " IS NOT NULL";
            String[] whereArgs2 = new String[]{String.valueOf(farmerid)};

            db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values2, whereClause2, whereArgs2);


        } catch (Exception e) {
        }

    }

    public void updateSyncedStatusFixedMs(long id, long updatedStakeholderId) {
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED, true);
            values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID, updatedStakeholderId);
            String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);

        } catch (Exception e) {
        }

    }

    public void updateSyncedStatusPlot(long farmerid, long updatedFarmerId) {
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID, updatedFarmerId);
            String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerid)};
            db.update(OFISDBContract.PlotEntry.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
        }

    }

    public void updateModerationStatus(Long farmerid, boolean moderationstatus) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_MODERATION_STATUS, moderationstatus);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        try {
            db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}

    }

    public void updateConsentStatus(long farmerid) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE, "true");
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        try {
            db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
    }

    public void updateSubdistrict(int farmerid, ContentValues values, String olamfarmername) {
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        try {
            db.update(OFISDBContract.FarmerEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            deleteNetworkQueueItemByFarmerId(farmerid, "updatesubdistrict");
        }catch (Exception e){
            e.printStackTrace();
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "updatesubdistrict");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, "Farmer subdistrict captured");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, olamfarmername);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBDISTRICT_ID, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID, values.getAsString(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerid);
//        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, farmerid);

        insertNetworkQueueEntry(networkQueueValues);
    }

    public Cursor getnationalidsbyfarmerids(long id, String columnName) {
        String[] columns = new String[]{"*"};
        String whereClause = columnName + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getnationalidsbyfarmerid(long farmerid, long nidtype, String columnName) {
        String[] columns = new String[]{"*"};
        String whereClause = columnName + " = ? AND " +
                OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid),String.valueOf(nidtype)};
        String orderBy = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getUpdateAvailableNationalIdsByFarmerId(long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " +
                    OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId), "null"};
        String orderBy = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getassignmentbyCountryId(int countryid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerAssignments.COLUMN_COUNTRY_iD + " = ?";
        String[] whereArgs = new String[]{String.valueOf(countryid)};
        String orderBy = OFISDBContract.FarmerAssignments.COLUMN_COUNTRY_iD + " ASC";

        return db.query(OFISDBContract.FarmerAssignments.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getFLbycountryId(int farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerLeadDetails.COLUMN_COUNTRY_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.FarmerLeadDetails.COLUMN_FL_ID + " ASC";

        return db.query(OFISDBContract.FarmerLeadDetails.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getcountrynameid(long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.CountriesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getregionnameid(long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.RegionsEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.RegionsEntry.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.RegionsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getdistrictnameid(long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.DistrictsEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.DistrictsEntry.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.DistrictsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getsubdistrictnameid(long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SubDistrictsEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.SubDistrictsEntry.COLUMN_NAME_ID + " ASC";

        return db.query(OFISDBContract.SubDistrictsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public boolean getSyncedStatus(long farmerId) {
        boolean status = true;
        Cursor nids = getnationalidsbyfarmerids(farmerId, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);
        try {
            while (nids.moveToNext()) {
                String isSynced = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED));
                if (isSynced.equalsIgnoreCase("0")) {
                    status = false;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return status;
    }

    public boolean isODOrigin(int countryid) {
        boolean status = false;
        Cursor nids = getassignmentbyCountryId(countryid);
        try {
            while (nids.moveToNext()) {
                String isODorigin = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerAssignments.COLUMN_ISODORIGIN));
                if (isODorigin.equalsIgnoreCase("true")) {
                    status = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return status;
    }

    public boolean isSubdistrictCapture(int countryid) {
        if(countryid == 0){
            return  false;
        }
        boolean status = false;
        Cursor nids = getcountrynameid(countryid);
        try {
            while (nids.moveToNext()) {
//                commenting OFIS OD Logic
                String isSubDistCapture = nids.getString(nids.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_ISSUBDISTCAPTURE));
                //              String isSubDistCapture = "false";
                if (isSubDistCapture.equalsIgnoreCase("true")) {
                    status = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return status;
    }

    public List<FarmerLeadDTO> getFarmerLeads(int countryId) {

        List<FarmerLeadDTO> list = new ArrayList<FarmerLeadDTO>();
//        Cursor nids = db.rawQuery("SELECT * FROM " + OFISDBContract.FarmerLeadDetails.TABLE_NAME, null);;
        Cursor nids = getFLbycountryId(countryId);
        try {
            while (nids.moveToNext()) {
                FarmerLeadDTO leadDTO = new FarmerLeadDTO();
                leadDTO.setFarmerLeadFullName(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerLeadDetails.COLUMN_FL_FULLNAME)));
                leadDTO.setFarmerLeadDisplayId(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerLeadDetails.COLUMN_FL_DISPLAYID)));
                leadDTO.setContactNumber(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerLeadDetails.COLUMN_FL_NUMBER)));
                leadDTO.setFarmerLeadId(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerLeadDetails.COLUMN_FL_ID)));
                list.add(leadDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return list;
    }

    public String getRegionName(int id) {
        String name = "NA";
        Cursor nids = getregionnameid(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.RegionsEntry.COLUMN_NAME_REGION_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public String getDistrictName(int id) {
        String name = "NA";
        Cursor nids = getdistrictnameid(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.DistrictsEntry.COLUMN_NAME_DISTRICT_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public String getSubDistrictName(int id) {
        String name = "NA";
        Cursor nids = getsubdistrictnameid(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.SubDistrictsEntry.COLUMN_NAME_SUBDISTRICT_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public String getPlaceName(int id) {
        String name = "NA";
        Cursor nids = getPlaceById(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public String getFGName(int id) {
        String name = "NA";
        Cursor nids = getFGById(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public String getSectionName(int id) {
        String name = "NA";
        Cursor nids = getSectionById(id);
        try {
            while (nids.moveToNext()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }
    
    public boolean isMultipleCountries() {
        boolean status = false;
        Cursor ids = null;

        try {
            ids = db.query(OFISDBContract.CountriesEntry.TABLE_NAME, new String[]{"*"}, null, null, null, null, null);

            if (ids.getCount() > 1) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ids.close();
        }

        return status;
    }


    public boolean isValueavailable(long farmerId) {
        boolean status = false;
        Cursor nids = getnationalidsbyfarmerids(farmerId, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);

        try {
            if (nids.getCount() > 0) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return status;
    }

    public boolean isUpdateNationalIdAvailable(long farmerId) {
        boolean isAvailable = false;
        Cursor availableNationIds = getUpdateAvailableNationalIdsByFarmerId(farmerId);
        try {
            isAvailable = availableNationIds != null && availableNationIds.getCount() != 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            availableNationIds.close();
        }
        return isAvailable;
    }

    public boolean isCertificationavailable(long farmerId) {
        boolean status = false;
        Cursor nids = getCertificationsbyfarmerid(farmerId);

        try {
            if (nids.getCount() > 0 ) {
                if(nids.moveToFirst()) {
                    String id = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID));
                    String text = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT));
                    if(id!=null || text!=null){
                        if(!id.equals("null") || !text.equals("null")){
                            status = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return status;
    }

    public boolean isSignatureCaptured(long farmerId) {
        boolean status = false;
        Cursor farmerdata = getFarmerById(farmerId);

        try {
            if (farmerdata.getCount() > 0) {
                farmerdata.moveToFirst();
                String sign = farmerdata.getString(farmerdata.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
                if (!TextUtils.isEmpty(sign)) {
                    status = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            farmerdata.close();
        }

        return status;
    }

    public boolean isUnderModeration(String nationalid, String type) {
        boolean status = false;
        Cursor nids = getModerationStatusbyNID(nationalid, type);

        try {
            if (nids.getCount() > 0) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

    //OFS-44
    Cursor getAllQuestionIds(int moduleId) {
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String submoduleTable = OFISDBContract.SubmoduleEntry.TABLE_NAME;
        String moduleTable = OFISDBContract.ModuleEntry.TABLE_NAME;
        String whereClause = moduleTable + "." + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(moduleId)
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
        String sql = "SELECT " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " FROM " + questionTable + " INNER JOIN " + submoduleTable
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
        String columnPosition = OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION;

        String sortOrder = "CASE " +
                "WHEN " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density dropdown' THEN " +
                columnPosition + " " +
                "WHEN " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density' THEN " +
                columnPosition + " + 2 " +
                "ELSE " +
                "CASE WHEN " + columnPosition + " > " +
                "(SELECT MAX(" + columnPosition + ") " +
                "FROM " + OFISDBContract.QuestionEntry.TABLE_NAME + " " +
                "WHERE " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density dropdown')" +
                " THEN " + columnPosition + " + 2 " +
                " ELSE " + columnPosition + " + 2 " +
                "END END";
        String sql = "SELECT " + questionTable + ".*, " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name FROM " + questionTable
                + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + whereClause + " ORDER BY " + sortOrder;

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

    public Cursor getQuestionsAndAnswersBySubModuleId(int submoduleId, long farmerId, long farmerModuleId, String stakeholderId) {
        Log.d(TAG, "getQuestionsAndAnswersBySubModuleId");
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String columnPosition = OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION;

        // code for only making the farm tree density drop down as priority
        /*String sortOrder = "CASE " +
                "WHEN "+ OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density dropdown' THEN 1 ELSE CASE WHEN " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density' THEN 2 " +
                "ELSE " + orderBy + " + 2 " +
                "END END";*/

        String sortOrder = "CASE " +
                "WHEN " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density dropdown' THEN " +
                columnPosition + " " +
                "WHEN " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density' THEN " +
                columnPosition + " + 2 " +
                "ELSE " +
                "CASE WHEN " + columnPosition + " > " +
                "(SELECT MAX(" + columnPosition + ") " +
                "FROM " + OFISDBContract.QuestionEntry.TABLE_NAME + " " +
                "WHERE " + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " = 'farm tree density dropdown')" +
                " THEN " + columnPosition + " + 2 " +
                " ELSE " + columnPosition + " + 2 " +
                "END END";

        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY " + sortOrder;

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    farmerQueryValue,
                    String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId)
            };
        } else {
            whereArgs = new String[]{
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId)
            };
        }


        return db.rawQuery(sql, whereArgs);
    }

    Cursor getQuestionsAndAnswersBySubModuleId1(int submoduleId, long farmerId, String stakeholderId, long farmerModuleId, int question_id) {
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

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?  GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    farmerQueryValue,
                    String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId),
                    String.valueOf(question_id)
            };
        } else {
            whereArgs = new String[]{
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId),
                    String.valueOf(question_id)
            };
        }

        return db.rawQuery(sql, whereArgs);
    }

    Cursor getAnswersForQuestionByFarmer(int questionId, long farmerId, String stakeholderId, long farmerModuleId) {

        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery + " AND "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue ,
                    String.valueOf(farmerModuleId)
            };
        } else {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId)
            };
        }


        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getAnswersForQuestionByFarmerRepeaterCheckbox(int questionId, long farmerId, String stakeholderId, long farmerModuleId, int repeaterItemPosition) {
        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery + " AND "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " = ?";

        String[] whereArgs;
        if (stakeholderId == null){
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue,
                    String.valueOf(farmerModuleId),
                    String.valueOf(repeaterItemPosition)
            };
        } else {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId),
                    String.valueOf(repeaterItemPosition)
            };
        }


        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    Cursor getAnswersForQuestionByFarmer(int questionId, long farmerId) {
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? " ;
        String farmerQueryValue = String.valueOf(farmerId);
        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery;
        String[] whereArgs = new String[]{
                String.valueOf(questionId),
                farmerQueryValue
        };

        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, new String[]{"*"}, whereClause, whereArgs, null, null, null);
    }

    Cursor getSavedAnswersForQuestionByFarmer(int questionId, long farmerId, String stakeholderId, long farmerModuleId) {
        String[] columns = new String[]{"*"};
        String farmerQuery = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID + " = ? " ;
        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery;
//                + " AND " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{
                String.valueOf(questionId),
                farmerQueryValue
        };

        return db.query(OFISDBContract.SavedAnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    // get the child answers from dropdown
    Cursor getAnswersForQuestionByChild(int questionId, long farmerId, String stakeholderId, long farmerModuleId) {
        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + farmerQuery + " AND "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue,
                    String.valueOf(farmerModuleId)
            };
        } else {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId)
            };
        }

        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    public long insertFarmerModule(long moduleId, long farmerId, String stakeholderId, String olamFarmerId, int farmId, int subModuleId, int statusStarted, String recordedLat, String recordedLng, int plotId) {
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
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_PLOT_ID, plotId);
        farmerModuleValues.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeholderId);
        long rowId=0;
        try {
            rowId=db.insert(OFISDBContract.FarmerModulesEntry.TABLE_NAME, null, farmerModuleValues);
        }catch (Exception e){}
        return  rowId;
    }

    public long insertFarmerChildModule(long moduleId, long farmerId, String olamFarmerId, int farmId, int subModuleId, int statusStarted, String recordedLat, String recordedLng, String childId) {
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
        farmerChildModuleValues.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CHILD_ID, childId);
        long rowId=0;
        try {
            rowId=db.insert(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, null, farmerChildModuleValues);
        }catch (Exception e){}
        return  rowId;
    }

    Cursor getRepeaterItemsForQuestion(long id) {
        Log.d(TAG, "getRepeaterItemsForQuestion");
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + ", "
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
        Log.d(TAG, "getRepeaterItemsForQuestion1");
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + ", "
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

    Cursor getRepeaterAnswersForQuestionByPosition(int questionId, int position, long farmerId, String stakeholderId, long farmerModuleId) {
        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;
        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION
                + " = ? AND " + farmerQuery + " AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";

        String[] whereArgs;
        if(stakeholderId == null) {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    String.valueOf(position),
                    farmerQueryValue,
                    String.valueOf(farmerModuleId)
            };
        } else {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    String.valueOf(position),
                    farmerQueryValue, stakeholderId,
                    String.valueOf(farmerModuleId)
            };
        }

        return db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    Cursor getRepeaterSavedAnswersForQuestionByPosition(int questionId, int position, long farmerId, String stakeholderId, long farmerModuleId) {
        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String farmerQueryValue = String.valueOf(farmerId);

        String whereClause = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_QUESTION_ID + " = ? AND " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION
                + " = ? AND " + farmerQuery ;
//                + " AND " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    String.valueOf(position),
                    farmerQueryValue,
//                String.valueOf(farmerModuleId)
            };
        } else {
            whereArgs = new String[]{
                    String.valueOf(questionId),
                    String.valueOf(position),
                    farmerQueryValue, stakeholderId,
//                String.valueOf(farmerModuleId)
            };
        }

        return db.query(OFISDBContract.SavedAnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public void updateSubModuleAnswers(ArrayList<JSONObject> answerSet, long farmerModuleId, int subModuleId, long farmerId, String stakeholderId, String olamFarmerId, int farmId, String type) {
        try {
            // delete existing answers for this farmer and submodule
            String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
            String idColumn = (type.equalsIgnoreCase("fmp")) ? OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID : OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID;
            String farmerQuery = answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ?";

            String farmerQueryValue = String.valueOf(farmerId);
            String[] whereArgs;
            if (stakeholderId != null) {
                farmerQuery += " AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " = ? ";
                whereArgs = new String[] {
                        String.valueOf(subModuleId),
                        farmerQueryValue, stakeholderId,
                        String.valueOf(farmerModuleId)
                };
            } else {
                whereArgs = new String[] {
                        String.valueOf(subModuleId),
                        farmerQueryValue,
                        String.valueOf(farmerModuleId)
                };
            }


            String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND " + farmerQuery + " AND " + idColumn + " = ? ";

            db.delete(answerTable, whereClause, whereArgs);
            long farmerIdLOG = Utilities.farmerId;
            Log.d("AnswerSet" , "Farmer ID: " +  farmerIdLOG + " / Answer Set: " + answerSet.toString());

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
                    Integer globalMetricId = currentAnswer.getInt("globalMetricId");
                    Integer localMetricId = currentAnswer.getInt("localMetricId");
                    double metricAnswer = currentAnswer.getDouble("metricAnswer");

                    ContentValues values = new ContentValues();
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, currentQuestionId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID, parentQuestionId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION, repeaterPosition);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID, subModuleId);
                    values.put(idColumn, farmerModuleId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeholderId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_FARM_ID, farmId);

                    values.put("answer_" + datatype, answer);

                    if (sub_answers.length() > 0 && (!datatype.equalsIgnoreCase("text"))) {
                        values.put("answer_text", sub_answers);
                    }
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, reportingYear);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_GLOBAL_ID, globalMetricId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_LOCAL_ID, localMetricId);
                    values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC, metricAnswer);

                    db.insert(OFISDBContract.AnswerEntry.TABLE_NAME, null, values);

                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error updating submodule answers", e));
            e.printStackTrace();
        }
    }

    public void deleteDatafromGeoPlot(String plotName){
        try {
            String whereClause = OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " = ?";
            String[] whereArgs = new String[]{String.valueOf(plotName)};
            db.delete(OFISDBContract.PlotGeoData.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public long deleteNetworkQueuePlotItemByFarmerId(long farmerId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        return db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }


    public void updateFarmerModule(long farmerModuleId, int subModuleId, int statusStarted, int statusFinished, int plotId) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED, statusFinished);
        values.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_PLOT_ID, plotId);

        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        try {
            db.update(OFISDBContract.FarmerModulesEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
    }

    public void updateFarmerChildModule(long farmerModuleId, int subModuleId, int statusStarted, int statusFinished) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID, subModuleId);
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_STARTED, statusStarted);
        values.put(OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_FINISHED, statusFinished);

        String whereClause = OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        try {
            db.update(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){}
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
            return db.rawQuery("SELECT * FROM " + OFISDBContract.NetworkQueueEntry.TABLE_NAME +
                            " ORDER BY CASE " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'farmer' THEN 1 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'consentstatement' THEN 2 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'mandate_module' THEN 3 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'stakeholder' THEN 4 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'module' THEN 5 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'children' THEN 6 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'Multifarm Update' THEN 7 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = 'infrastructure' THEN 8 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE + " = 'Polygon recorded' THEN 9 " +
                            "WHEN " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE + " = 'Location recorded' THEN 10 " +
                            "ELSE 11 END, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE,
                    null);
        }

    }

    void deleteNetworkQueueItem(int networkQueueItemId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkQueueItemId)};
        try {
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

    }

    void updatePlacelocalupdate() {
        ContentValues newValues = new ContentValues();
        newValues.put(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE, "VILLAGE");
        String whereClause = OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE + " = ?";
        String[] whereArgs = new String[]{"localinsert"};
        try {
            db.update(OFISDBContract.PlacesEntry.TABLE_NAME, newValues, whereClause, whereArgs);
        }catch (Exception e){}

    }

    void deleteSipNetworkQueueItem(int networkQueueItemId) {
        String whereClause = OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(networkQueueItemId)};
        try {
            db.delete(OFISDBContract.AnswerSipEntry.TABLE_NAME, whereClause, whereArgs);
            deleteNetworkQueueItem(networkQueueItemId);
        }catch (Exception e){}
    }

    public void deleteSupplierSurveyNetworkQueueItem(String supplierId, String type, int moduleId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID + " = ? ";
        String[] whereArgs = new String[]{supplierId, type, String.valueOf(moduleId)};
        try {
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

    }

    public long deleteNetworkQueueItemByFarmerId(int farmerId, String type) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId), type};
        return db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    public void deleteNetworkQueueItemByFarmerChildModuleId(int farmerModuleId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};
        try {
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isFarmerSynced(long farmerId) {
        Cursor farmer = null;
        try {
            farmer = getFarmerById(farmerId);
            return farmer.moveToNext() && (farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 1);
        } finally {
            farmer.close();
        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isChildSynced(long childId) {
        Cursor child = null;
        try {
            child = getChildById(childId);
            return child.moveToNext() && (child.getInt(child.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED)) == 1);
        } finally {
            child.close();
        }

    }

    void markFarmerAsSynced(int localFarmerId, int id, int farmId, int localPlaceId, int newPlaceId, String olamId, String odFarmerId, String complainceFlag) {
        // check to see if new id is already existing
        Cursor newFarmer = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, new String[]{"id"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        try {
            ContentValues newValues = new ContentValues();
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ID, id);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID, farmId);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, olamId);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED, 1);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_FARMER_ID, id);
            newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG, complainceFlag);
            if (newPlaceId > 0) {
                newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, newPlaceId);
            }
            try {
                newValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID, "null");
            }catch (Exception e){
                e.printStackTrace();
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
                } catch (Exception e) {
                }

                try {
                    db.update(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, farmerValues, OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                } catch (Exception e) {
                }

                try {
                    ContentValues farmerChildVisitValues = new ContentValues();
                    farmerChildVisitValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID, id);
                    db.update(OFISDBContract.ChildVisitDetails.TABLE_NAME, farmerChildVisitValues, OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                } catch (Exception e) {
                }

                try {
                    ContentValues farmerChildValues = new ContentValues();
                    farmerChildValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, id);
                    db.update(OFISDBContract.ChildEntry.TABLE_NAME, farmerChildValues, OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                } catch (Exception e) {
                }
                // Child labour update Ends

                // update farmer Id in db
                try {
                    ContentValues gpsTrackValues = new ContentValues();
                    gpsTrackValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_ID, id);
                    db.update(OFISDBContract.GPSDetails.TABLE_NAME, gpsTrackValues, OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_ID + " = ?", whereArgs);
                } catch (Exception e) {
                }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        newFarmer.close();
    }

    void markFarmerAsSyncedMs(long localFarmerId, long msId, String farmId, String olamFarmerId) {

        try {

            String[] whereArgs = new String[]{String.valueOf(localFarmerId)};

            if (ofisRoomDB.multiStakeHolderDao().updateStakeholderAfterSync(localFarmerId, msId, farmId, olamFarmerId, 1) > 0) {

                ContentValues stakeHolderIdValue = new ContentValues();
                stakeHolderIdValue.put(OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID, msId);

                // update any saved phone numbers
                db.update(OFISDBContract.PhoneNumber.TABLE_NAME, stakeHolderIdValue, OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID + " = ?", whereArgs);

                // update any network queue items
                db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, stakeHolderIdValue, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID + " = ?", whereArgs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Cursor getAllUnUploadedPhotoById(Integer farmerModuleId, ArrayList<Integer> submoduleList) {
        StringBuilder submoduleIds = new StringBuilder();
        for (int subId : submoduleList) {
            submoduleIds.append(subId).append(",");
        }
        String submoduleIdsString = submoduleIds.length() > 0 ? submoduleIds.substring(0, submoduleIds.length() - 1) : "";

        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*,"
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " in (" + submoduleIdsString + ") AND "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID + " = 12 ORDER BY "
                + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
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

    public Cursor getAnswersByFarmerModuleId(int farmerModuleId, ArrayList<Integer> submoduleList) {
        StringBuilder submoduleIds = new StringBuilder();
        for (int subId : submoduleList) {
            submoduleIds.append(subId).append(",");
        }
        String submoduleIdsString = submoduleIds.length() > 0 ? submoduleIds.substring(0, submoduleIds.length() - 1) : "";

        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ANSWER_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " in (" + submoduleIdsString + ") AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }

    public Cursor getAnswersByFarmerModuleQuestionId(int farmerModuleId, int questionId) {
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0  AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ?  AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + "= ? ORDER BY " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
        String[] whereArgs = new String[]{String.valueOf(questionId), String.valueOf(farmerModuleId)};

        return db.rawQuery(sql, whereArgs);
    }


    public Cursor getAnswersByQuestionId(int farmerModuleId, int questionId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId), String.valueOf(questionId)};
        Cursor cursor = db.query(OFISDBContract.AnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        return cursor;
    }

    Cursor getAnswersByFarmerChildModuleId(int farmerModuleId, ArrayList<Integer> submoduleList) {
        StringBuilder submoduleIds = new StringBuilder();
        for (int subId : submoduleList) {
            submoduleIds.append(subId).append(",");
        }
        String submoduleIdsString = submoduleIds.length() > 0 ? submoduleIds.substring(0, submoduleIds.length() - 1) : "";

        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String sql = "SELECT " + answerTable + ".*," + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ANSWER_TYPE + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME +
                " FROM " + answerTable + " INNER JOIN " + questionTable + " ON " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " = " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID +
                " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID +
                " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " = 0 AND " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " in (" + submoduleIdsString + ") AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? ORDER BY " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " ASC";
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

    public Cursor getFmpByFarmerId(long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(farmerId)
        };
        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return cursor;

        //  return  db.query(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public Cursor getfarmerModules(String modulesQuery, String[] modulesQueryValues) {
        @SuppressLint("Recycle") Cursor farmerModules = db.rawQuery(modulesQuery, modulesQueryValues);
        return farmerModules;
    }


    Cursor getFmpSections() {
        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.FarmManagementPlanSectionsEntry.TABLE_NAME, new String[]{"*"}, null, null, null, null, null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        return  cursor;
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
        Cursor cursor=null;
        try {
            cursor= db.rawQuery(sql, whereArgs);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        return  cursor;

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
        try {
            db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
    }

    Cursor getFmpBaselineQuestions(long farmerId, long fmpId) {
        Log.d(TAG, "getFmpBaselineQuestions");
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + ", "
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
        try {
            db.delete(OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME, delWhereClause, delWhereArgs);
        }catch (Exception e){}

        String updWhereClause = OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID + " = ?";
        String[] updWhereArgs = new String[]{String.valueOf(fmpId)};
        ContentValues updateValues = new ContentValues();
        updateValues.put(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_HAS_FEEDBACK, 1);
        try {
            db.update(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, updateValues, updWhereClause, updWhereArgs);
        }catch (Exception e){}

    }

    void syncFarmerModule(int farmerModuleId, long farmerId, int moduleId, String submissionDate, int farmId, int plotId, String stakeholderId) {
        ArrayList<Integer> getAllSubModulesId = getAllSubmodulesByModuleId(moduleId);
        StringBuilder submoduleIds = new StringBuilder();
        for (int subId : getAllSubModulesId) {
            submoduleIds.append(subId).append(",");
        }
        String submoduleIdsString = submoduleIds.length() > 0 ? submoduleIds.substring(0, submoduleIds.length() - 1) : "";
        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " in (" + submoduleIdsString + ") " ;
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        try {
            db.delete(OFISDBContract.AnswerEntry.TABLE_NAME, whereClause, whereArgs);

            whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " = ?";
            db.delete(OFISDBContract.FarmerModulesEntry.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

        try {
            JSONObject newModuleEntry = new JSONObject();
            newModuleEntry.put("id", moduleId);
            newModuleEntry.put("created_at", submissionDate);
            newModuleEntry.put("farmId", farmId);
            newModuleEntry.put("plotId", plotId==0 ? "null" : plotId);
            JSONObject newModule = new JSONObject();
            newModule.put("module", newModuleEntry);

            String submittedModulesJSON;
            if (stakeholderId != null) {
                MultiStakeHolder multiStakeHolder = ofisRoomDB.multiStakeHolderDao().getStakeHolderById(Long.parseLong(stakeholderId));
                submittedModulesJSON = (multiStakeHolder.submitted_modules == null) ? "null" : multiStakeHolder.submitted_modules;
                ofisRoomDB.multiStakeHolderDao().updateSubmittedModulesById(stakeholderId, updateModules(submittedModulesJSON, newModule));
            } else {
                Cursor farmer = getFarmerById(farmerId);
                if (farmer.getCount() > 0) {
                    farmer.moveToFirst();
                    submittedModulesJSON = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES));
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES, updateModules(submittedModulesJSON, newModule));
                    db.update(OFISDBContract.FarmerEntry.TABLE_NAME, updateValues, "id = ?", new String[]{String.valueOf(farmerId)});
                    db.update(OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME, updateValues, "id = ?", new String[]{String.valueOf(farmerId)});
                }
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DataLayer: Error creating farmer submitted module", e));
            e.printStackTrace();
        }
    }

    private String updateModules(String submittedModulesJSON, JSONObject newModule) throws JSONException {
        String sampleStr = "{\"modules\":null}";
        if (sampleStr.equals(submittedModulesJSON)) {
            submittedModulesJSON = submittedModulesJSON.replaceAll("\"modules\":null", "");
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
        return submittedModules.toString();
    }



    // Remove and update after sync
    void syncChildFarmerModule(int farmerModuleId, long childId, int moduleId, String submissionDate, String surveyCode) {
        ArrayList<Integer> getAllSubModulesId = getAllSubmodulesByModuleId(moduleId);
        StringBuilder submoduleIds = new StringBuilder();
        for (int subId : getAllSubModulesId) {
            submoduleIds.append(subId).append(",");
        }
        String submoduleIdsString = submoduleIds.length() > 0 ? submoduleIds.substring(0, submoduleIds.length() - 1) : "" ;
        String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? AND " + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " in (" + submoduleIdsString + ") ";
        String[] whereArgs = new String[]{String.valueOf(farmerModuleId)};

        db.delete(OFISDBContract.AnswerEntry.TABLE_NAME, whereClause, whereArgs);

        whereClause = OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " = ?";
        db.delete(OFISDBContract.FarmerChildModulesEntry.TABLE_NAME, whereClause, whereArgs);


        // Instead of farmer, remove from child submitted modules thing

        Cursor child = getChildById(childId);
        if (child.getCount() > 0) {
            child.moveToFirst();
            try {


                JSONObject newModuleEntry = new JSONObject();
                newModuleEntry.put("id", moduleId);
                newModuleEntry.put("surveyCode", surveyCode);
                newModuleEntry.put("created_at", submissionDate);

                String submittedModulesJSON = child.getString(child.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES));
                JSONObject submittedModules = null;

                if (submittedModulesJSON == null || submittedModulesJSON.equalsIgnoreCase("null")) {
                    submittedModules = new JSONObject();
                    JSONArray moduleArray = new JSONArray();
                    moduleArray.put(newModuleEntry);
                    submittedModules.put("modules", moduleArray);
                } else {
                    submittedModules = new JSONObject(submittedModulesJSON);
                    JSONArray modulesArray = submittedModules.getJSONArray("modules");
                    modulesArray.put(newModuleEntry);
                }

                ContentValues updateValues = new ContentValues();
                updateValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES, submittedModules.toString());

                db.update(OFISDBContract.ChildEntry.TABLE_NAME, updateValues, "tmpId = ?", new String[]{String.valueOf(childId)});

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("DataLayer: Error creating farmer submitted module", e));
                e.printStackTrace();
            }

        }
    }

    void removeNIDPhotostatusByType(String type, long farmerid) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? AND " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? ";
        String[] whereArgs = new String[]{type, String.valueOf(farmerid)};

        try {
            db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}

    }

    void updateNIDPhoto(int id, String photo) {
//        delete photo if exists
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        try {
            db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO, photo);
        values.put(OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE, id);
        try {
            db.insert(OFISDBContract.LocalNationalIDImages.TABLE_NAME, null, values);
        }catch (Exception e){}


    }

    void clearlocalniddbvalue() {
        try {
            db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, null, null);
            db.delete(OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME, null, null);
        }catch (Exception e){}
    }

    void removeNIDPhoto(int id) {
//        delete photo if exists
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        try {
            db.delete(OFISDBContract.LocalNationalIDImages.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){}
    }

    public Cursor getPhotoByType(int id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(id)};

        return db.query(OFISDBContract.LocalNationalIDImages.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    //JIRA req OW-505
    int updateFarmerPhoto(long farmerId, String farmerPhoto, String olamFarmerId, String farmerName) {

        int updated = 0;
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, farmerPhoto);
        values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY, 1);
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};

        try {
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
                if (networkEntryTypeExistsByFarmerId("farmer_photo", farmerId, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)) {
                    updateNetworkQueueFarmerPhotoEntry(nqiValues, farmerId);
                } else {
                    insertNetworkQueueEntry(nqiValues);
                }
            }
            return updated;
        }catch (Exception e){}
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
        Long rowId = null;
        try {
            rowId= db.insert(OFISDBContract.Subquestionanswer.TABLE_NAME, null, values);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return rowId;
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
        String sql_query = "select farmers.name as 'farmer.name', "
                + "farmers.olam_farmer_id, countries.id as 'country.id', "
                + "countries.name as 'country.name', modules.name as 'module.name',"
                + "farmergroups.name as 'farmergroup.name', farmergroups.id as 'farmergroup.id',"
                + " farmergroups.product_id as 'farmergroup.product_id', farmergroups.partner_id as 'farmergroup.partner_id' "
                + "from farmers "
                + "inner join places inner join districts inner join regions inner join countries "
                + "inner join modules inner join sections inner join farmergroups On "
                + "farmers.place_id=places.id  and farmers.section_id = sections.id and "
                + "sections.farmergroup_id=farmergroups.id and places.district_id = districts.id and "
                + "districts.region_id= regions.id and regions.country_id = countries.id "
                + "where farmers.id =" + farmerId + " and modules.id = " + moduleId;

        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql_query, null);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;

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

        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql, whereArgs);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
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

        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql, whereArgs);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;

    }

    public Cursor getSipRequiredQuestionIds(int moduleId, int subModuleId) {
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String submoduleTable = OFISDBContract.SipSubGroupEntry.TABLE_NAME;
        String moduleTable = OFISDBContract.SipGroupEntry.TABLE_NAME;
        String whereClause = moduleTable + "." + OFISDBContract.SipGroupEntry.COLUMN_NAME_ID + " = ? " +
                "AND " + submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID + " = ? AND " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " LIKE ?";
        String[] whereArgs = new String[]{
                String.valueOf(moduleId),
                String.valueOf(subModuleId),
                "%\"requiredfield\":true%"
        };
        String sql = "SELECT " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " FROM " + questionTable + " INNER JOIN " + submoduleTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_SUBMODULE_ID + " = " + submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID
                + " INNER JOIN " + moduleTable + " ON " + submoduleTable + "." + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " = " + moduleTable + "."
                + OFISDBContract.SipGroupEntry.COLUMN_NAME_ID + " WHERE " + whereClause;
        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql, whereArgs);} catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public long insertNetworkQueueSipEntry(ContentValues values, String randomId) {
        long newRowId = 0;
        if (!values.containsKey(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)) {
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, randomId);
        }

        try {
            newRowId = db.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return newRowId;
    }

    public Cursor getSIPModules() {
        String[] columns = new String[]{"*"};
        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.SipGroupEntry.TABLE_NAME, columns, null, null, null, null, null);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public Cursor getSipSubmodulesByModuleId(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        String orderBy = OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " ASC";//OFISDBContract.SipSubGroupEntry.COLUMN_NAME_POSITION + " ASC";

        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.SipSubGroupEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return cursor;
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

        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql, whereArgs);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public Cursor getSIPQuestionsAndAnswersByUploadId(String ulpoadID, int submoduleId/* long farmerId, long farmerModuleId*/) {
        String questionTable = OFISDBContract.QuestionSipEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesSipEntry.TABLE_NAME;
        String answerTable = OFISDBContract.AnswerSipEntry.TABLE_NAME;

        String columns = questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;
        String Query = "" + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " = ?"
                + " AND " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_SUBMODULE_ID + " =?";

        String sql = "SELECT " + columns + " FROM " + answerTable + " LEFT OUTER JOIN " + questionTable + " INNER JOIN datatypes_sip "
                + " ON " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID + " = " + datatypeTable + "." + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_ID
                + " AND " + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID
                + " WHERE " + Query + " GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + " ASC";
        String[] whereArgs = new String[]{
                ulpoadID,
                String.valueOf(submoduleId)
        };

        Cursor cursor=null;
        try {
            cursor=db.rawQuery(sql, whereArgs);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;

    }

    public boolean isIdAvailablePhoneDB(long farmerLong) {
        boolean available = false;
        String sql = "select * from phonenumbers where farmer_id =" + farmerLong;
        if (db.rawQuery(sql, null).getCount() > 0) {
            available = true;
        }
        return available;
    }

    public Cursor getEnumeratorCountries() {
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT * FROM countries", null);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public Cursor getAllPlaces() {
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT * FROM places", null);
        } catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public int getEnumeratorProduct() {
        Cursor productCursor;
        int productId = 0;
        try {
            productCursor = db.rawQuery("SELECT id FROM products", null);
            if (productCursor.getCount() > 0) {
                productCursor.moveToNext();
                productId = productCursor.getInt(productCursor.getColumnIndex("id"));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return productId;
    }

    public long insertChild(Context context, ContentValues contentValues, String name) {
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
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, name);
            insertNetworkQueueEntry(networkQueueValues);
        }

        return newChildId;
    }

    public long updateChild(Context context, ContentValues contentValues, String id, String name, String childName) {
        long newChildId;
        long update_nq = 0;
        String whereClause = OFISDBContract.ChildEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_ID))};
        newChildId = db.update(OFISDBContract.ChildEntry.TABLE_NAME, contentValues, whereClause, whereArgs);


        if (newChildId != -1) {
            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "children");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, context.getResources().getString(R.string.new_child));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, contentValues.getAsString(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, name);

            // update data from network queue entry
            String d_whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
            ;
            String[] d_whereArgs = new String[]{String.valueOf(id), "children"};
            update_nq = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, networkQueueValues, d_whereClause, d_whereArgs);
        }
        try {
            ContentValues networkQueueValuesChildModule = new ContentValues();
            networkQueueValuesChildModule.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, context.getResources().getString(R.string.child) + ": " + childName);
            String d_whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
            ;
            String[] d_whereArgs = new String[]{String.valueOf(id), "childmodule"};
            update_nq = db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, networkQueueValuesChildModule, d_whereClause, d_whereArgs);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }


        return update_nq;
    }

    public long insertConsentStatementDetails(ContentValues contentValues, String olamfarmername, String type, String poiType ) {
        long newRowId;
        newRowId = db.insert(OFISDBContract.ConsentStatementEntry.TABLE_NAME, null, contentValues);
        if (type.equalsIgnoreCase("update")) {
            if (newRowId != -1) {
                // add a network queue entry
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                ContentValues networkQueueValues = new ContentValues();
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "consentstatement");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, poiType);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, olamfarmername);
//            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_DISPLAYFARMER_ID));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, contentValues.getAsString(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_ID));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, contentValues.getAsString(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_MULTI_STAKEHOLDER_ID));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, contentValues.getAsString(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_DISPLAYFARMER_ID));

                insertNetworkQueueEntry(networkQueueValues);
            }
        }
        return newRowId;
    }

    public long insertChildVisitSurveyDetails(ContentValues contentValues, String name, String type) {
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
        return newRowId;
    }

    public Cursor getChildVisitDetailsById(Long farmerId) {
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT * FROM child_visit where farmerId=" + farmerId, null);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return cursor;
    }

    public long deleteChildVisitDetailsById(Long farmerId) {
        String whereClause = OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(farmerId), "0"};
        return db.delete(OFISDBContract.ChildVisitDetails.TABLE_NAME, whereClause, whereArgs);
    }

    public Cursor getChildDetailsById(Long farmerId) {
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT * FROM children where farmerId=" + farmerId, null);}
        catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return cursor;
    }


    public Cursor getChildById(long id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.ChildEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.ChildEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;

    }

    public Cursor getConsentStatementById(long id, String columnName) {
        String[] columns = new String[]{"*"};
        String whereClause = columnName + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        Cursor cursor=null;
        try {
            cursor=db.query(OFISDBContract.ConsentStatementEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}

        return cursor;
    }

    public void updateChildId(int tempChildId, int generatedChildId) {

        ContentValues cv = new ContentValues();
        cv.put(OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID, generatedChildId); //These Fields should be your String values of actual column names
        cv.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 1);
        try {
            db.update(OFISDBContract.ChildEntry.TABLE_NAME, cv, "tmpId=" + tempChildId, null);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        // update child Id in db
        String[] whereArgs = new String[]{String.valueOf(tempChildId)};
        try {
            ContentValues gpsTrackValues = new ContentValues();
            gpsTrackValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_ID, generatedChildId);
            db.update(OFISDBContract.GPSDetails.TABLE_NAME, gpsTrackValues, OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_ID + " = ?", whereArgs);
        } catch (Exception e) {
        }

    }

    public void updateCheckdetails(int fmpId, int surveyCode) {


        ContentValues cv = new ContentValues();
        cv.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED, 1);
        String whereClause = OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " = ?  AND " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE + " = ?";

        String[] whereArgs = new String[]{
                String.valueOf(fmpId),
                String.valueOf(surveyCode)
        };
        try {
            db.update(OFISDBContract.ChildVisitDetails.TABLE_NAME, cv, whereClause, whereArgs);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
    }

    public Integer getChildServerId(long childNwId) {
        Integer serverId = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM children where tmpId=" + childNwId, null);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        if (cursor != null) {
            while (cursor.moveToNext()) {
                serverId = cursor.getInt(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID));
            }
        }
        return serverId;
    }

    public long insertGeoData(Context context, ContentValues contentValues) {
        long newGeoId = 0;
        try {
            newGeoId = db.insert(OFISDBContract.GPSDetails.TABLE_NAME, null, contentValues);
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return newGeoId;
    }

    public Cursor getGeoTrackList() {
        return db.rawQuery("SELECT * FROM " + OFISDBContract.GPSDetails.TABLE_NAME, null);
    }

    public long insertGPSDetailsNetworkQueue(Context context, ContentValues contentValues) {

        long newGpsEntry;
        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "gps_track");
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, Utilities.getEnumeratorNameSharedPrefs(context));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, context.getResources().getString(R.string.enumerator_location_capture));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, Utilities.getFromSharedPrefs(context));

        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, contentValues.getAsString(OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, contentValues.getAsString(OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ACCURACY, contentValues.getAsString(OFISDBContract.GPSDetails.COLUMN_NAME_GPS_ACCURACY));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());

        newGpsEntry = insertNetworkQueueEntry(networkQueueValues);
        return newGpsEntry;

    }


    public void deleteGeoPoints() {
        db.delete(OFISDBContract.GPSDetails.TABLE_NAME, null, null);
    }

    public long updateSignature(Context context, long farmerId, String signature, String farmername) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.SignatureEntry.COLUMN_NAME_FAMRERID, farmerId);
        values.put(OFISDBContract.SignatureEntry.COLUMN_NAME_SIGNATURE, signature);
        updateNetworkQueueSignatureEntry(context, farmerId, signature, farmername);
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
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());

        newEntry = insertNetworkQueueEntry(networkQueueValues);

        return newEntry;
    }

    // getting nationalids type id values
    public String getNidTypebyid(int id) {
        String type = null;
        try {
            Cursor nid = getNIDById(id);
            if (nid.getCount() > 0) {
                nid.moveToFirst();
                type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE));
                String language = Locale.getDefault().getLanguage();
                if (language.equalsIgnoreCase(Utilities.FR)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR));
                } else if (language.equalsIgnoreCase(Utilities.ES)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES));
                } else if (language.equalsIgnoreCase(Utilities.PT)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT));
                } else if (language.equalsIgnoreCase(Utilities.ID)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID));
                } else if (language.equalsIgnoreCase(Utilities.TR)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR));
                } else if (language.equalsIgnoreCase(Utilities.LO)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO));
                } else if (language.equalsIgnoreCase(Utilities.VI)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI));
                } else if (language.equalsIgnoreCase(Utilities.TH)) {
                    type = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH));
                }
            }}catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return type;
    }

    private long updateNetworkQueueNIDEntry(Context context, Long farmerId, String stakeHolderId, int nationalidtype, String farmername, String status) {
        long newEntry;
        String submissionType = "National_ID";
        if (!status.equals("2")) {
            submissionType = "Update_ID";
        }

        ContentValues networkQueueValues = new ContentValues();
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, submissionType);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, context.getResources().getString(R.string.nationalid));
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, farmername);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, getNidTypebyid(nationalidtype));

       // OW-4590 Fix
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, nationalidtype);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeHolderId);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());

        newEntry = insertNetworkQueueEntry(networkQueueValues);
        return newEntry;

    }

    private void deleteExistingNationalIdFromNq(Context context, Long farmerId) {
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + "=? AND " +
                OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE + "=?";

        String[] whereArgs = new String[] {
                String.valueOf(farmerId),
                context.getResources().getString(R.string.nationalid)
        };

        db.delete(OFISDBContract.NetworkQueueEntry.TABLE_NAME, whereClause, whereArgs);
    }

    // OW-4590 Fix
    public int getNIDTypeIDByName(String type) {
        int id = 0;
        try {
            Cursor nid = getNIDByName(type);
            if (nid.getCount() > 0) {
                nid.moveToFirst();
                id = nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID));
            } else {
                Cursor nids = getNIDByNameAnywhere(type);
                if (nids.getCount() > 0) {
                    nids.moveToFirst();
                    id = nids.getInt(nids.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID));
                }
                nids.close();
            }
            nid.close();
        }catch (Exception e){FirebaseCrashlytics.getInstance().log(e.getMessage());}
        return id;
    }

    // OW-4590 Fix
    public Cursor getNIDByNameAnywhere(String type) {
        String whereClause = OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " = ? OR " +
                OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH + " = ?";
        String[] columns = new String[]{"*"};
        String[] whereArgs = new String[]{type,type,type,type,type,type,type,type,type};
        return db.query(OFISDBContract.NationalIDEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }


    public int checkToSyncGPS() {
        int count = 0;
        Cursor cursor = db.rawQuery("select * from networkqueue where submission_type in (" + "'farmer'" + "," + "'children'" + "," + "'infrastructure'" + ")", null);

        try {
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return count;
    }

    public void updateFarmerDetails(long editFarmerId, String farmerNameValue, String farmerGender) {
        try {
            String whereClause = OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(editFarmerId)};
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_NAME, farmerNameValue);
            values.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_GENDER, farmerGender);
            db.update(OFISDBContract.GPSDetails.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    public void updateConsentValue(String farmerName, long farmerId) {
        try {
            String whereClause = OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER, farmerName);
            values.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_OLAM_FARMER_NAME, farmerName);
            db.update(OFISDBContract.ConsentStatementEntry.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    public void updateChildDetails(String editChildId, String childNameValue, String childGender, String age) {
        try {
            String whereClause = OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(editChildId)};
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_NAME, childNameValue);
            values.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_GENDER, childGender);
            values.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_AGE, age);
            db.update(OFISDBContract.GPSDetails.TABLE_NAME, values, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    public void updateSIPNameforGPS(Context context, String sipName, int updated) {
        try {
            String whereClause = OFISDBContract.GPSDetails.COLUMN_NAME_NETWORK_QUEUE_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(updated)};
            ContentValues sipContentValues = new ContentValues();
            sipContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SIP_NAME, sipName);
            db.update(OFISDBContract.GPSDetails.TABLE_NAME, sipContentValues, whereClause, whereArgs);
        } catch (Exception e) {
            // ignore if fails , Can't able to update
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

    }

    public ArrayList<String> getNationalIdNames(ArrayList<String> nationalIds) {
        String nationalIdsStr = TextUtils.join(", ", nationalIds);
        Cursor cursor=null;
        try {
            cursor = db.rawQuery("select * from nationalids where id in (" + nationalIdsStr + ")", null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        ArrayList<String> types = new ArrayList<>();
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    types.add(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE)));
                }
            }
        }
        return types;
    }


    public boolean farmerExistByfarmerGroupSection(long farmerGroupId, long sectionId, long moduleId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP + " = ? AND "
                +OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION + " = ? AND "
                + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerGroupId), String.valueOf(sectionId), String.valueOf(moduleId)};

        Cursor cursor = db.query(OFISDBContract.DownloadedSurveyList.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();


        return exists;
    }


    public boolean isAvailableInNqTable(long farmerId){
        boolean available = false;
        try {
            // Check in Add Farmer registration Table
            String[] columnsReg = new String[]{"*"};
            String whereClauseReg = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
            String[] whereArgsReg = new String[]{String.valueOf(farmerId), "farmer"};
            Cursor cursorReg = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columnsReg, whereClauseReg, whereArgsReg, null, null, null);

            if (cursorReg != null) {
                if (cursorReg.getCount() > 0) {
                    available = true;
                }
            }
            cursorReg.close();
        } catch (Exception e) {
        }

        return available;
    }


    public String getQuestionNo(String questionNo) {

        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(questionNo)};
        Cursor cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        int questionPosition = 0;
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String displayConfigStr;
                        displayConfigStr = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION));
                        JSONObject displayObject=new JSONObject(displayConfigStr);
                        String calculationNode = displayObject.optString(CoffeeConstants.CALCULATION);
                        JSONObject jsonObject = new JSONObject(calculationNode);
                        questionPosition = jsonObject.optInt(CoffeeConstants.QUESTION_NUMBER,0);
                    }
                }
            } catch (Exception e) {
            }
        }
        cursor.close();
        return String.valueOf(questionPosition);
    }

    public String getQuestionSurveyCompletion(String questionNo) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(questionNo)};
        Cursor cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        String questionPosition = "";
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String displayConfigStr;
                        displayConfigStr = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION));
                        JSONObject displayObject=new JSONObject(displayConfigStr);
                        String calculationNode = displayObject.optString(CoffeeConstants.CALCULATION);
                        JSONObject jsonObject = new JSONObject(calculationNode);
                        questionPosition = jsonObject.optString(CoffeeConstants.QUESTION_NUMBER,"");
                    }
                }
            } catch (Exception e) {
            }
        }
        cursor.close();
        return questionPosition;
    }

    public Cursor getQuestionById(int questionId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(questionId)};
        Cursor cursor=null;
        try {
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        return cursor;
    }

    public ArrayList<Integer> getAllSubmodulesQuestionsId(Cursor questions) throws JSONException {
        // get the list of questions by submodules
        ArrayList<Integer> questionList=new ArrayList<>();
        if(questions != null && questions.moveToFirst()) {
            do {
                String questionId = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                questionList.add(Integer.parseInt(questionId));
            } while (questions.moveToNext());
        }
        return questionList;
    }

    public ArrayList<Integer> getAllSubmodulesByModuleId(int moduleId) {
        ArrayList<Integer> submoduleList = new ArrayList<>();

        String selection = "module_id = ?";
        String[] selectionArgs = {String.valueOf(moduleId)};

        Cursor cursor = db.query(OFISDBContract.SubmoduleEntry.TABLE_NAME, new String[]{OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID},
                selection, selectionArgs,null,
                null,
                null);

        while (cursor.moveToNext()) {
            int subId = cursor.getInt(cursor.getColumnIndexOrThrow(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
            submoduleList.add(subId);
        }

        cursor.close();

        return submoduleList;
    }

    public String getProductInfo() {
        String productdetails="";
        String[] columns = new String[]{"*"};
        Cursor cursor = db.query(OFISDBContract.ProductsEntry.TABLE_NAME, columns, null, null, null, null, null);

        List<String> products = new ArrayList<String>();
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String product = cursor.getString(cursor.getColumnIndex(OFISDBContract.ProductsEntry.COLUMN_NAME_PRODUCT_NAME));
                        products.add(product);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        productdetails= String.join(", ", products);
        return productdetails;
    }

    public long getProductIdByFarmerGroupId(long farmerGroupId) {

        long productId=0;
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID+ " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerGroupId)};
        Cursor mCursor= db.query(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        try {
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    productId = Long.valueOf(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PRODUCT_ID)));
                }
            }
        }catch (Exception e){

        }finally {
            mCursor.close();
        }
        return productId;
    }

    public FarmerGeo getFarmerInfo(Context context, Long farmerId) {
        FarmerGeo farmerGeo = new FarmerGeo();
        Cursor farmer = getFarmerById(farmerId);
        if (farmer != null && farmer.getCount() > 0) {
            farmer.moveToFirst();
            try {
                farmerGeo.setCountryId(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID))));
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
            }

            long farmerGroupID=0;
            try {
                farmerGroupID = Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID)));
                long productId=getProductIdByFarmerGroupId(farmerGroupID);
                farmerGeo.setProductId((int)productId);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
            }
        }
        return farmerGeo;
    }

    public GeoDTO getInfoByFarmerGroupId(int farmerGroupId) {
        GeoDTO geoDTO=new GeoDTO();
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID+ " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerGroupId)};
        Cursor mCursor= db.query(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        try {
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    geoDTO.setProductId(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PRODUCT_ID)));
                    geoDTO.setPartnerId(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PARTNER_ID)));
                    geoDTO.setProgrammeId(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PROGRAMME_ID)));
                }
            }
        }catch (Exception e){

        }finally {
            mCursor.close();
        }
        return geoDTO;

    }
    public List<FarmerRoleDto> getFarmerRoles(String locale) {

        List<FarmerRoleDto> farmerRoleDTOList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID + "," +  locale  + " as name " + " FROM multifarmRoles ", null);
        }
        catch (Exception e) {
            cursor = db.rawQuery("select * from multifarmRoles", null);
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        FarmerRoleDto farmerRoleDTO = new FarmerRoleDto();
                        int roleId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID)));
                        String name = cursor.getString(cursor.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME));
                        if (roleId > 1) {
                            farmerRoleDTO.setId(roleId);
                            farmerRoleDTO.setName(name);
                            farmerRoleDTOList.add(farmerRoleDTO);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if(!cursor.isClosed()){
            cursor.close();
        }

        return farmerRoleDTOList;
    }

    public String getFarmerNamefromDb(Long farmerId) {
        String farmerName="";
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    farmerName = farmerCursor.getString(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return  farmerName;
    }

    String getValueByLookUpID(String lookupId, Cursor qCursor, String questionColumnSuffix){
        String lookupValue = "";
        Cursor farmerCursor = null;
        final String columnName = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.LookupValuesEntry.COLUMN_NAME_ID + " =?";
            String[] whereArgs = new String[]{lookupId};
            farmerCursor = db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null,null);
            if (farmerCursor != null){
                while(farmerCursor.moveToNext()){
                    lookupValue = farmerCursor.getString(farmerCursor.getColumnIndex(columnName));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            farmerCursor.close();
        }
        return lookupValue;
    }

    public String getChildNamefromDb(Long childId) {
        String childName="";
        Cursor cursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(childId)};
            cursor=db.query(OFISDBContract.ChildEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    childName = cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME));
                }
            }
        } catch (Exception e) {
        }finally {
            cursor.close();
        }
        return  childName;
    }

    public String getAnswerTypeValue(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.AnswerType.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.AnswerType.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.AnswerType.COLUMN_NAME_VALUE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }
    public String getQuestionAssignmentTypeId(int id){
        String value = "" ;
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ASSIGNMENT_TYPE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String getQuestionAssignmentFieldId(int id){
        String value = "" ;
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ASSIGNMENT_FIELD));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }


    public String getAnswerValuebylookUpId(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.LookupValuesEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.LookupValuesEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String getAnswerTypeByQuestionID(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ANSWER_TYPE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String radioAnswerValueFromAnswersTable(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.AnswerEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String radioAnswerValueFromSavedAnswersTable(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.SavedAnswerEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_INT));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String locationValueFromSavedAnswersTable(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.SavedAnswerEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_TEXT));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String getQuestionTypeValue(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionType.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionType.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionType.COLUMN_NAME_VALUE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String getDisplayTypeIdByQuestionID(int id){
        String value = "" ;
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"displaytypeId"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public String getDisplayConfigurationByQuestionID(int id){
        String value = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            cursor = db.query(OFISDBContract.QuestionEntry.TABLE_NAME,columns,whereClause,whereArgs,null,null,null,null);
            if (cursor != null){
                while(cursor.moveToNext()){
                    value = cursor.getString(cursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return value;

    }

    public long getFarmerGroupById(Long farmerId) {
        long farmerGroupId=0;
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    farmerGroupId = Long.valueOf(farmerCursor.getString(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID)));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return  farmerGroupId;
    }

    public int getProductByFarmerId(Long farmerId) {
        int productId=0;
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    productId = farmerCursor.getInt(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return productId;
    }

    public int getCountryByFarmerId(Long farmerId) {
        int countryId = 0;
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    countryId = farmerCursor.getInt(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return countryId;
    }

    public long getSectionByFarmerId(Long farmerId) {
        long sectionId=0;
        Cursor farmerCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            farmerCursor = db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (farmerCursor != null) {
                while (farmerCursor.moveToNext()) {
                    sectionId = Long.valueOf(farmerCursor.getString(farmerCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID)));
                }
            }
        } catch (Exception e) {
        }finally {
            farmerCursor.close();
        }
        return  sectionId;
    }

    public boolean getModuledetailsByPlotId(int currentId, long farmerId ,int plotId) {

        boolean available=false;
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_PLOT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(currentId), String.valueOf(farmerId),String.valueOf(plotId)};
        Cursor cursor=null;
        try {
            cursor = db.query(OFISDBContract.FarmerModulesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if(cursor!=null){
                if(cursor.getCount()>0){
                    available=true;
                }
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return available;

    }

    public String getFarmerModuleIdByPlotId(long currentId, long farmerId ,int plotId) {

        String farmer_module_id = "0";
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_PLOT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(currentId), String.valueOf(farmerId),String.valueOf(plotId)};
        Cursor cursor=null;
        try {
            cursor = db.query(OFISDBContract.FarmerModulesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if(cursor!=null){
                if(cursor.getCount()>0){
                    while (cursor.moveToNext()) {
                        farmer_module_id = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID));
                    }
                }
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return farmer_module_id;

    }

    public ChildModuleDto getFarmerModuleDetailsByPlotId(long currentId, long farmerId ,int plotId) {
        ChildModuleDto moduleDto = null;
        String farmer_module_id = "0";
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_PLOT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(currentId), String.valueOf(farmerId),String.valueOf(plotId)};
        Cursor cursor=null;
        try {
            cursor = db.query(OFISDBContract.FarmerModulesEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if(cursor!=null){
                if(cursor.getCount()>0){
                    while (cursor.moveToNext()) {
                        moduleDto = new ChildModuleDto();
                        moduleDto.setStatus_started(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)));
                        moduleDto.setStatus_finished(cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)));
                    }
                }
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return moduleDto;

    }

    Cursor getFarmerRoleById(int Id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Id)};
        return db.query(OFISDBContract.MultiFarmRoles.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
    }

    public String getFarmerRole(int id) {
        String localeSuffixColumn = Utilities.getColumnNameByLanguage();
        String name = "";
        Cursor nids = getFarmerRoleById(id);
        try {
            nids.moveToFirst();
            name = nids.getString(nids.getColumnIndex(localeSuffixColumn));
            if (name.isEmpty()) {
                name = nids.getString(nids.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME));
            }
        } catch (Exception e) {
            nids.moveToFirst();
            name = nids.getString(nids.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME));
            e.printStackTrace();
        } finally {
            nids.close();
        }

        return name;
    }

    public Cursor getTraineeRelationship(int trainingRecordId, long farmerId) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " = ? AND " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId), String.valueOf(farmerId)};
        Cursor cursor=null;
        try {
            cursor = db.query(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        return cursor;
    }

    public JSONArray getTraineeRelationshipByRecordId(int trainingRecordId,Context context) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId)};
        Cursor cursor=null;
        JSONArray jsonArray=new JSONArray();
        try {
            cursor = db.query(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (cursor.getCount()>0){
                if(cursor != null && cursor.moveToFirst()) {
                    do {
                        JSONObject jsonObject1=new JSONObject();
                        jsonObject1.put("trainingattendeesId",cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID)));
                        jsonObject1.put("enumeratorsId",Utilities.getFromSharedPrefs(context));
                        jsonObject1.put("attendeesName",cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_NAME)));
                        jsonObject1.put("attendeesAge",cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_AGE)) );
                        jsonObject1.put("relationshipId",cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP_ID)));
                        jsonObject1.put("attendeesGender", cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER)));
                        jsonObject1.put("active", "true");
                        jsonArray.put(jsonObject1);
                    } while (cursor.moveToNext());
                }
            }

        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }finally {
            cursor.close();
        }
        return jsonArray;
    }

    public void addOrUpdateTrainees(Context context, long farmerId, int trainingRecordId, ArrayList<Attendee> attendeeList) {

        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " = ? AND " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId), String.valueOf(farmerId)};
        Cursor traineeCursor=null;
        try {
            traineeCursor = db.query(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        if (traineeCursor != null && traineeCursor.getCount() > 0) {
            db.delete(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, whereClause, whereArgs);
        }

        if (attendeeList.size()>0) {
            for (Attendee attendee : attendeeList) {
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID, farmerId);
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID, trainingRecordId);
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_NAME, attendee.getTraineeName());
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_AGE, attendee.getTraineeAge());
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP, attendee.getTraineeRelationship());
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP_ID, attendee.getTraineeRelationshipId());
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER, attendee.getTraineeGender());
                values.put(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER_ID, attendee.getTraineeGenderId());
                db.insert(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, null, values);
            }
        }
    }

    public int getFarmerByAttendeeItem(int itemId) {

        int farmerId = 0;
        try {
            Cursor cursor=db.rawQuery("select * from trainingattendees where id="+itemId,null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        farmerId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID)));
                    }
                }
            }catch (Exception e){
            }finally {
                cursor.close();
            }
        }catch (Exception e){}

        return  farmerId;
    }

    public HashMap<Long, Integer> prepareAttendeeCountList(Cursor trainingAttendees, int trainingRecordId) {
        HashMap<Long,Integer> hashMap=new HashMap<Long,Integer>();
        try {
            if (trainingAttendees != null) {
                while (trainingAttendees.moveToNext()) {
                    Cursor cursor=null;
                    try {
                        long farmerId=Long.valueOf(trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID)));
                        cursor=  getTraineeRelationship(trainingRecordId,farmerId);
                        if (cursor!=null){
                            hashMap.put(farmerId,cursor.getCount());
                        }
                    }catch (Exception e){
                    }finally {
                        cursor.close();
                    }
                }
            }
        }catch (Exception e){}

        return hashMap;
    }


    public List<AttendeeRelations> getTrainingRelations(RegisterAdditionalTrainee registerAdditionalTrainee,String locale) {
        List<AttendeeRelations> attendeeRelationsList=new ArrayList<AttendeeRelations>();
        if(locale.equalsIgnoreCase("_en")){
            locale="";
        }
        Cursor cursor = db.rawQuery("select * from farmerRelationShips", null);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        AttendeeRelations attendeeRelations = new AttendeeRelations();
                        attendeeRelations.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_ID))));
                        attendeeRelations.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME+locale)));
                        attendeeRelationsList.add(attendeeRelations);
                    }
                }
            } catch (Exception e) {
            }
        }
        if(!cursor.isClosed()){
            cursor.close();
        }

        return attendeeRelationsList;
    }

    public void deleteAttendeeByTrainingId(int attendeeId, long trainingRecordId) {

        // getfarmer Id from Attendee Id
        int farmerId=0;
        try {
            farmerId=getFarmerByAttendeeItem(attendeeId);
        }catch (Exception e){}


        String whereClause = OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " = ? AND " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(trainingRecordId), String.valueOf(farmerId)};
        try {
            db.delete(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

    }

    public boolean isCaptureAttendeeEligible(long trainingModuleId) {

        boolean isCaptureAttendee=false;

        Cursor cursor=db.rawQuery("select * from trainingmodules where id="+trainingModuleId,null);
        try {
            if (cursor !=null){
                if(cursor.getCount()>0){
                    while (cursor.moveToNext()) {
                        isCaptureAttendee=Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_NAME_CAPTURE_ATTENDEE)));
                    }
                }
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return isCaptureAttendee;

    }

    public void deleteTrainingAttendeeRelations(long trainingRecordId) {

        try {
            String attendeeWhereClause = OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " = ?";
            String[] attendeeWhereArgs = new String[]{String.valueOf(trainingRecordId)};
            db.delete(OFISDBContract.TrainingAttendeeRelations.TABLE_NAME, attendeeWhereClause, attendeeWhereArgs);

        }catch (Exception e){}
    }

    public Cursor getViewModules() {
        Cursor cursor = db.rawQuery("SELECT modules.id as id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, " +
                " modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th, moduleassignments.view_module, " +
                " moduleassignments.moduleassignment_type, moduleassignments.moduleassignment_id,"+
                " moduleassignments.isFarmSurvey FROM moduleassignments  " +
                " JOIN modules ON moduleassignments.module_id = modules.id  " +
                " WHERE moduleassignments.childLabour != 'true' AND moduleassignments.isFarmSurvey == 'false'  AND moduleassignments.view_module = '1'",null);
        return cursor;
    }

    public ArrayList<ModuleDTO> viewModuleList() {
        ArrayList<ModuleDTO> list= new ArrayList<ModuleDTO>();
        Cursor moduleList = getViewModules();
        ModuleDTO moduleDTO;
        if (moduleList.getCount() > 0) {
            while (moduleList.moveToNext()) {
                moduleDTO = new ModuleDTO();
                String moduleType = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_TYPE));
                String assignmentId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_ID));
                String moduleId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_ID));
                String moduleName = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME));
                moduleDTO.setName_lo(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_LO)));
                moduleDTO.setName_es(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ES)));
                moduleDTO.setName_fr(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_FR)));
                moduleDTO.setName_id(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ID)));
                moduleDTO.setName_pt(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_PT)));
                moduleDTO.setName_th(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TH)));
                moduleDTO.setName_tr(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TR)));
                moduleDTO.setName_vi(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_VI)));
                moduleDTO.setModule_id(moduleId);
                moduleDTO.setName(moduleName);
                moduleDTO.setModuleassignment_type(moduleType);
                moduleDTO.setModuleassignment_id(assignmentId);
                if (moduleType.equalsIgnoreCase("Country") && isValueExist(assignmentId, OFISDBContract.CountriesEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("Region") && isValueExist(assignmentId, OFISDBContract.RegionsEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("District") && isValueExist(assignmentId, OFISDBContract.DistrictsEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("Place") && isValueExist(assignmentId, OFISDBContract.PlacesEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("Farmer Group") && isValueExist(assignmentId, OFISDBContract.FarmerGroupsEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("Section") && isValueExist(assignmentId, OFISDBContract.SectionsEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                } else if (moduleType.equalsIgnoreCase("Product") && isValueExist(assignmentId, OFISDBContract.ProductsEntry.TABLE_NAME)) {
                    list.add(moduleDTO);
                }

            }
        }
        return list;
    }

    public boolean isValueExist(String id,String table) {
        Cursor cursor = db.query(table, new String[]{"*"}, "id = ?",
                new String[]{id}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isSurveyAnsDownloaded(int submoduleId, long farmerId, String stakeholderId) {
        String[] columns = new String[]{"*"};

        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String whereClause = OFISDBContract.SavedAnswerEntry.COLUMN_NAME_SUBMODULE_ID + " = ? AND "
                + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID +" = ? AND " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition;

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{String.valueOf(submoduleId),String.valueOf(farmerId)};
        } else {
            whereArgs = new String[]{String.valueOf(submoduleId),String.valueOf(farmerId), stakeholderId};
        }
        Cursor cursor = db.query(OFISDBContract.SavedAnswerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isSurveyAnsDownloadedByModuleId(int moduleId, long farmerId, String stakeholder) {
        boolean exists = false;
        Cursor subModules = getSubmodulesByModuleId(moduleId);
        try {
            while (subModules.moveToNext()) {
                int id = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
                boolean data = isSurveyAnsDownloaded(id,farmerId, stakeholder);
                if(data){
                    exists = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            subModules.close();
        }
        return exists;
    }

    public ArrayList<ModuleDTO> removeDuplicateModule(ArrayList<ModuleDTO> list) {
        ArrayList<ModuleDTO> finallist = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();
        for(int i=0; i<list.size();i++){
            if(! ids.contains(list.get(i).getModule_id())){
                ids.add(list.get(i).getModule_id());
                finallist.add(list.get(i));
            }
        }
        return finallist;
    }

    public ArrayList<Integer> getAssignedFGs(String moduleId) {
        ArrayList<Integer> fgIds = new ArrayList<>();
        Cursor nids =  db.query(OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME, new String[]{"*"}, "module_id = ? AND moduleassignment_type = ? ",
                new String[]{moduleId,"Farmer Group"}, null, null, null);
        try {
            while (nids.moveToNext()) {
                int val = nids.getInt(nids.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_ID));
                fgIds.add(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }
        return fgIds;
    }

    public ArrayList<Integer> getSectionFGs(ArrayList<Integer> sectionIds) {
        String result = null;
        ArrayList<Integer> fgIds = new ArrayList<>();
        if (sectionIds.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int s : sectionIds) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        Cursor nids = db.rawQuery("SELECT * FROM " + OFISDBContract.SectionsEntry.TABLE_NAME +
                " WHERE " + OFISDBContract.SectionsEntry.COLUMN_NAME_ID
                + " IN (" + result + ")"  ,null);
        try {
            while (nids.moveToNext()) {
                int val = nids.getInt(nids.getColumnIndex(OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID));
                fgIds.add(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }
        return fgIds;
    }

    public ArrayList<Integer> getAssignedSections(String moduleId) {
        ArrayList<Integer> ids = new ArrayList<>();
        Cursor nids =  db.query(OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME, new String[]{"*"}, "module_id = ? AND moduleassignment_type = ? ",
                new String[]{moduleId,"Section"}, null, null, null);
        try {
            while (nids.moveToNext()) {
                int val = nids.getInt(nids.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_ID));
                ids.add(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }
        return ids;
    }

    public boolean isAssignedwithtype(String moduleId, String type) {
        boolean status = false;
        Cursor nids =  db.query(OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME, new String[]{"*"}, "module_id = ? AND moduleassignment_type = ? ",
                new String[]{moduleId,type}, null, null, null);
        try {
            while (nids.moveToNext()) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nids.close();
        }
        return status;
    }

    public void addDownloadedSurveyList(AnswerRequestDTO answerRequestDTO) {
        try {
            String table = OFISDBContract.DownloadedSurveyList.TABLE_NAME;
            String[] whereArgs = new String[]{
                    answerRequestDTO.getFarmerGroupId(),answerRequestDTO.getSectionId(),answerRequestDTO.getModule_id()
            };
            String whereClause = OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP + " = ? AND " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION + " = ? AND "
                    + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID + " = ? ";
            db.delete(table, whereClause, whereArgs);

            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String date = format.format(today);
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID, answerRequestDTO.getModule_id());
            values.put(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_NAME, answerRequestDTO.getModule_name());
            values.put(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP, answerRequestDTO.getFarmerGroupId());
            values.put(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION, answerRequestDTO.getSectionId());
            values.put(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SAVED_DATE, date);
            db.insert(OFISDBContract.DownloadedSurveyList.TABLE_NAME, null, values);
        }catch (Exception e){e.printStackTrace();}
    }

    public ArrayList<AnswerRequestDTO> getDownloadedSurveyList(String moduleId) {
        ArrayList<AnswerRequestDTO> list= new ArrayList<AnswerRequestDTO>();
        try {
            Cursor moduleList=db.rawQuery("SELECT * FROM " + OFISDBContract.DownloadedSurveyList.TABLE_NAME
                    + " WHERE " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID
                    + " = " + moduleId + " ORDER BY " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SAVED_DATE + " DESC", null);
            AnswerRequestDTO moduleDTO;
            if (moduleList.getCount() > 0) {
                while (moduleList.moveToNext()) {
                    moduleDTO = new AnswerRequestDTO();
                    String farmerGroupId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP));
                    String sectionId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION));
                    String moduleIdval = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID));
                    moduleDTO.setModule_id(moduleIdval);
                    moduleDTO.setModule_name(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_NAME)));
                    moduleDTO.setFarmerGroupId(farmerGroupId);
                    moduleDTO.setSectionId(sectionId);
                    moduleDTO.setListedName(getFGName(Integer.parseInt(farmerGroupId)) + "-" +
                            getSectionName(Integer.parseInt(sectionId)) +
                            "(Farmers:" + farmerCountByModuleSectionIds(moduleIdval,sectionId) + ")");
                    moduleDTO.setDateTime(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SAVED_DATE)));
                    if(getFGName(Integer.parseInt(farmerGroupId))!= "NA" &&
                            getSectionName(Integer.parseInt(sectionId)) != "NA" &&
                            farmerCountByModuleSectionIds(moduleIdval,sectionId) > 0){
                        list.add(moduleDTO);
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public int farmerCountByModuleSectionIds(String moduleId,String sectionId) {
        Cursor data = db.rawQuery("SELECT DISTINCT farmer_id FROM savedAnswers a inner join farmers b on a.farmer_id = b.id where a.farmer_module_id = "+ moduleId +" and b.section_id ="+ sectionId, null);
        return data.getCount();
    }

    public void deleteDownloadedSurveys(String fgId, String sectionId, String moduleId) {
        String deleteAnswersRecord = "DELETE FROM " + OFISDBContract.SavedAnswerEntry.TABLE_NAME + " WHERE "
                + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " =" + moduleId + " AND "
                + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID
                + " IN ( SELECT " + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " FROM " + OFISDBContract.FarmerEntry.TABLE_NAME
                + " WHERE " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID + " =" + fgId
                + " AND " + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID+ " =" + sectionId
                + ");";
        String deleteSurveyRecord = "DELETE FROM " + OFISDBContract.DownloadedSurveyList.TABLE_NAME
                + " WHERE " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP + " =" + fgId
                + " AND " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID+ " =" + moduleId
                + " AND " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION+ " =" + sectionId + ";";
        db.execSQL(deleteAnswersRecord);
        db.execSQL(deleteSurveyRecord);
    }

    public Cursor retrieveQuestionsAndAnswersBySubModuleId(int submoduleId, long farmerId, String stakeholderId, long farmerModuleId) {
        Log.d(TAG, "retrieveQuestionsAndAnswersBySubModuleId");
        String questionTable = OFISDBContract.QuestionEntry.TABLE_NAME;
        String datatypeTable = OFISDBContract.DatatypesEntry.TABLE_NAME;
        String answerTable = OFISDBContract.SavedAnswerEntry.TABLE_NAME;
        String columns = questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + ", " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ID + " as answer_id, "
                + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " as datatype_name, " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + ", " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + ", "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + ", "
                + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_INT + ", " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_BOOL + ", "
                + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + ", " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_DATE + ", "
                + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_TEXT + ", " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR;


        String stakeholderCondition = stakeholderId == null ? " IS NULL " : " = ? " ;
        String farmerQuery = answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + stakeholderCondition ;

        String farmerQueryValue = String.valueOf(farmerId);

        String sql = "SELECT " + columns + " FROM " + questionTable + " LEFT OUTER JOIN " + answerTable
                + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " = " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_QUESTION_ID + " AND "
//                + farmerQuery + " AND " + answerTable + "." + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " = ? INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
                + farmerQuery + " INNER JOIN " + datatypeTable + " ON " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID
//                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ?  AND " + OFISDBContract.SavedAnswerEntry.COLUMN_IS_SYNCED + " is not NULL GROUP BY "
                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? GROUP BY "
//                + " = " + datatypeTable + "." + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " WHERE " + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ?  AND " +  " answers.isSynced = 1" + " GROUP BY "
//        AND answers.isSynced is not NULL OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " = ? GROUP BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " ORDER BY "
                + questionTable + "." + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " ASC";

        String[] whereArgs;
        if (stakeholderId == null) {
            whereArgs = new String[]{
                    farmerQueryValue,
//                String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId)
            };
        } else {
            whereArgs = new String[]{
                    farmerQueryValue, stakeholderId,
//                String.valueOf(farmerModuleId),
                    String.valueOf(submoduleId)
            };
        }


        return db.rawQuery(sql, whereArgs);
    }

    public String getIdByFarmerId(long farmerId, String type) {

        String productId = null;
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID+ " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerId)};
        Cursor mCursor= db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
        try {
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    if(type == "Product") {
                        productId = String.valueOf(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID)));
                    }else if(type == "Country"){
                        productId = String.valueOf(mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));
                    }
                }
            }
        }catch (Exception e){

        }finally {
            mCursor.close();
        }
        return productId;
    }

    public ArrayList<Integer> getFDPNotificationFarmers() {
        ArrayList<Integer> notificationList = new ArrayList<>();
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.NotificationAssignments.COLUMN_MODULEID + " = ? ";
        String[] whereArgs = new String[]{"1"};
        String orderBy = OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT ;
        Cursor nIds = db.query(OFISDBContract.NotificationAssignments.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);

        while (nIds.moveToNext()) {
            notificationList.add(nIds.getInt(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID)));
        }
        return notificationList;
    }

    public ArrayList<NotificationDTO> getSmartNotificationList(String moduleName) {
        ArrayList<NotificationDTO> notificationList = new ArrayList<>();
        String query = " select n.* from " + OFISDBContract.NotificationAssignments.TABLE_NAME + " n INNER JOIN " + OFISDBContract.NotificationModuleAssignments.TABLE_NAME + " m ON m.id = n.moduleId " + " WHERE m.moduleName = ? AND n.status != 'D' ORDER BY " + OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT + " DESC ";
        String[] params = new String[]{moduleName};
        Cursor nIds = db.rawQuery(query, params);

        if (nIds == null) {
            return notificationList;
        }

        try {
            while (nIds.moveToNext()) {
                NotificationDTO notificationDTO = new NotificationDTO();
                notificationDTO.setId(nIds.getInt(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_NAME_ID)));
                notificationDTO.setChildId(nIds.getInt(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID)));
                notificationDTO.setFarmerId(nIds.getInt(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID)));
                notificationDTO.setModuleId(nIds.getInt(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_MODULEID)));
                notificationDTO.setCreatedAt(nIds.getString(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT)));
                notificationDTO.setStatus(nIds.getString(nIds.getColumnIndex(OFISDBContract.NotificationAssignments.COLUMN_STATUS)));
                notificationList.add(notificationDTO);
            }
        }
        finally {
            nIds.close();
        }

        return notificationList;
    }

    public String getMessageByModuleId(int moduleId) {
        String message="";
        Cursor moduleCursor=null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(moduleId)};
            moduleCursor = db.query(OFISDBContract.NotificationModuleAssignments.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (moduleCursor != null) {
                while (moduleCursor.moveToNext()) {
                    message = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_MSG));
                }
            }
        } catch (Exception e) {
        }finally {
            moduleCursor.close();
        }
        return  message;
    }

    public String getShowDescriptionModule(int moduleID , int subModuleID){
        String showDescription = "";
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"showDescription"};
            String whereClause = OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID + " = ? AND "+ OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " =? ";
            String[] whereArgs = new String[]{String.valueOf(subModuleID), String.valueOf(moduleID)};
            cursor = db.query(OFISDBContract.SubmoduleEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null , null);
            if (cursor!=null){
                while (cursor.moveToNext()){
                    showDescription = cursor.getString(cursor.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_SHOW_DESCRIPTION));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return showDescription;
    }

    public String getDescriptionByModuleId(int moduleId ,String lang) {
        String description = "";
        Cursor moduleCursor = null;
        try{
            String[] columns = null;
            if (Objects.equals(lang, "en")){
                columns = new String[]{"description"};
            } else if (Objects.equals(lang, "in")) {
                columns = new String[]{"descriptionId"};
            } else {
                columns = new String[]{"description"+lang};
            }

            String whereClause = OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(moduleId)};
            moduleCursor = db.query(OFISDBContract.ModuleEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null , null);
            if (moduleCursor != null){
                while (moduleCursor.moveToNext()){
                    if (Objects.equals(lang, "en")){
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION));
                    } else if (Objects.equals(lang , "es")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_ES));
                    }else if (Objects.equals(lang , "fr")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_FR));
                    }else if (Objects.equals(lang , "id") || Objects.equals(lang , "in")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_ID));
                    }else if (Objects.equals(lang , "lo")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_LO));
                    }else if (Objects.equals(lang , "pt")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_PT));
                    }else if (Objects.equals(lang , "th")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_TH));
                    }else if (Objects.equals(lang , "tr")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_TR));
                    }else if (Objects.equals(lang , "vi")) {
                        description = moduleCursor.getString(moduleCursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_VI));
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            moduleCursor.close();
        }

        return description;
    }


    public void removeNotificationModule(int moduleId) {
        String table = OFISDBContract.NotificationAssignments.TABLE_NAME;
        String[] whereArgs = new String[]{ String.valueOf(moduleId) };
        String whereClause = OFISDBContract.NotificationAssignments.COLUMN_NAME_ID + " = ? ";
//        db.delete(table, whereClause, whereArgs);
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.NotificationAssignments.COLUMN_STATUS, Utilities.DELETED);
        db.update(table,values, whereClause, whereArgs);
    }


    public void deleteNotificationByFarmerId(ArrayList<Integer> farmerList, String column) {
        for(long farmerId : farmerList) {
            String table = OFISDBContract.NotificationAssignments.TABLE_NAME;
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            String whereClause = column + " = ? ";
            db.delete(table, whereClause, whereArgs);
        }
    }

    public void deleteCLMRSNotification() {
        String table = OFISDBContract.NotificationAssignments.TABLE_NAME;
        String[] whereArgs = new String[]{"2"};
        String whereClause = OFISDBContract.NotificationAssignments.COLUMN_MODULEID + " = ? ";
        db.delete(table, whereClause, whereArgs);
    }


    public int getNotificationCount() {
        int count = 0;
        try {
            String table = OFISDBContract.NotificationAssignments.TABLE_NAME;
            String[] whereArgs = new String[]{ Utilities.DELETED };
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.NotificationAssignments.COLUMN_STATUS + " != ? ";
            Cursor cursor = db.query(table, columns, whereClause, whereArgs, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getNotificationCountByModule(String moduleName) {
        int count = 0;
        try {
            String query = " select * from " + OFISDBContract.NotificationAssignments.TABLE_NAME + " n INNER JOIN " + OFISDBContract.NotificationModuleAssignments.TABLE_NAME + " m ON m.id = n.moduleId " + " WHERE m.moduleName = ? AND n.status != 'D' ";
            String[] params = new String[]{moduleName};
            Cursor cursor = db.rawQuery(query, params);
            if (cursor != null) {
                count = cursor.getCount();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }


    public void updateNotificationModule(int farmerId, int moduleId, int childId, Map<String, String> columns) {
        String table = OFISDBContract.NotificationAssignments.TABLE_NAME;
        String[] whereArgs = new String[]{ String.valueOf(farmerId), String.valueOf(moduleId), String.valueOf(childId)};
        String whereClause = OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID + " = ? AND " + OFISDBContract.NotificationAssignments.COLUMN_MODULEID + " = ? AND " + OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID + " = ?";
        ContentValues values = new ContentValues();

        for (Map.Entry<String,String> entry : columns.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }
        db.update(table,values, whereClause, whereArgs);
    }


    public void updateNqForDMSurvey(String farmerId, String moduleId, String type, String moduleName, int plotId, String location){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        boolean updateWaiting = networkEntryTypeExistsByFarmerModuleId(type, farmerId, moduleId, plotId);
        String moduleType = plotId != 0 ? res.getString(R.string.dm_plot_survey) : res.getString(R.string.diagnostic_survey);
        if (updateWaiting) {
            ContentValues nqiValues = new ContentValues();
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, location);
            String[] whereArgsFarm = new String[]{farmerId,type, String.valueOf(plotId), moduleId};
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, nqiValues, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID + " = ? ", whereArgsFarm);
        }else {
            ContentValues networkQueueValues = new ContentValues();
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, type);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, moduleName);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, moduleType);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID, moduleId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, getFarmerNamefromDb(Long.valueOf(farmerId)));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID, plotId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, location);
            insertNetworkQueueEntry(networkQueueValues);
        }
    }

    public boolean networkEntryTypeExistsByFarmerModuleId(String submission_type, String farmerId, String moduleId, int plotId) {
        String[] columns = new String[]{"id"};
        String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND "
                + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND "
                + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID + " = ? AND "
                + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " = ?";
        String[] whereArgs = new String[]{
                submission_type,
                farmerId,
                moduleId,
                String.valueOf(plotId)
        };

        Cursor nqi = null;
        int nqicount = 0;
        try {
            nqi = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            nqicount = nqi.getCount();
        } finally {
            if (nqi != null)
                nqi.close();
        }
        return (nqicount > 0);
    }

    public Cursor getCertificationsbyfarmerid(long farmerid) {
        String[] columns = new String[]{OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID,OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT};
        String whereClause = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(farmerid)};
        String orderBy = OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " ASC";
        return db.query(OFISDBContract.FarmerEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, orderBy);
    }

    public Cursor getInfoFromTable(String tableName){
        return db.rawQuery("SELECT id, name FROM " + tableName, null);
    }

    public Cursor searchFarmerList(String query, long selectedCountry, long selectedProduct,
                                   List<Integer> selectedRegions, List<Integer> selectedDistricts,
                                   List<Integer> selectedPlaces, List<Integer> selectedPartners,
                                   List<Integer> selectedProgrammes, List<Integer> selectedFarmerGroups,
                                   List<Integer> selectedSections, List<Integer> selectedCertifications
    ) {
        String whereClause = "";
        String farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME;
        if (query.length() > 0) {
            whereClause = " WHERE " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " LIKE '%" + query + "%' OR " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + " LIKE '%" + query + "%' OR " +
                    farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " LIKE '%" + query + "'";
        }
        if (selectedCountry > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + " = " + selectedCountry;
        }
        if (selectedProduct > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID + " = " + selectedProduct;
        }
        if (selectedRegions.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String regions = selectedRegions.toString().replace("[", "").replace("]", "");
            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID + " IN (" + regions +")";
        }
        if (selectedSections.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String sections = selectedSections.toString().replace("[", "").replace("]", "");
            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID + " IN( " + sections +")";
        }
        if (selectedDistricts.size()> 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String districts = selectedDistricts.toString().replace("[", "").replace("]", "");

            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID
                    + " IN( " + districts + ")";
        }
        if (selectedPlaces.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String places = selectedPlaces.toString().replace("[", "").replace("]", "");

            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID
                    + " IN( " + places + ")";
        }
        if (selectedPartners.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String partners = selectedPartners.toString().replace("[", "")
                    .replace("]", "");

            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID
                    + " IN( " + partners + ")";
        }
        if (selectedProgrammes.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String programmes = selectedProgrammes.toString().replace("[", "")
                    .replace("]", "");

            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID
                    + " IN( " + programmes + ")";
        }
        if (selectedFarmerGroups.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String farmerGroups = selectedFarmerGroups.toString().replace("[", "")
                    .replace("]", "");
            whereClause += farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID
                    + " IN( " + farmerGroups + ")";
        }
        if (selectedCertifications.size() > 0) {
            if (whereClause.length() == 0) {
                whereClause += " WHERE ";
            } else {
                whereClause += " AND ";
            }
            String[] certifications = selectedCertifications.toString().replace("[", "")
                    .replace("]", "").replace(" ", "").split(",");
            whereClause += "(" + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID
                    + " LIKE '%";

            for (int i = 0; i < certifications.length; i++) {
                if (i == certifications.length - 1) {
                    whereClause += certifications[i] + "%')";
                } else {
                    whereClause += certifications[i] + "%' OR " + farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID
                            + " LIKE '%";
                }
            }
        }

        String sql =  "select distinct(farmers.id) as _id, " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + ", " +
                farmerTable + "." + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + " from " +
                farmerTable + whereClause + " order by " + farmerTable + "."
                + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " ASC";

        return db.rawQuery(sql, null);
    }

    public void updateSurveyCapturedForPlots(Map<Boolean, List<Integer>> submittedPlots) {
        for (Map.Entry<Boolean, List<Integer>> entry : submittedPlots.entrySet()) {
            String columnName = entry.getKey() ? OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED : OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED;
            List<Integer> plotIds = entry.getValue();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < plotIds.size(); i++) {
                sb.append("?");
                if (i < plotIds.size() - 1) {
                    sb.append(",");
                }
            }
            String whereClause = "id IN (" + sb + ")";

            String[] whereArgs = new String[plotIds.size()];
            for (int i = 0; i < plotIds.size(); i++) {
                whereArgs[i] = String.valueOf(plotIds.get(i));
            }

            ContentValues values = new ContentValues();
            values.put(columnName, "true");

            db.update(OFISDBContract.PlotEntry.TABLE_NAME, values, whereClause, whereArgs);
        }
    }

    public int fetchSyncedPlotCountByFarmerIdAndCleansedStatus(String id) {
        String[] columns = new String[]{"*"};
        String whereClause = OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " = ? AND ("
                + "LOWER(" + OFISDBContract.PlotEntry.COLUMN_NAME_IS_CLEANSED_STATUS + ") = ? OR "
                + "LOWER(" + OFISDBContract.PlotEntry.COLUMN_NAME_IS_CLEANSED_STATUS + ") = ?) AND "
                + OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id), "updated", "repaired", "1"};
        Cursor data = db.query(OFISDBContract.PlotEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

        while (data.getCount() > 0 && data.moveToNext()) {
            boolean isDeforestationSurveyCaptured = data.getString(data.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED)).equalsIgnoreCase("true");
            boolean isPaLegalitySurveyCaptured = data.getString(data.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED)).equalsIgnoreCase("true");
            boolean isDeforestationPlot = data.getString(data.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_DEFORESTATION_PLOT)).equalsIgnoreCase("false");
            boolean isPaLegalityPlot = data.getString(data.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_PLOT)).equalsIgnoreCase("false");


            if (isDeforestationPlot || isPaLegalityPlot ||isDeforestationSurveyCaptured || isPaLegalitySurveyCaptured) {
                return 1;
            }
        }
        return 0;

    }

    public void updatePlotFlagsAndSurveyCapture(boolean isDeforeStationSurvey, int plotId) {
        String flagColumn = isDeforeStationSurvey ? OFISDBContract.PlotEntry.COLUMN_NAME_IS_DEFORESTATION_PLOT : OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_PLOT;
        String surveyColumn = isDeforeStationSurvey ? OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED : OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED;

        ContentValues values = new ContentValues();
        values.put(flagColumn, "true");
        values.put(surveyColumn, "true");

        db.update(OFISDBContract.PlotEntry.TABLE_NAME, values, "id = ?", new String[] {String.valueOf(plotId)});

    }

    public boolean getCompositeFlagByCountryId(int countryId) {
        Cursor farmerAssignments = getassignmentbyCountryId(countryId);

        try {
            if (farmerAssignments != null) {
                farmerAssignments.moveToFirst();
                return farmerAssignments.getString(farmerAssignments.getColumnIndex(OFISDBContract.FarmerAssignments.CALCULATE_COMPOSITE_FLAG)).equalsIgnoreCase("true");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            farmerAssignments.close();
        }

        return false;
    }

    public List<StakeHolderDto> getAllStakeHolderListByFarmerId(Long farmerId) {
        return ofisRoomDB.multiStakeHolderDao().getStakeHoldersByFarmerId(farmerId);
    }

    public String getDiallingCodeByFarmerId(long  farmerId) {
        Integer countryId = getCountryByFarmerId(farmerId);

        String selectDialingCodeQuery = "SELECT dialling_code FROM countries WHERE id = ?";
        Cursor countries = db.rawQuery(selectDialingCodeQuery, new String[]{String.valueOf(countryId)});

        String dialingCode = "";
        if (countries.moveToFirst()) {
            dialingCode = countries.getString(0);
            countries.close();
        }
        return dialingCode;
    }

    public Integer getCountryByFarmerId(long  farmerId) {
        String selectQuery = "SELECT country_id FROM farmers WHERE id = ?";
        Cursor farmer = db.rawQuery(selectQuery, new String[]{String.valueOf(farmerId)});

        Integer countryId = null;
        if (farmer.moveToFirst()) {
            countryId = farmer.getInt(0);
            farmer.close();
        }
        return countryId;
    }

    public List<MultiFarmRoleDTO> getMultiFarmRoles(String locale) {
        List<MultiFarmRoleDTO> multiFarmRoleDTOList = new ArrayList<MultiFarmRoleDTO>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID + "," + locale + " as name " + " FROM multifarmRoles ", null);
        } catch (Exception e) {
            cursor = db.rawQuery("select * from multifarmRoles", null);
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        MultiFarmRoleDTO multiFarmRoleDTO = new MultiFarmRoleDTO();
                        multiFarmRoleDTO.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID))));
                        multiFarmRoleDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME)));
                        multiFarmRoleDTOList.add(multiFarmRoleDTO);
                    }
                }
            } catch (Exception e) {

            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return multiFarmRoleDTOList;
    }

    public void updateNqForStakeHolder(String farmerId, String stakeHolderId, String stakeHolderPhoto,String stakeHolderName, String olamStakeHolderId){

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ContentValues networkQueueValues = new ContentValues();
        boolean updateWaiting = networkEntryTypeExistsByFarmerId("update stakeholder", Long.parseLong(stakeHolderId), OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID);
        if (updateWaiting) {
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            if (stakeHolderPhoto.equalsIgnoreCase("true")) {
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_PHOTO, stakeHolderPhoto);
            }
            String[] whereArgsFarm = new String[]{farmerId, "update stakeholder", String.valueOf(stakeHolderId)};
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, networkQueueValues, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID + " = ?", whereArgsFarm);
        }
        else {
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "update stakeholder");
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.update_stakeholder));
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, stakeHolderName);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamStakeHolderId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, stakeHolderId);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_PHOTO, stakeHolderPhoto);
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            insertNetworkQueueEntry(networkQueueValues);
        }
    }

    public void insertOnGroundSurveyNotificationData() {
        List<Integer> farmerGroups = getComplianceFarmerGroups();
        for (int farmerGroupId : farmerGroups) {
            List<String> surveyNames = ofisRoomDB.dmMainModuleDao().getDiagnosticSurveysByFarmerGroupId(farmerGroupId);

            if (surveyNames.contains("On Ground Verification Survey")) {
                getPendingEUDRGroundVerificationSurveyData(farmerGroupId, Constants.EUDR);
            }

            if (surveyNames.contains("PA Legality Survey")) {
                getPendingEUDRGroundVerificationSurveyData(farmerGroupId, Constants.PA);
            }
        }

    }

    public void getPendingEUDRGroundVerificationSurveyData(int farmerGroupId, String surveyType) {
        String flagColumn = surveyType.equals(Constants.EUDR) ? OFISDBContract.PlotEntry.COLUMN_NAME_IS_DEFORESTATION_PLOT : OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_PLOT;
        String surveyCaptured = surveyType.equals(Constants.EUDR) ? OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED : OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED;
        String geoDbFlagStatus = surveyType.equals(Constants.EUDR) ? "DF%" : "%PA";
        String query = """
                SELECT DISTINCT f.*, p.id AS plotId FROM plot p INNER JOIN farmers f ON f.id = p.farmerId 
                WHERE f.complainceFlag = 'false'
                AND f.farmerGroupId = ?
                AND (p.cleansedStatus LIKE 'updated' OR p.cleansedStatus LIKE 'repaired')
                AND (p.geoDbFlagStatus LIKE '""";

        query += geoDbFlagStatus + "' AND (p." + flagColumn +"= 'false' AND p." + surveyCaptured + "= 'false'))";

        String[] params = new String[]{String.valueOf(farmerGroupId)};
        Cursor cursor = db.rawQuery(query, params);

        try {
            List<OnGroundSurveyNotification> onGroundSurveyNotifications = new ArrayList<>();
            while (cursor.moveToNext()) {
                OnGroundSurveyNotification onGroundSurveyNotification = new OnGroundSurveyNotification();
                onGroundSurveyNotification.farmerGroupId = farmerGroupId;
                onGroundSurveyNotification.farmerId = cursor.getLong(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                onGroundSurveyNotification.farmerName = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
                onGroundSurveyNotification.photo = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                onGroundSurveyNotification.olamFarmerId = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
                onGroundSurveyNotification.createdOnPhone = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY));
                onGroundSurveyNotification.countryId = cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID));
                onGroundSurveyNotification.odFarmerId = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID));
                onGroundSurveyNotification.plotId = cursor.getLong(cursor.getColumnIndex("plotId"));
                onGroundSurveyNotification.surveyType = surveyType;
                onGroundSurveyNotification.sectionId = cursor.getLong(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID));
                onGroundSurveyNotifications.add(onGroundSurveyNotification);
            }
            ofisRoomDB.onGroundSurveyNotificationDao().insertAll(onGroundSurveyNotifications);
        }
        catch (Exception e) {
            e.printStackTrace();
            cursor.close();
        }
    }
    public boolean getIsConsentAvailableToSync(long farmerId) {
        Cursor networkEntry = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId), "consentstatement"};
            networkEntry = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

            if (networkEntry != null && networkEntry.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (networkEntry != null) {
                networkEntry.close();
            }
        }
        return false;
    }



    public List<Integer> getComplianceFarmerGroups() {
        String query = """
                    SELECT DISTINCT f.id FROM farmergroups f
                    INNER JOIN assignments a ON a.productId = f.product_id
                    WHERE a.isCompliance = 'true'
                """;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null) {
            return Collections.emptyList();
        }

        List<Integer> farmerGroupsId = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    farmerGroupsId.add(cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID)));
                } while(cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return farmerGroupsId;
    }

    public Cursor getComplianceFarmerGroupsCursor(List<Integer> farmerGroupId) {
        String query = """
                    SELECT DISTINCT f.name, f.id AS _id, f.code, f.programme_id, f.partner_id, f.product_id FROM farmergroups f
                    INNER JOIN assignments a ON a.productId = f.product_id
                    WHERE f.id IN (""";

        String placeholders = String.join(",", Collections.nCopies(farmerGroupId.size(), "?"));
        query += placeholders + ")";

        String[] farmerGroupIdArray = new String[farmerGroupId.size()];
        for (int i = 0; i < farmerGroupId.size(); i++) {
            farmerGroupIdArray[i] = String.valueOf(farmerGroupId.get(i));
        }
        return db.rawQuery(query, farmerGroupIdArray);
    }

    public Cursor getSectionByFarmerGroup(List<Integer> farmerGroupId) {
        String query = """
                    SELECT DISTINCT s.name, s.id AS _id, s.code FROM sections s
                    WHERE s.farmergroup_id IN (""";

        String placeholders = String.join(",", Collections.nCopies(farmerGroupId.size(), "?"));
        query += placeholders + ")";

        String[] farmerGroupIdArray = new String[farmerGroupId.size()];
        for (int i = 0; i < farmerGroupId.size(); i++) {
            farmerGroupIdArray[i] = String.valueOf(farmerGroupId.get(i));
        }
        return db.rawQuery(query, farmerGroupIdArray);
    }
    public Collection<Integer> getAllUnSyncedModule(long farmerId) {
        Collection<Integer> moduleIds = new ArrayList<>();
        Collection<Integer> subModuleIds = new ArrayList<>();

        // Query to get distinct subModuleIds based on farmerId
        String query = "SELECT DISTINCT " + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " FROM " + OFISDBContract.AnswerEntry.TABLE_NAME + " WHERE " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " = ? ";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(farmerId)});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID));
                subModuleIds.add(id);
            }
            cursor.close();
        }

        if (!subModuleIds.isEmpty()) {
            StringBuilder subModuleIdsString = new StringBuilder();
            for (Integer subModuleId : subModuleIds) {
                if (subModuleIdsString.length() > 0) {
                    subModuleIdsString.append(",");
                }
                subModuleIdsString.append(subModuleId);
            }

            String moduleQuery = "SELECT DISTINCT " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " FROM " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " WHERE " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID + " IN (" + subModuleIdsString.toString() + ")";
            Cursor moduleCursor = db.rawQuery(moduleQuery, null);

            if (moduleCursor.getCount() > 0) {
                while (moduleCursor.moveToNext()) {
                    int moduleId = moduleCursor.getInt(moduleCursor.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID));
                    boolean moduleFinished = moduleFinishedCheck(moduleId, farmerId);
                    if (moduleFinished) {
                        moduleIds.add(moduleId);
                    }
                }
                moduleCursor.close();
            }
        }
        return moduleIds;
    }
    private boolean moduleFinishedCheck(Integer moduleId, long farmerId) {
        boolean statusFinished = false;
        Integer status = null;
        String moduleQuery = "SELECT " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED + " FROM " + OFISDBContract.FarmerModulesEntry.TABLE_NAME + " WHERE " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID  + " = ? AND " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID  + " = ? ";
        Cursor cursor = db.rawQuery(moduleQuery, new String[]{String.valueOf(farmerId), String.valueOf(moduleId)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                status = cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED));
            }
            cursor.close();
        }

        if (status!=null && Objects.equals(status,1)){
            statusFinished = true;
        }
        return statusFinished;
    }

    public Cursor getAllModuleByFarmerId(long id) {
        Cursor farmer = null;

        try {
            farmer = getFlattenedFarmer(id);
            Cursor output = null;

            if (farmer.getCount() > 0) {
                farmer.moveToFirst();

                String countryId = farmer.getString(farmer.getColumnIndex("country_id"));
                String regionId = farmer.getString(farmer.getColumnIndex("region_id"));
                String districtId = farmer.getString(farmer.getColumnIndex("district_id"));
                String placeId = farmer.getString(farmer.getColumnIndex("place_id"));
                String farmerGroupId = farmer.getString(farmer.getColumnIndex("farmergroup_id"));
                String sectionId = farmer.getString(farmer.getColumnIndex("section_id"));
                String programmeId = farmer.getString(farmer.getColumnIndex("programme_id"));
                String productId = farmer.getString(farmer.getColumnIndex("product_id"));
                String partnerId = farmer.getString(farmer.getColumnIndex("partner_id"));

                String modulesQuery = "SELECT distinct modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                        "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                        "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type, moduleassignments.disabled_months, moduleassignments.startDate, moduleassignments.isFarmSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId,moduleassignments.farmerRoleId, " +
                        " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration, modules.isMandate FROM moduleassignments " +
                        " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                        " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                        "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                        " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                        "moduleassignments.childLabour != 'true' AND " +
                        "moduleassignments.isPlotSurvey != 'true' AND "+
                        "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                        "((moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) group by modules.id";

                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        countryId,
                        countryId,
                        productId,
                        regionId,
                        districtId,
                        placeId,
                        farmerGroupId,
                        sectionId,
                        programmeId,
                        partnerId
                };
                @SuppressLint("Recycle")
                Cursor farmerModules = getfarmerModules(modulesQuery, modulesQueryValues);

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
                        "startDate",
                        "isFarmSurvey",
                        "view_module",
                        "fkFarmerRoleId",
                        "farmerRoleId",
                        "farmer_module_id",
                        "status_started",
                        "status_finished",
                        "current_submodule_id",
                        "recorded_lat",
                        "recorded_lng",
                        "surveyDuration",
                        "isMandate"
                };
                MatrixCursor mc = new MatrixCursor(mcColumns);
                Cursor[] cursors = {mc, farmerModules};
                output = new MergeCursor(cursors);
            }
            return output;
        } finally {
            farmer.close();
        }
    }

    public Set<Integer> getSurveyNQBasedOnFarmerId(long farmerId) {
        Cursor nqCursor=null;
        Set<Integer> surveyId = new HashSet<>();
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            nqCursor = db.query(OFISDBContract.NetworkQueueEntry.TABLE_NAME, columns, whereClause, whereArgs, null, null, null);
            if (nqCursor != null) {
                while (nqCursor.moveToNext()) {
                    surveyId.add(nqCursor.getInt(nqCursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            assert nqCursor != null;
            nqCursor.close();
        }
        return surveyId;
    }

    public void updateNqAndAnswerForStakeHolderModule(long localStakeholderId, int msId) {
        try {
            ContentValues values = new ContentValues();
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID, msId);
            String whereClause = OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID + " = ? AND " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(localStakeholderId), "module"};
            db.update(OFISDBContract.NetworkQueueEntry.TABLE_NAME, values, whereClause, whereArgs);

            ContentValues valuesAnswer = new ContentValues();
            values.put(OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID, msId);
            String whereClauseAnswer = OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " = ?";
            String[] whereArgsAnswer = new String[]{String.valueOf(localStakeholderId)};
            db.update(OFISDBContract.AnswerEntry.TABLE_NAME, valuesAnswer, whereClauseAnswer, whereArgsAnswer);

        } catch (Exception e) {
        }
    }


    public Boolean isSurveyMandate(long moduleId){
        boolean isMandate = false;
        Cursor cursor = null;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(moduleId)};
            cursor = db.query(OFISDBContract.ModuleEntry.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    isMandate = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_IS_MANDATE)));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return isMandate;
    }

    public void updateDuplicateStatusOfPhoneNumber(long localFarmerId) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DUPLICATE, "true");

        String[] whereArgs = new String[]{String.valueOf(localFarmerId), "1"};
        String whereClause = OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY + " = ? ";

        db.update(OFISDBContract.PhoneNumber.TABLE_NAME, values, whereClause, whereArgs);
    }

    public void updateDuplicateStatusOfNationalId(long localFarmerId, String typeId) {
        ContentValues values = new ContentValues();
        values.put(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DUPLICATE, "true");

        String[] whereArgs = new String[]{String.valueOf(localFarmerId), typeId};
        String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ? AND " + OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " = ? ";

        db.update(OFISDBContract.FarmerNationalIDs.TABLE_NAME, values, whereClause, whereArgs);
    }


    public boolean checkNationalIdDuplicateByFarmerId(long farmerId) {
        boolean isDuplicate = false;
        try {
            String[] columns = new String[]{"*"};
            String whereClause = OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(farmerId)};
            Cursor cursor = db.query(OFISDBContract.FarmerNationalIDs.TABLE_NAME , columns , whereClause , whereArgs , null , null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String isDuplicateData = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DUPLICATE));
                    if (isDuplicateData != null && Objects.equals(isDuplicateData,"true")) {
                        isDuplicate = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return isDuplicate;
    }
}
