package com.olam.digital.ofis;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.scottyab.aescrypt.AESCrypt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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
    public static int syctask=0;



    public  static  long farmerId;
    public  static String productId;
    public  static String countryId;
    public  static String type = "NA";
    public  static  String olamFarmerId;
    public  static  int moduleId;

    public  static  long farmerModuleId;
    public  static  int subModuleId;
    public  static  String packagename;
    public static boolean isAppAlive;

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
    public static String NOTIFICATIONLIST = "notificationDetailsList";
    public static String MODULELIST = "moduleList";
    public static String FDP = "FDP";
    public static String MODULE = "module";

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

    static String[] since1950() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int difference = currentYear - 1950;
        String[] since1950 = new String[difference];

        for (int i = 0; i < difference; i++) {
            since1950[i] = String.valueOf(currentYear - i);
        }
        return since1950;
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
//        Passcode added - Penetration testing
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        if (prefName.equalsIgnoreCase("ENUMERATOR_ID")) {
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.setUserProperty("Enumerator", value);
            }
        }else if(prefName.equalsIgnoreCase("PASSCODE")){
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.setUserProperty("Passcode", value);
            }
        }
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
        String s=sharedPreferences.getString(GPSConstants.COLUMN_ENUMERATOR_NAME, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(GPSConstants.COLUMN_ENUMERATOR_NAME, s);
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

    public static boolean getMultifarmPref(Context context,String prefName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(prefName, false);
    }

    public static void setMultiFarmPref(Context context,String prefName,boolean prefValue){
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

    public static String getStringFromSharedPrefs(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(prefName, "");
    }

    public static String getIMEI(Context c) {
        int permissionCheck = ContextCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            String android_id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
//            return (telephonyManager.getDeviceId() == null) ? "emulator" : telephonyManager.getDeviceId();
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

    static String scaleRotateAndSaveImage(String imagePath, String kind) {

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

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        WindowManager.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getDividerHeight()) * (listAdapter.getCount()));

        listView.setLayoutParams(params);
        listView.requestLayout();

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


}