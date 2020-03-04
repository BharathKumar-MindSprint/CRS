package com.olam.digital.ofis;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.inapp.InApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Base Activity Created by ben on 13/11/2016.
 */
public class BaseActivity extends AppCompatActivity {
    public final static String LIMIT_XED = "limit";
    public final static String DUPLICATE = "duplicate";
    public final static String ENTER_NID = "novalues";
    public final static String SELECT_NATIONALID = "ID TYPE";
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidateOptionsMenu();
        }
    };
    public DataLayer dl;
    public Resources res;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        res = getResources();
        dl = new DataLayer(res, context);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("TasksUpdated"));
        InApp.identifiedUserWithEnvironment(new AuthenticationHelper(this).getuserName(), BuildConfig.FLAVOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_other, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        if (id == R.id.action_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean getNIDPhotostatusByType(String type, long editFarmerId) {
        boolean photostatus = false;
        Cursor nid = dl.getNIDPhotostatusByType(type, editFarmerId);
        if (nid.getCount() > 0) {
            photostatus = true;
        }
        return photostatus;
    }

    public String getNIDPhotoByType(String type, long farmerid) {
        String photo = null;
        Cursor nid = dl.getNIDPhotostatusByType(type, farmerid);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photo = nid.getString(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO));
        }
        return photo;
    }

    public ArrayList<Integer> getAllphotoTypes() {
        ArrayList<Integer> types = new ArrayList<>();
        Cursor nid = dl.getAllPhotos();
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            int type = nid.getInt(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE));
            types.add(type);
        }
        return types;
    }

    public void compareTypes(ArrayList<Integer> selected_types, ArrayList<Integer> allphotoTypes) {
        for (int type2 : allphotoTypes) {
            boolean found = false;
            for (int type1 : selected_types) {
                if (type2 == type1) {
                    found = true;
                }
            }
            if (!found) {
                dl.removeNIDPhoto(type2);
            }
        }
    }

    // getting nationalids type id values
    public int getNidType(String type) {
        int id = 0;
        Cursor nid = dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id = nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID));
        }
        return id;
    }

    // getting nationalids type id values
    public int getNidLimit(String type) {
        int id = 0;
        Cursor nid = dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id = nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_LIMIT));
        }
        return id;
    }

    // getting nationalids type id values
    public String getNidCaptureStatus(String type) {
        String photostatus = "true";
        Cursor nid = dl.getNIDPhotostatusByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photostatus = nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_PHOTO_REQUIRED));
        }
        return photostatus;
    }

    // getting nationalids type id values
    public boolean getPhotostatusByType(String type) {
        boolean photostatus = false;
        Cursor nid = dl.getPhotostatusByType(type);
        if (nid.getCount() > 0) {
            photostatus = true;
        }
        return photostatus;
    }

    public void removehmvalue(HashMap<Integer, String> hm_uripath, int val) {
        Iterator<Integer> iterator = hm_uripath.keySet().iterator();
        while (iterator.hasNext()) {
            Integer nid = iterator.next();
            if (nid == val) {
                iterator.remove();
            }
        }
    }

    public void removehmURIvalue(HashMap<Integer, Uri> hm_uripath, int val) {
        Iterator<Integer> iterator = hm_uripath.keySet().iterator();
        while (iterator.hasNext()) {
            Integer nid = iterator.next();
            if (nid == val) {
                iterator.remove();
            }
        }
    }

    public JSONArray fetchnationalidvalues() {
        Cursor nids = dl.getnidsfromlocaldb();
        JSONArray storeableNationalids = new JSONArray();
        try {
            while (nids.moveToNext()) {
                JSONObject storeableNids = new JSONObject();
                storeableNids.put("nationalIdType", new Long(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE))));
                storeableNids.put("nationalIdNumber", nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
                storeableNids.put("photo", nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO)));

                storeableNationalids.put(storeableNids);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return storeableNationalids;
    }

    // getting nationalids type id values
    public String getPhotobytype(int type) {
        String photo = null;
        Cursor nid = dl.getPhotoByType(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photo = nid.getString(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO));
        }
        return photo;
    }

    // getting nationalids type id values
    public String getNidTypebyid(int id) {
        String type = null;
        Cursor nid = dl.getNIDById(id);
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
        }
        return type;
    }

    public String getnationalidSubmitStatus(LinearLayout nationalids) {
        String typeadded = "false";
        int nidrowscount = nationalids.getChildCount();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < nidrowscount; i++) {
            LinearLayout view = (LinearLayout) nationalids.getChildAt(i);

            Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
            EditText nidnumber = view.findViewById(R.id.nidnumber);
            String value = idtypespinner.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            int limit = getNidLimit(idtypespinner.getSelectedItem().toString());
            if (limit < nid.length()) {
                typeadded = LIMIT_XED;
            }
            if (nidrowscount == 1 && i == 0) {
                if (type == 0) {
                    typeadded = ENTER_NID;
                }
            }
            names.add(String.valueOf(type));
        }
        Set<String> store = new HashSet<>();
        for (String name : names) {
            if (store.add(name) == false) {
                typeadded = DUPLICATE;
            }
        }

        return typeadded;
    }

    public boolean validateSpinnerValues(LinearLayout nationalids, String selected_item) {
        boolean status = true;
        if (selected_item.equalsIgnoreCase(SELECT_NATIONALID)) {
            return false;
        }
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nationalidinclude = (LinearLayout) nationalids.getChildAt(j);
            Spinner spinner = nationalidinclude.findViewById(R.id.idtypespinner);
            String val = spinner.getSelectedItem().toString();
            if (val.equalsIgnoreCase(SELECT_NATIONALID)) {

                return false;
            }
        }
        return status;
    }

    public void showSettingsDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.permission_permanent_denial_title);
        dialog.setMessage(msg).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName())));
            }
        }).setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Toast.makeText(context, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    public void displayBitmapforNID(LinearLayout nationalids, int nidvalue, String savedImagePath) {
        // set our desired width and height for display
        int baseWidth = 100;
        int baseHeight = 133;
        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());
        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);
        int nidrowscount = nationalids.getChildCount();

        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            Spinner nid_type = nidrow.findViewById(R.id.idtypespinner);
            ImageButton imageButton = nidrow.findViewById(R.id.nationalidphoto);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            if (type == nidvalue) {
                imageButton.setImageBitmap(bitmap);
            }
        }


    }

    public void refreshTags(LinearLayout nationalids) {
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            if (j > 0) {
                LinearLayout nationalidinclude = (LinearLayout) nationalids.getChildAt(j);
                ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                actionbtn.setTag(j + 1);
                nationalidphoto.setTag(j + 1);

            }
        }
    }
}
