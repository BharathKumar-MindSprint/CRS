package com.olam.digital.ofis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.authentication.AccessTokenTask;
import com.olam.digital.ofis.authentication.DeviceIDxctractor;
import com.olam.digital.ofis.authentication.OnAccessTokenInterface;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.model.CountriesDTO;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SettingsActivity extends AppCompatActivity implements OnAccessTokenInterface {
    // update
    private Context context;
    private EditText enumeratorID;
    private Resources res;
    private DataLayer dl;
    private ProgressDialog m_ProgressDialog;
    Button buttonSave;
    private static final String SHARED_PREFS_NAME = "com.olam.digital.ofis.app_prefs";
    private static final String SHARED_FIREBASE_TOKEN_UPDATED = "firebase_token_updated";
    private boolean updateData=false;
    // Runtime Permission Request Code
    private final static int MY_PERMISSIONS_REQUEST = 9999;
    Button buttonPrivacyConsent, buttonReenterPasscode;
    private long lastClickTime = 0;
    TextView deviceid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        res = getResources();
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        dl = new DataLayer(res, context);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        TextView deviceid = (TextView) findViewById(R.id.deviceid);
        deviceid.setText(getResources().getString(R.string.android_id) + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        buttonPrivacyConsent=(Button) findViewById(R.id.buttonPrivacyConsent);
        buttonReenterPasscode=(Button) findViewById(R.id.reenterpasscode);
        buttonReenterPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogLogout();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 3000){
                    return;
                }else {
                // For clear cache functionality
                // FileUtils.deleteQuietly(context.getCacheDir());
                // Request Permission only for Android Version Marshmallow and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Phone Permission
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                            android.Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{android.Manifest.permission.READ_PHONE_STATE},
                                MY_PERMISSIONS_REQUEST);

                    } else {

                        // Permission already granted
                        bindSaveButton();
                    }
                } else {

                    // Call methods directly for Android versions lower than Marshmallow
                    bindSaveButton();
                }
            }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        buttonPrivacyConsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent=new Intent(SettingsActivity.this, PrivacyStatementActivity.class);
                Intent intent=new Intent(SettingsActivity.this, PrivacyStatementWebViewActivity.class);
                startActivity(intent);

            }
        });

        setupUI();
        bindHelpButton();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_HOME_ACTIVITY, ScreenNameConstants.SCREEN_SETTINGS_ACTIVITY, ScreenNameConstants.SCREEN_SETTINGS);
        String enumerator_id = Utilities.getFromSharedPrefs(context);
        if (enumerator_id.length() > 0) {
            enumeratorID.setText(enumerator_id);
        }

    }

    private void setupUI() {
        enumeratorID = (EditText) findViewById(R.id.enumeratorID);
    }

    private void bindSaveButton() {

        if(enumeratorID.getText().toString().trim().equalsIgnoreCase(Utilities.getFromSharedPrefs(context))){

        }else{
            Toast.makeText(this, R.string.invalid_imei_msg, Toast.LENGTH_LONG).show();
            return ;
        }

        Cursor networkQueue = dl.getNetworkQueue();
        if (networkQueue.getCount() == 0) {
            updateData=true;
            checkAuthenticate();
        } else {
            Toast.makeText(context, R.string.please_sync_before_update, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
        }
    }

    private void checkAuthenticate(){
        NetworkLayer nl = new NetworkLayer(context, res);
        if (nl.isPhoneOnline()) {
            AccessTokenTask accessTokenTask = new AccessTokenTask(context, getAuthRequestDTO(), SettingsActivity.this);
            accessTokenTask.execute();
        }else{
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private AuthRequestDTO getAuthRequestDTO(){
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(enumeratorID.getText().toString().trim());
        authRequestDTO.setPasscode(Utilities.getPasscodeFromSharedPrefs(SettingsActivity.this));
        return authRequestDTO;
    }


    private void bindHelpButton() {

        Button askForHelp = (Button) findViewById(R.id.buttonAssistance);
        Button sendForHelp = (Button) findViewById(R.id.buttonAssistanceSend);
        final EditText name = (EditText) findViewById(R.id.UserName);
        final Spinner countrySpinner = (Spinner) this.findViewById(R.id.spinner_country);
        final EditText phoneNo = (EditText) findViewById(R.id.UserPhoneNo);
        final EditText faultDescription = (EditText) findViewById(R.id.FaultDescription);
        final ImageButton hideHelpBox = (ImageButton) findViewById(R.id.helpCloseButton);
        final LinearLayout helpRequestHolder = (LinearLayout) findViewById(R.id.helpRequestHolder);
        final Animation mSlideBottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        final Animation mSlideBottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);

        Cursor CountriesDialCodes = dl.getEnumeratorCountries();


        List<CountriesDTO> DialCodes = new ArrayList<>();
        while (CountriesDialCodes.moveToNext()) {
            CountriesDTO countriesDTO = new CountriesDTO();
            countriesDTO.setDialingCodeId (CountriesDialCodes.getInt(CountriesDialCodes.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_ID)));
            countriesDTO.setDialingCode(CountriesDialCodes.getString(CountriesDialCodes.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE)));
            countriesDTO.setCountry(CountriesDialCodes.getString(CountriesDialCodes.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME)));
            DialCodes.add(countriesDTO);
        }


        CustomListAdapter_CountriesCode customSpinnerAdapter=new CustomListAdapter_CountriesCode(context, DialCodes);
        countrySpinner.setAdapter(customSpinnerAdapter);



        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long myId = id;

//                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        askForHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpRequestHolder.setVisibility(View.VISIBLE);
                helpRequestHolder.startAnimation(mSlideBottomUp);
            }
        });

        hideHelpBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpRequestHolder.startAnimation(mSlideBottomDown);
            }
        });

        sendForHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String helpName = name.getText().toString();
                CountriesDTO countriesDTO = (CountriesDTO) countrySpinner.getSelectedItem();
                String countryId = countriesDTO.getDialingCode();

                String helpPhoneNo = phoneNo.getText().toString();
                String helpDescription = faultDescription.getText().toString();


                if (helpName.length() > 0 && helpDescription.length() > 0) {
                    NetworkLayer nl = new NetworkLayer(context, getResources());

                    if (nl.isPhoneOnline()) {
                        updateData=false;
                        checkAuthenticate();
                    } else {
                        Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(context, R.string.Please_fill_required_fields, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSlideBottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                helpRequestHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
            return true;
        }else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }else {
            onBackPressed();
            return true;
        }

    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (m_ProgressDialog!=null && m_ProgressDialog.isShowing()) {
            m_ProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void showSettingsDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission was permanently denied");
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName())));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(SettingsActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        // Permission Denied
                        Toast.makeText(this, R.string.phone_state_permission_denial_msg, Toast.LENGTH_LONG).show();
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            // Permission Allowed
                            bindSaveButton();
                        } else {
                            // Checkbox set to Never Ask Again
                            showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onTokenCompleted(String status) {
        if(! status.equalsIgnoreCase("false")){
            if(updateData){
                if (enumeratorID.getText().length() > 0) {
                    Utilities.saveToSharedPrefs(context, "ENUMERATOR_ID", enumeratorID.getText().toString(), null);
                    NetworkLayer nl = new NetworkLayer(context, getResources());
                    nl.syncData();
                    //noinspection UnusedAssignment
                    nl = null;
                } else {
                    Toast.makeText(context, R.string.Please_fill_all_fields, Toast.LENGTH_SHORT).show();
                }
            }else {
                askForHelpUpdate();
            }
        }else{
            Toast.makeText(SettingsActivity.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
        }
    }

    private void askForHelpUpdate() {

        final EditText name = (EditText) findViewById(R.id.UserName);
        Spinner countrySpinner = (Spinner) this.findViewById(R.id.spinner_country);
        final EditText phoneNo = (EditText) findViewById(R.id.UserPhoneNo);
        final EditText faultDescription = (EditText) findViewById(R.id.FaultDescription);
        final ImageButton hideHelpBox = (ImageButton) findViewById(R.id.helpCloseButton);
        final LinearLayout helpRequestHolder = (LinearLayout) findViewById(R.id.helpRequestHolder);
        final Animation mSlideBottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        final Animation mSlideBottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
        NetworkLayer nl=new NetworkLayer(context,res);

        String helpName = name.getText().toString();
        String helpPhoneNo = phoneNo.getText().toString();
        String helpDescription = faultDescription.getText().toString();

        CountriesDTO countriesDTO = (CountriesDTO) countrySpinner.getSelectedItem();
        String countryId = countriesDTO.getDialingCode();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), false);
            }
        });

        Handler helpHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String pdUpdate = res.getString(R.string.item_synced);
                try {
                    final JSONObject response = (JSONObject) msg.obj;
                    String columnname = "message" + Utilities.getLocaleSuffix(res);
                    if (columnname.equalsIgnoreCase("message_en")) {
                        columnname = "message";
                    }
                    if (msg.what == 1) {
                        pdUpdate = response.optString(columnname, res.getString(R.string.item_synced));
                    } else if (msg.what == 2) {
                        pdUpdate = res.getString(R.string.item_sync_error);
                    }
                } catch(ClassCastException e) {
                    pdUpdate = res.getString(R.string.item_sync_error);
                } finally {
                    if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);

                        //Auto Closing of Help window after submitting
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                name.setText("");
                                faultDescription.setText("");
                                phoneNo.setText("");
                                helpRequestHolder.startAnimation(mSlideBottomDown);
                            }
                        });
                    }
                }
            }
        };

        nl.sendForHelp(helpName, countryId, helpPhoneNo, helpDescription, helpHandler);
    }

    private void dialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(res.getString(R.string.logout_note));
        Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                Utilities.saveToSharedPrefs(SettingsActivity.this, "FIRST_RUN", "", true);
                Intent in = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(in);
            }
        });

        alert.show();
    }




}
