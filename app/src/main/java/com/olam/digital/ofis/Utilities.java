package com.olam.digital.ofis;

import static org.apache.commons.lang3.time.DateUtils.isSameDay;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.Common.Constants.StringConstants;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyDto;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by bengallienne on 29/11/14.
 * Utility functions for use throughout the application
 */
public class Utilities {
    // update
    private static final String SHARED_PREFS_NAME = "com.olam.digital.ofis.app_prefs";
    private static final String SHARED_FIREBASE_TOKEN_UPDATED = "firebase_token_updated";
    public static final String HOUSEHOLD_ENUMERATOR = "household_enumerator";
    public static final String FARMERLEAD_ENUMERATOR = "farmerlead_enumerator";
    public static final String AGROLEARN_ENUMERATOR = "isEnableAgrolearn";
    public static final String DM_ENUMERATOR = "isEnableDiagnosticModule";

    public static final String CALCULATE_COMPOSITE_FLAG = "isCompliance";

    public static final String IS_SUPPLIER_ENABLED = "isEnableSupplierModule";
    public static final String IS_DISTRIBUTION_MODULE_ENABLED = "isDistributionModuleEnabled";

    public  static  long farmerId;
    public  static  String stakeholderId;
    public  static String productId;
    public  static String countryId;
    public  static int datatypeIdCLMRS;
    public  static String type = "NA";
    public  static  String olamFarmerId;
    public  static  int moduleId;

    public  static  long farmerModuleId;
    public  static  int subModuleId;
    public  static  String packagename;
    public static boolean isAppAlive;
    public static String notificationType = null;

    public static  int minProgressCount=0;

    public static String EN = "en";
    public static String FR = "fr";
    public static String ES = "es";
    public static String PT = "pt";
    public static String ID = "id";
    public static String TR = "TR";
    public static String LO = "lo";
    public static String VI = "vi";
    public static String TH = "th";
    public static int notificationLimit=500;
    public static String READ = "R";
    public static String UNREAD = "U";
    public static String DELETED = "D";
    public static String NOTIFICATIONLIST = "notificationDetailsList";
    public static String FDP = "FDP";
    public static String MODULE = "module";
    public static String PASSWORD = "password";
    public static String SCOPE = "offline_access";
    public static String CONSENTSTATEMENT = "ConsentStatement";

    public static String CONSENTERROR = "Error in saving FarmerConsent";
    public static String NID_ERROR = "This national ID has already been added to the farmer";

    // OW-4529 Fix
    public static String DUPLICATE_ERROR = "Duplicate survey entry";

    // OW-4516 Fix
    public static final int DATE_TIME_SETTING =6575;

    public static final String LOGIN_ATTEMPTS = "loginAttempts";

    public static final String LOCKOUT_TIME = "lockoutTime";

    public static final String PREVIOUS_ENTERED_USERNAME = "previousEnteredUsername";

    public static final String EUDR_GROUND_VERIFICATION_SURVEY = "On Ground Verification Survey";
    public static final String STAKE_HOLDER_ID = "stakeHolderId";
    public static final String STAKE_HOLDER_DISPLAY_ID = "stakeHolderId";
    public static final String STAKE_HOLDER_REGISTRATION = "stakeHolderRegistration";
    public static final String STAKE_HOLDER_NAME = "stakeHolderName";

    public static final String FARMER_ID = "farmerId";
    public static final String FARM_HOLDING_CODE = "farmHoldingCode";
    public static final String FARMER_NAME = "farmerName";
    public static final String FARM_ID = "farmId";
    public static final String KEY_LAST_SHOWN_DATE = "keyLastShownDate";
    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String IS_FROM_FIND_FARMER = "showRemainderSurvey";
    public static final String CHILD_LABOUR_ACTIVITY = "ChildLabourCompleteModuleActivity";
    public static final String CLMRS = "CLMRS";
    public static final String QWR = "QWR";
    public static final String DISTRIBUTION = "distribution";

    public static final int REQUEST_PERMISSIONS_FOR_TIRAMISU = 2025;
    public static final int REQUEST_PERMISSIONS_FOR_M = 2024;
    public static final String FLMRS = "FLMRS";
    public static final String SURVEY_TYPE_FLMRS = "8";
    public static final String SUPPLIER = "supplier";
    public static final String SUPPLIER_SURVEY = "supplierSurvey";
    public static final String DM_ERROR_MESSAGE = "No Diagnostic module assigned for user";
    public static final String FOLLOW_UP_COUNT = "followUpCount";
    public static final String ATTENDEE_SIGNATURE = "ATTENDEE_SIGNATURE";
    public static final String TYPE_POLYGON = "POLYGON";
    private static String blockCharacterSet = "~#^|$%&*!'\"";
    public static long SIZE_20_MB = 20 * 1024 * 1024;

public  static String base64ConversionForImage(String filepath)  {
    String encoded="";
    try {
        File photo = new File(filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
        Bitmap compressedBitmap=ImageCompressionUtil.imageCompress(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }catch (Exception e){

    }
    return encoded;
}


    public static String getLocaleSuffix(Resources res) {
        String result;
        result = res.getConfiguration().locale.getLanguage();
        if (result.equalsIgnoreCase("in")) {
            result = "id";
        }
        if (result.length() > 0) {
            result = "_" + result;
        }
        return result.toLowerCase();
    }

    static String getDefaultTextByLocalValue(Hashtable<String, String> hashtable, String localValue, Resources res) {
        Enumeration hkeys = hashtable.keys();
        String returnVal = localValue;
        String localeSuffix = getLocaleSuffix(res);

        if (localeSuffix.equalsIgnoreCase("_en")) {
            return returnVal;
        }

        while (hkeys.hasMoreElements()) {
            String hkey = (String) hkeys.nextElement();
            String hval = hashtable.get(hkey);
            if (hval.equalsIgnoreCase(localValue)) {
                returnVal = hkey.replace(localeSuffix, "");
                break;
            }
        }
        return returnVal;
    }

    static String[] since1900() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int difference = currentYear - 1899;
        String[] since1900 = new String[difference];

        for (int i = 0; i < difference; i++) {
            since1900[i] = String.valueOf(currentYear - i);
        }
        return since1900;
    }

    public static SQLiteDatabase getWriteableDb(Context context) {
        OFISDB ofisdb = OFISDB.getInstance(context);
        return ofisdb.getWritableDatabase();
    }

    static String makePlaceholders(int len) {
        if (len < 1) {
            return "?";
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    public static void removeFromSharedPrefs(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(prefName);
        editor.apply();
    }

    public static void saveToSharedPrefs(Context context, String prefName, String value, Boolean altVal) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value != null) {
            // enumeratorId and Passcode
            String encryptedContent="";
            try {
                encryptedContent = AESCrypt.encrypt(prefName, value);
            }catch (GeneralSecurityException e){
                //handle error
            }
            editor.putString(prefName, encryptedContent);
        }
        if (altVal != null) {
            editor.putBoolean(prefName, altVal);
        }



        editor.apply();
    }

    public static void saveLongToSharedPrefs(Context context, @SuppressWarnings("SameParameterValue") String prefName, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value != 0) {
            editor.putLong(prefName, value);
        }

        editor.apply();
    }

    public static void saveIntToSharedPrefs(Context context, @SuppressWarnings("SameParameterValue") String prefName, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value != 0) {
            editor.putInt(prefName, value);
        }

        editor.apply();
    }

    public static String getFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        String s= sharedPreferences.getString("ENUMERATOR_ID", "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt("ENUMERATOR_ID", s);
        }catch (Exception e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        if (decryptedMsg.equalsIgnoreCase("")){
            return  s;
        }else{
            return decryptedMsg;
        }

    }


    /**
     * get and set the enumerator Name
     * @param context
     * @param prefName
     * @param prefValue
     */
    public static void setEnumeratorNameSharedPrefs(Context context,String prefName,String prefValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        String encryptedContent="";
        try {
            encryptedContent = AESCrypt.encrypt(prefName, prefValue);
        }catch (GeneralSecurityException e){
            //handle error
        }

        editor.putString(prefName,encryptedContent);
        editor.commit();
    }
    public static String getEnumeratorNameSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String s=sharedPreferences.getString(StringConstants.ENUMERATOR_NAME, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(StringConstants.ENUMERATOR_NAME, s);
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        return decryptedMsg;
    }

    public static String getdatafromSharedPrefs(Context context, String data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String s=sharedPreferences.getString(data, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(data, s);
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        return decryptedMsg;
    }




//  getPasscode from sharedPref - Penetration testing
    public static String getPasscodeFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String s= sharedPreferences.getString("PASSCODE", "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt("PASSCODE", s);
        }catch (Exception e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        if (decryptedMsg.equalsIgnoreCase("")){
            return s;
        }else{
            return decryptedMsg;
        }

    }

    public static String getFirebaseTokenSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(SHARED_FIREBASE_TOKEN_UPDATED, "");
    }

   public static boolean getChildLabourPref(Context context,String prefName){
       SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
       return sharedPreferences.getBoolean(prefName, false);
   }

   public static void setChildLabourPref(Context context,String prefName,boolean prefValue){
       SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
       SharedPreferences.Editor editor=sharedPreferences.edit();
       editor.putBoolean(prefName,prefValue);
       editor.commit();
   }

    public static boolean getBoolFromSharedPrefs(Context context, String prefName, boolean defaultResponse) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(prefName, defaultResponse);
    }

    public static long getLongFromSharedPrefs(Context context, @SuppressWarnings("SameParameterValue") String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getLong(prefName, 0);
    }

    public static int getIntFromSharedPrefs(Context context, @SuppressWarnings("SameParameterValue") String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getInt(prefName, 0);
    }

    public static String getIMEI(Context c) {
        int permissionCheck = ContextCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            String android_id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
            return android_id;
        } else {
            return "imei not available";
        }
    }

    public static ProgressDialog openProgressDialog(Context c, String message, boolean showprogress) {
        ProgressDialog pd = new ProgressDialog(c);
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        if (!showprogress) {
            pd.setProgressNumberFormat("");
            pd.setProgressPercentFormat(null);
        }

        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(message);
        pd.show();
        return pd;
    }

    public static ProgressDialog updateProgressDialog(final ProgressDialog pd, final int max, final int progress, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null) {
                    minProgressCount=progress;
                    if (max != 0 && progress != 0 && pd.isIndeterminate()) {
                        pd.setIndeterminate(false);
                    }
                    if (max != 0) {
                        pd.setMax(max);
                    }
                    if (progress != 0) {
                        pd.setProgress(progress);
                    }
                    if (message != null) {
                        pd.setMessage(message);
                    }
                    pd.show();
                }
            }
        });
        return pd;
    }

    public static void closeProgressDialog(final ProgressDialog pd, Integer delay) {
        if (delay != null) {
            Handler handler = new Handler(Looper.getMainLooper());

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                }
            }, delay);
        } else {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    static void setSpinnerSelectionByValue(String selectedValue, Spinner spinner) {
        int spinnerCount = spinner.getCount();
        for (int i = 0; i < spinnerCount; i++) {
            String value = (String) spinner.getItemAtPosition(i);
            if (value.equalsIgnoreCase(selectedValue)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public static Bitmap loadSizedBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inMutable = true;

        BitmapFactory.decodeFile(filePath, options);
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static Hashtable<String, Integer> getDimensionsForDisplayFromBase(int baseWidth, int baseHeight, Resources res) {
        Hashtable<String, Integer> dimensions = new Hashtable<>();

        // now we have mdpi size, scale appropriately for screen
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        double multiplier;

        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_HIGH:
                multiplier = 1.5;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                multiplier = 2;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                multiplier = 3;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                multiplier = 4;
                break;
            default:
                multiplier = 1;
                break;
        }

        int outputWidth = (int) Math.round(baseWidth * multiplier);
        int outputHeight = (int) Math.round(baseHeight * multiplier);

        dimensions.put("width", outputWidth);
        dimensions.put("height", outputHeight);

        return dimensions;

    }

    public static String scaleRotateAndSaveImage(String imagePath, String kind) {

        return saveResizedImage(imagePath, kind);

    }

    private static String saveResizedImage(String imagePath, String imageType) {
        // we want to save the image regardless at its max phone size in the correct orientation
        int saveWidth = 600;
        int saveHeight = 798;

        String finalImagePath = imagePath;

        if (imageType.equalsIgnoreCase("signature")) {
            saveHeight = 600;
        }

        if (imageType.equalsIgnoreCase("trainingphoto")) {
            saveWidth = 804;
            saveHeight = 480;
        }

        if (imageType.equalsIgnoreCase("modulephoto")) {
            saveWidth = 804;
            saveHeight = 480;
        }

        // now we have a width and height check orientation
        try {
            if (imagePath != null) {
                ExifInterface exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int angle = exifToDegrees(orientation);

                Matrix mat = new Matrix();
                mat.postRotate(angle);
                Bitmap baseBitmap;

                if (angle == 90 || angle == 270) {
                    // this is a landscape image so lets create the bitmap sized appropriately
                    //noinspection SuspiciousNameCombination
                    baseBitmap = loadSizedBitmap(imagePath, saveHeight, saveWidth);
                } else {
                    baseBitmap = loadSizedBitmap(imagePath, saveWidth, saveHeight);
                }

                if (baseBitmap != null) {
                    Bitmap bitmap = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.getWidth(), baseBitmap.getHeight(), mat, true);

                    // save the newly scaled and rotated image
                    File destinationFile = new File(imagePath);

                    FileOutputStream out = new FileOutputStream(destinationFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.flush();
                    out.close();
                    finalImagePath = destinationFile.getAbsolutePath();

                    return finalImagePath;
                } else {
                    return null;
                }
            }

            } catch(IOException | OutOfMemoryError e){
                FirebaseCrash.report(new Exception("Utilities: Error resizing image", e));
                e.printStackTrace();
            }


        return finalImagePath;
    }

    public static long daysToUpdate(Context context) {
        long days = 30;
        // get date notified update was available
        long upgrade_by = getLongFromSharedPrefs(context, "upgrade_by");
        if (upgrade_by > 0) {
            long diff = upgrade_by - System.currentTimeMillis();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }

        return days;
    }

    public static boolean convertDT(String input){

        boolean consent=false;
        if (input.equalsIgnoreCase("1")){
            consent=true;
        }
        return consent;
    }

    public static Long currentTimeInMilliSeconds(){
        Calendar calendar=Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static Long currentDateStringToMilliSeconds(){

        Calendar cal=Calendar.getInstance();
        Date currentDate=cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Long timeInSeconds=0L;
        try{
            String strCurrentDate=formatter.format(currentDate);
            Date date = formatter.parse(strCurrentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            timeInSeconds= calendar.getTimeInMillis();
        }catch(ParseException e){
            e.printStackTrace();
        }
        return timeInSeconds;
    }

    public static Long dateStringToMilliSeconds(String dateStr){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long timeInSeconds=0L;
        try{
            Date date = sdf.parse(dateStr.substring(0,10));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            timeInSeconds= calendar.getTimeInMillis();
        }catch(ParseException e){
            e.printStackTrace();
        }
        return timeInSeconds;
    }

    public static void setKeycloakClientDetails(Context context, String prefName, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value != null) {
            String encryptedContent = "";
            try {
                encryptedContent = AESCrypt.encrypt(prefName, value);
            } catch (GeneralSecurityException e) {
                //handle error
            }
            editor.putString(prefName, encryptedContent);
        }
        editor.apply();
    }

    public static String getKeyCloakClientDetails(Context context,String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String s= sharedPreferences.getString(prefName, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(prefName, s);
        }catch (Exception e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        if (decryptedMsg.equalsIgnoreCase("")){
            return  s;
        }else{
            return decryptedMsg;
        }

    }


    // Logger for Manna
    public static void setCommoditySharedPrefs(Context context,String prefValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("commodity",prefValue);
        editor.commit();
    }
    public static String getCommoditySharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String commodity=sharedPreferences.getString("commodity", "nil");
        return commodity;
    }

    public static void setOperatingUnitSharedPrefs(Context context,String prefValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("opUnit",prefValue);
        editor.commit();
    }
    public static String getOperatingUnitSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String opunit=sharedPreferences.getString("opUnit", "nil");
        return opunit;
    }

    public static int getLoginAttemptsFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(LOGIN_ATTEMPTS, 0);
    }

    public static void setLoginAttemptsFromSharedPrefs(Context context, int prefValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(LOGIN_ATTEMPTS, prefValue);
        editor.apply();
    }

    public static Long getLockoutTimeFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(LOCKOUT_TIME, -1L);
    }

    public static void setLockoutTimeFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(LOCKOUT_TIME, currentTimeInMilliSeconds());
        editor.apply();
    }

    public static void setSharedPrefs(Context context, String prefValue, String prefName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefName, prefValue);
        editor.apply();
    }

    public static String getSharedPrefs(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(prefName, null);
    }


    public static String getColumnNameByLanguage() {
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> "nameEs";
            case "fr" -> "nameFr";
            case "id", "in" -> "nameId";
            case "lo" -> "nameLo";
            case "pt" -> "namePt";
            case "th" -> "nameTh";
            case "tr" -> "nameTr";
            case "vi" -> "nameVi";
            default -> "name";
        };
    }

    public static String getNamesByLanguage(DmMainModule dmMainModule){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "en" -> dmMainModule.name;
            case "es" -> dmMainModule.nameEs;
            case "fr" -> dmMainModule.nameFr;
            case "id", "in" -> dmMainModule.nameIdn;
            case "lo" -> dmMainModule.nameLo;
            case "pt" -> dmMainModule.namePt;
            case "th" -> dmMainModule.nameTh;
            case "tr" -> dmMainModule.nameTr;
            case "vi" -> dmMainModule.nameVi;
            default -> dmMainModule.name;
        };
    }

    public static String getContentByLanguage(SupplierSurveyDto supplierSurveyDto, boolean isNames){
        String language = Locale.getDefault().getLanguage();
        String result = switch (language) {
            case "es" -> isNames ? supplierSurveyDto.getNameEs() : supplierSurveyDto.getDescriptionEs();
            case "fr" -> isNames ? supplierSurveyDto.getNameFr() : supplierSurveyDto.getDescriptionFr();
            case "id", "in" -> isNames ? supplierSurveyDto.getNameIdn() : supplierSurveyDto.getDescriptionIdn();
            case "lo" -> isNames ? supplierSurveyDto.getNameLo() : supplierSurveyDto.getDescriptionLo();
            case "pt" -> isNames ? supplierSurveyDto.getNamePt() : supplierSurveyDto.getDescriptionPt();
            case "th" -> isNames ? supplierSurveyDto.getNameTh() : supplierSurveyDto.getDescriptionTh();
            case "tr" -> isNames ? supplierSurveyDto.getNameTr() : supplierSurveyDto.getDescriptionTr();
            case "vi" -> isNames ? supplierSurveyDto.getNameVi() : supplierSurveyDto.getDescriptionVi();
            default -> isNames ? supplierSurveyDto.getName() : supplierSurveyDto.getDescription();
        };

        return result != null ? result : "";
    }

    public static String getNamesByLanguage(DmQuestionsModule dmQuestionsModule){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> dmQuestionsModule.questionEs;
            case "fr" -> dmQuestionsModule.questionFr;
            case "id", "in" -> dmQuestionsModule.questionIdn;
            case "lo" -> dmQuestionsModule.questionLo;
            case "pt" -> dmQuestionsModule.questionPt;
            case "th" -> dmQuestionsModule.questionTh;
            case "tr" -> dmQuestionsModule.questionTr;
            case "vi" -> dmQuestionsModule.questionVi;
            default -> dmQuestionsModule.question;
        };
    }

    public static String getModuleSectionByLanguage(DmSectionsItem dmSectionsItem, boolean isSectionName){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> isSectionName ? dmSectionsItem.nameEs : dmSectionsItem.descriptionEs;
            case "fr" -> isSectionName ? dmSectionsItem.nameFr : dmSectionsItem.descriptionFr;
            case "id", "in" -> isSectionName ? dmSectionsItem.nameId : dmSectionsItem.descriptionIdn;
            case "lo" -> isSectionName ? dmSectionsItem.nameLo : dmSectionsItem.descriptionLo;
            case "pt" -> isSectionName ? dmSectionsItem.namePt : dmSectionsItem.descriptionPt;
            case "th" -> isSectionName ? dmSectionsItem.nameTh : dmSectionsItem.descriptionTh;
            case "tr" -> isSectionName ? dmSectionsItem.nameTr : dmSectionsItem.descriptionTr;
            case "vi" -> isSectionName ? dmSectionsItem.nameVi : dmSectionsItem.descriptionVi;
            default -> isSectionName ? dmSectionsItem.name : dmSectionsItem.description;
        };
    }

    public static String getLookupValueByLanguage(DmLookupValuesItem dmLookupValuesItem){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> dmLookupValuesItem.valueEs;
            case "fr" -> dmLookupValuesItem.valueFr;
            case "id", "in" -> dmLookupValuesItem.valueId;
            case "lo" -> dmLookupValuesItem.valueLo;
            case "pt" -> dmLookupValuesItem.valuePt;
            case "th" -> dmLookupValuesItem.valueTh;
            case "tr" -> dmLookupValuesItem.valueTr;
            case "vi" -> dmLookupValuesItem.valueVi;
            default -> dmLookupValuesItem.value;
        };
    }


    public static String appendSuffixForFarmName(String farmerName, String farmHoldingSuffix) {
        String language = Locale.getDefault().getLanguage();

        boolean isPrefix = switch (language) {
            case "es", "fr", "id", "in", "pt" -> true;
            default -> false;
        };

        return isPrefix ? farmHoldingSuffix + farmerName : farmerName + farmHoldingSuffix;
    }

    public static String getActionPlanByLanguage(DmRangeActionPlansItems dmRangeActionPlansItems){
        String language = Locale.getDefault().getLanguage();
        String actionPlan =  switch (language) {
            case "es" -> dmRangeActionPlansItems.actionPlanEs;
            case "fr" -> dmRangeActionPlansItems.actionPlanFr;
            case "id", "in" -> dmRangeActionPlansItems.actionPlanId;
            case "lo" -> dmRangeActionPlansItems.actionPlanLo;
            case "pt" -> dmRangeActionPlansItems.actionPlanPt;
            case "th" -> dmRangeActionPlansItems.actionPlanTh;
            case "tr" -> dmRangeActionPlansItems.actionPlanTr;
            case "vi" -> dmRangeActionPlansItems.actionPlanVi;
            default -> dmRangeActionPlansItems.actionPlan;
        };

        return actionPlan == null ? dmRangeActionPlansItems.actionPlan : actionPlan;
    }



// OW-4516 Fix
    public static void checkSystemTime(Context context, Resources res, AppCompatActivity activity, String className) {
        try {
            int auto_time_enabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0);
            int auto_time_zone_enabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
            if (auto_time_enabled == 0 || auto_time_zone_enabled == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(res.getString(R.string.set_auto_time_message))
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                                activity.startActivityForResult(intent, DATE_TIME_SETTING);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (Exception e) {
            FirebaseCrash.report(new Exception(className+": Error getting phone auto time settings", e));
            e.printStackTrace();
        }
    }

    public static boolean hasPermissions(String[] permissions, Context context) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static Set<Integer> getSubmittedModuleWithRecurrence(Cursor getCursorRecurrence, Set<Integer> allSubmittedModuleId, JSONObject submittedModules, long farmerId, DataLayer dl) {
        Set<Integer> recurrenceList = new HashSet<>(allSubmittedModuleId);
        boolean isCurrentRecurrence ;
        while (getCursorRecurrence.moveToNext()) {
            int currentId = getCursorRecurrence.getInt(getCursorRecurrence.getColumnIndex("_id"));
            try {
                isCurrentRecurrence = isCurrentRecurrence(getCursorRecurrence, submittedModules, currentId, farmerId, dl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (!isCurrentRecurrence) {
                recurrenceList.remove(currentId);
            }
        }
        return recurrenceList;
    }

    public static boolean isCurrentRecurrence(Cursor cursor, JSONObject submittedModules, int currentId, long farmerId, DataLayer dl) {
        final Calendar currentDate = Calendar.getInstance();
        final boolean hasSubmittedModules = (submittedModules.has("modules"));
        JSONArray submittedModulesArray = null;
        try {
            if (hasSubmittedModules) {
                submittedModulesArray = submittedModules.getJSONArray("modules");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String disabled_months_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_DISABLED_MONTHS));
        List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
        String start_date_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_START_DATE));

        int month = 0;
        int day = 0;
        int year = 0;
        Calendar recurrenceDate = Calendar.getInstance();
        Date startDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (start_date_string != null) {
                startDate = formatter.parse(start_date_string.split("T")[0]);
                recurrenceDate.setTime(startDate);
                month = recurrenceDate.get(Calendar.MONTH);
                day =  recurrenceDate.get(Calendar.DAY_OF_MONTH);
                year = recurrenceDate.get(Calendar.YEAR);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean currentRecurrence = false;
        if (hasSubmittedModules) {
            try {
                int recurrence_frequency = cursor.getInt(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_FREQUENCY));
                String recurrence_type = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_TYPE));
                Calendar calculationDate = (Calendar) currentDate.clone();

                for (int i = 0; i < submittedModulesArray.length(); i++) {
                    JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                    JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");

                    if (submittedModuleDetails.has("id") && submittedModuleDetails.getInt("id") == currentId) {

                        if (recurrence_frequency > 0 && !disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                            // for recurring
                            try {
                                // get last submitted date
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                                Calendar lastSubmittedDate = Calendar.getInstance();

                                //O20HS-84 - To enable the annual survey.
                                calculationDate = (Calendar) currentDate.clone();
                                //

                                try {
                                    lastSubmittedDate.setTime(sdf.parse(submittedModuleDetails.getString("created_at")));
                                } catch (ParseException e) {
                                    FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence date values", e));
                                    e.printStackTrace();
                                }

                                // code for india spices survey
                                String assignmentTypeColumn = "";
                                String assignmentType = "1";

                                switch (assignmentType){
                                    case "1" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID;
                                        break;
                                    case "2" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID;
                                        break;
                                    case "3" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID;
                                        break;
                                    case "4" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID;
                                        break;
                                    case "5" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID;
                                        break;
                                    case "6" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID;
                                        break;
                                    case "7" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID;
                                        break;
                                    case "8" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID;
                                        break;
                                    case "9" :
                                        assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID;
                                        break;
                                }

                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId , assignmentTypeColumn);

                                if (recurrence_type != null) {
                                    if (recurrence_type.equalsIgnoreCase("years")) {

                                        if (Objects.equals(farmerAssignmentField, "48")){

                                            // set the date according to start date
                                            calculationDate.add(Calendar.YEAR, -recurrence_frequency);
                                            calculationDate.set(Calendar.MONTH, Calendar.JULY);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, 31);

                                        }else {
                                            Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();
                                            nextRecurrenceDate.add(Calendar.YEAR, recurrence_frequency);

                                            while (!currentDate.before(nextRecurrenceDate)) {
                                                year += recurrence_frequency;
                                                recurrenceDate.add(Calendar.YEAR, recurrence_frequency);
                                                nextRecurrenceDate.add(Calendar.YEAR, recurrence_frequency);
                                            }

                                            calculationDate.set(Calendar.YEAR, year);
                                            calculationDate.set(Calendar.MONTH, month);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, day);
                                        }
                                    } else if (recurrence_type.equalsIgnoreCase("months")) {
                                        Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();
                                        Calendar previousRecurrenceDate = (Calendar) recurrenceDate.clone();
                                        while (!currentDate.before(nextRecurrenceDate)) {
                                            previousRecurrenceDate = (Calendar) nextRecurrenceDate.clone();
                                            nextRecurrenceDate.add(Calendar.MONTH, recurrence_frequency);

                                        }
                                        int newYear = previousRecurrenceDate.get(Calendar.YEAR);
                                        int newMonth = previousRecurrenceDate.get(Calendar.MONTH);

                                        calculationDate.set(Calendar.YEAR, newYear);
                                        calculationDate.set(Calendar.MONTH, newMonth);
                                        calculationDate.set(Calendar.DAY_OF_MONTH, day);

                                    } else if (recurrence_type.equalsIgnoreCase("weeks")) {
                                        Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();

                                        long daysBetween = (currentDate.getTimeInMillis() - recurrenceDate.getTimeInMillis()) / (1000 * 60 * 60 * 24);

                                        int weekDifference = (int) (daysBetween / 7);

                                        int weeksPassed = weekDifference / recurrence_frequency;

                                        nextRecurrenceDate.add(Calendar.WEEK_OF_YEAR, weeksPassed * recurrence_frequency);

                                        if (currentDate.before(nextRecurrenceDate)) {
                                            nextRecurrenceDate.add(Calendar.WEEK_OF_YEAR, -recurrence_frequency);
                                        }

                                        int newYear = nextRecurrenceDate.get(Calendar.YEAR);
                                        int newMonth = nextRecurrenceDate.get(Calendar.MONTH);
                                        int newDay = nextRecurrenceDate.get(Calendar.DAY_OF_MONTH);
                                        calculationDate.set(Calendar.YEAR, newYear);
                                        calculationDate.set(Calendar.MONTH, newMonth);
                                        calculationDate.set(Calendar.DAY_OF_MONTH, newDay);


                                    } else {
                                        Calendar finalReccurenceDate = (Calendar) recurrenceDate.clone();
                                        Calendar nextRecurrenceDate = (Calendar) finalReccurenceDate.clone();

                                        nextRecurrenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);

                                        while (!currentDate.before(nextRecurrenceDate)) {
                                            finalReccurenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);
                                            nextRecurrenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);
                                        }

                                        int newYear = finalReccurenceDate.get(Calendar.YEAR);
                                        int newMonth = finalReccurenceDate.get(Calendar.MONTH);
                                        int newDay = finalReccurenceDate.get(Calendar.DAY_OF_MONTH);
                                        calculationDate.set(Calendar.YEAR, newYear);
                                        calculationDate.set(Calendar.MONTH, newMonth);
                                        calculationDate.set(Calendar.DAY_OF_MONTH, newDay);
                                    }

                                    if (lastSubmittedDate.after(calculationDate) || isSameDay(lastSubmittedDate, calculationDate)) {
                                        currentRecurrence = true;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            currentRecurrence = true;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return currentRecurrence;
    }

    public static boolean isDuplicateNationalId(HashMap<Integer, String> existingNationalId, HashMap<Integer, String> enteredNationalId) {
        boolean isDuplicate = false;
        if (!enteredNationalId.isEmpty() && !existingNationalId.isEmpty()) {
            for (String nidNumbers : enteredNationalId.values()) {
                if (existingNationalId.containsValue(nidNumbers)) {
                    isDuplicate = true;
                    break;
                }
            }
        }
        return isDuplicate;
    }

    public static InputFilter filter = (source, start, end, dest, dstart, dend) -> {

        if (source != null && blockCharacterSet.contains(("" + source))) {
            return "";
        }
        for (int i = start; i < end; i++) {
            int type = Character.getType(source.charAt(i));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };

    public static double roundArea(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}