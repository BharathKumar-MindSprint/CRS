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
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
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

   public static int syctask=0;



    public  static  long farmerId;
    public  static  String olamFarmerId;
    public  static  int moduleId;

    public  static  long farmerModuleId;
    public  static  int subModuleId;
    public  static  String packagename;
    public static boolean isAppAlive;

    public static  int minProgressCount=0;




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
            editor.putString(prefName, value);
        }
        if (altVal != null) {
            editor.putBoolean(prefName, altVal);
        }

        editor.apply();

        if (prefName.equalsIgnoreCase("ENUMERATOR_ID")) {
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.setUserProperty("Enumerator", value);
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

    public static String getFromSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString("ENUMERATOR_ID", "");
    }

    public static boolean getBoolFromSharedPrefs(Context context, String prefName, boolean defaultResponse) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(prefName, defaultResponse);
    }

    public static long getLongFromSharedPrefs(Context context, @SuppressWarnings("SameParameterValue") String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getLong(prefName, 0);
    }

    public static String getStringFromSharedPrefs(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(prefName, "");
    }

    public static String getIMEI(Context c) {
        int permissionCheck = ContextCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            return (telephonyManager.getDeviceId() == null) ? "emulator" : telephonyManager.getDeviceId();
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

        } catch (IOException | OutOfMemoryError e) {
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

    public static String Moduleresponse = "{\n" +
            "  \"status\": \"updated\",\n" +
            "  \"outputdata\": {\n" +
            "\"modules\": [\n" +
            "      {\n" +
            "        \"id\": 27,\n" +
            "        \"name\": \"Cocoa Baseline Survey\",\n" +
            "        \"name_fr\": \"Enquête de Base\",\n" +
            "        \"name_es\": \"Encuesta Productor\",\n" +
            "        \"n ame_id\": \"Survey Awal\",\n" +
            "        \"name_pt\": \"Pesquisa inicial do Cacau\",\n" +
            "        \"name_tr\": \"Cocoa Baseline Survey\",\n" +
            "        \"name_lo\": \"Cocoa Baseline Survey\",\n" +
            "        \"name_vi\": \"Cocoa Bas eline Survey\",\n" +
            "        \"name_th\": \"Cocoa Baseline Survey\",\n" +
            "        \"expires\": null,\n" +
            "        \"annual_survey\": false,\n" +
            "        \"is_oscmodule\": false,\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": \"2014-12-31T23:59:59.000+0000\",\n" +
            "        \"updated_ at\": \"2018-01-06T21:03:23.000+0000\",\n" +
            "        \"app_id\": 0\n" +
            "      }\n" +
            "    ],\"submodules\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"module_id\": 2,\n" +
            "        \"module_name\": \"Cocoa Baseline Survey\",\n" +
            "        \"position\": 1,\n" +
            "        \"name\": \"Personal Information\",\n" +
            "        \"name_fr\": \"Informations Personnelles\",\n" +
            "        \"name_es\": \"Información Personal\",\n" +
            "        \"name_id\": \"Informasi pribadi\",\n" +
            "        \"name_pt\": \"Informaçãoes pessoal\",\n" +
            "        \"name_tr\": \"Personal Information\",\n" +
            "        \"name_lo\": \"Personal Information\",\n" +
            "        \"name_vi\": \"Personal Information\",\n" +
            "        \"name_th\": \"Personal Information\",\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": \"2014-12-31T23:59:59.000+0000\",\n" +
            "        \"updated_at\": \"2018-03-07T11:04:40.000+0000\",\n" +
            "        \"app_id\": 0\n" +
            "      }\n" +
            "    ],\n" +
            "    \"moduleassignments\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"module_id\": 2,\n" +
            "        \"moduleassignment_id\": 1,\n" +
            "        \"moduleassignment_type\": \"Product\",\n" +
            "        \"recurrence_frequency\": 0,\n" +
            "        \"recurrence_type\": \"\",\n" +
            "        \"disabled_months\": null,\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": \"2014-12-31T23:59:59.000+0000\",\n" +
            "        \"updated_at\": \"2014-12-31T23:59:59.000+0000\",\n" +
            "        \"app_id\": 0\n" +
            "      }\n" +
            "    ],\n" +
            "    \"datatypes\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"int\",\n" +
            "        \"created_at\": null,\n" +
            "        \"updated_at\": null,\n" +
            "        \"app_id\": null\n" +
            "      }\n" +
            "    ],\n" +
            "    \"questions\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"submodule_id\": 1,\n" +
            "        \"submodule_name\": \"Personal Information\",\n" +
            "        \"datatype_id\": 1,\n" +
            "        \"datatype_name\": \"int\",\n" +
            "        \"position\": 1,\n" +
            "        \"display_type\": \"Yes\\/No\",\n" +
            "        \"question\": \"What was the farmers highest educational level attained?\",\n" +
            "        \"question_fr\": \"Quel est le niveau scolaire atteint du planteur?\",\n" +
            "        \"question_es\": \"Cuál fue el nivel de eduacion más alto logrado por el agricultor?\",\n" +
            "        \"question_id\": \"Apa tingkat pendidikan tertinggi yang dimiliki oleh Petani\",\n" +
            "        \"question_pt\": \"Qual foi o nível educacional mais elevado dos agricultores alcançado?\",\n" +
            "        \"question_tr\": \"What was the farmers highest educational level attained?\",\n" +
            "        \"question_lo\": \"What was the farmers highest educational level attained?\",\n" +
            "        \"question_vi\": \"What was the farmers highest educational level attained\",\n" +
            "        \"question_th\": \"What was the farmers highest educational level attaine\",\n" +
            "        \"display_configuration\": \"{}\",\n" +
            "        \"storage_model\": \"Farmer\",\n" +
            "        \"storage_column\": \"education_level_id\",\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": \"2018-09-06T13:50:39.000+0000\",\n" +
            "        \"updated_at\": \"2018-09-06T13:50:39.000+0000\",\n" +
            "        \"app_id\": 2,\n" +
            "        \"is_subset_questions_required\": true,\n" +
            "        \"conditional_questions\": [\n" +
            "          {\n" +
            "            \"question\": \"2,3,1\",\n" +
            "            \"conditionCheck\": \"==YES\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"question\": \"3,9,1\",\n" +
            "            \"conditionCheck\": \"==NO\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"sipGroupId\": null,\n" +
            "        \"sipKey\": null\n" +
            "      }\n" +
            "    ],\n" +
            "    \"lookupvalues\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"question_id\": 15,\n" +
            "        \"question_name\": \"Does the farmer own his farm?\",\n" +
            "        \"position\": 1,\n" +
            "        \" value\": \"No School\",\n" +
            "        \"value_fr\": \"Pas d'école\",\n" +
            "        \"value_es\": \"Ningúna instrucción escolar\",\n" +
            "        \"value_id\": \"Tidak Sekolah\",\n" +
            "        \"value_pt\": \"Nunca foi na Escola\",\n" +
            "        \"value_tr\": \"No School\",\n" +
            "        \"value_lo\": \"No School\",\n" +
            "        \"value_vi\": \"No School\",\n" +
            "        \"value_th\": \"No School\",\n" +
            "        \"photo\": \"NULL\",\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": \"2014-12-31T23:59:59.000+0000\",\n" +
            "        \"updated_at\": \"2018-01-06T21:33:51.000+0000\",\n" +
            "        \"app_id\": 1,\n" +
            "        \"countryLookUpValueDtos\": [\n" +
            "          {\n" +
            "            \"id\": 1,\n" +
            "            \"country_id\": 1,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": \"2015-09-18T16:52:33.000+0000\",\n" +
            "            \"updated_at\": \"2015-09-18T16:52:33.000+0000\",\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 130,\n" +
            "            \"country_id\": 2,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": \"2015-09-18T16:52:35.000+0000\",\n" +
            "            \"updated_at\": \"2015-09-18T16:52:35.000+0000\",\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 259,\n" +
            "            \"country_id\": 3,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": \"2015-09-18T16:52:36.000+0000\",\n" +
            "            \"updated_at\": \"2015-09-18T16:52:36.000+0000\",\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 1417,\n" +
            "            \"country_id\": 11,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": \"2015-09-18T16:52:46.000+0000\",\n" +
            "            \"updated_at\": \"2015-09-18T16:52:46.000+0000\",\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 1546,\n" +
            "            \"country_id\": 12,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": \"2015-09-18T16:52:47.000+0000\",\n" +
            "            \"updated_at\": \"2015-09-18T16:52:47.000+0000\",\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 3420,\n" +
            "            \"country_id\": 16,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": null,\n" +
            "            \"updated_at\": null,\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 6869,\n" +
            "            \"country_id\": 27,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": null,\n" +
            "            \"updated_at\": null,\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 7457,\n" +
            "            \"country_id\": 42,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": null,\n" +
            "            \"updated_at\": null,\n" +
            "            \"app_id\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": 7707,\n" +
            "            \"country_id\": 44,\n" +
            "            \"lookupvalue_id\": 1,\n" +
            "            \"created_at\": null,\n" +
            "            \"updated_at\": null,\n" +
            "            \"app_id\": 2\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"assignmentTypes\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Country\",\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": null,\n" +
            "        \"updated_at\": null,\n" +
            "        \"app_id\": null\n" +
            "      }\n" +
            "    ],\n" +
            "    \"displayTypes\": [\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Calculated\",\n" +
            "        \"active\": true,\n" +
            "        \"created_at\": null,\n" +
            "        \"updated_at\": null,\n" +
            "        \"app_id\": null\n" +
            "      }\n" +
            "    ]\n" +
            "\n" +
            "    },\n" +
            "  \"resultcount\": 5076\n" +
            "}";
}