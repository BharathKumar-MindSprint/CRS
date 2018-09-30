package com.olam.digital.ofis;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends BaseListActivity implements Callback<AuthenticationDTO> {

    // update
    private Context context;
    private static Context staticContext;
    private static Resources staticRes;
    private Resources res;
    private HomeActivity me;
    private DataLayer dl;
    private NetworkLayer nl;
    private ProgressDialog m_ProgressDialog = null;
    private ProgressDialog mDialogLoading=null;
    private boolean farmerSynced = true, autotimeEnabled = false;
    private Cursor networkQueue;
    private CustomListAdapter_NetworkQueue customListAdapter_networkQueue;
    private static LinearLayout topSection;
    private static RelativeLayout mainSection;
    private static ScrollView firstRunSection;
    private static TextView queueCount;
    private static CardView recordTransactionWrapper;
    private static CardView recordTrainingWrapper;
    private SharedPreferences sharedPreferences;
    private static MenuItem settingsButton, tasksButton;
    private Handler mainHandler;

    private final static String EDIT_FARMER_ID = "EDIT_FARMER_ID";
    private final static String EDIT_TRANSACTION_ITEM_ID = "EDIT_TRANSACTION_ITEM_ID";
    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";
    private final static String EDIT_MODE = "EDIT_MODE";

    private static final String FARMER_ID = "FARMER_ID";
    private static final String FARM_ID = "FARM_ID";
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String FARMER_NAME = "FARMER_NAME";

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final String ACCOUNT_TYPE = BuildConfig.ACCOUNT_TYPE;
    private static final String ACCOUNT = "ofis_companion";
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SYNC_INTERVAL_IN_MINUTES = 1L;
    private static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    private EditText enumeratorID;
    private Button saveUpdateButton;

    // Runtime Permission Request Code
    private final static int MY_PERMISSIONS_REQUEST = 9999;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        staticContext = this;
        context = me = this;
        staticRes = getResources();
        res = getResources();

        dl = new DataLayer(res, context);
        nl = new NetworkLayer(context, res);
        mainHandler = new Handler(Looper.getMainLooper());
        sharedPreferences = getSharedPreferences("com.olam.digital.ofis.app_prefs", MODE_PRIVATE);

        LinearLayout itemsHeader = (LinearLayout) ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.home_header_buttons, getListView(), false);
        getListView().addHeaderView(itemsHeader);

        firstRunSection = (ScrollView) findViewById(R.id.firstRunWrapper);
        mainSection = (RelativeLayout) findViewById(R.id.mainWrapper);
        recordTransactionWrapper = (CardView) findViewById(R.id.recordTransactionWrapper);
        recordTrainingWrapper = (CardView) findViewById(R.id.recordTrainingWrapper);

        enumeratorID = (EditText) findViewById(R.id.enumeratorID);
        saveUpdateButton = (Button) findViewById(R.id.buttonSaveUpdate);
        topSection = (LinearLayout) findViewById(R.id.topSectionWrapper);

        checkTime();
        bindNetworkQueue();

        // logic Added for show and hide the wrapper section of first time running App
//        if(new AppUtils().AppStatic.equalsIgnoreCase("")){
//            firstRunSection.setVisibility(View.VISIBLE);
//        }else{
//            firstRunSection.setVisibility(View.GONE);
//        }
        if (!Utilities.isAppAlive){
            firstRunSection.setVisibility(View.VISIBLE);
        }

        saveUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEnumerator = enumeratorID.getText().toString().trim();
                if (userEnumerator!=null && !userEnumerator.equalsIgnoreCase("")){

                 //   doAuthentication(userEnumerator);

                    AuthRequestDTO authRequestDTO = new AuthRequestDTO();
                    authRequestDTO.setLanguage(Locale.getDefault().getLanguage());
                    authRequestDTO.setApplication("ttmobile");
                    authRequestDTO.setUsername("400");
                    //authRequestDTO.setPassword("3751e350-a1d4-4db9-b045-9dc50cf544fb");
                    authRequestDTO.setPassword("pA11wX6srkWUpRQcpGjZeYcQuGUlXI6JkJxULTEeNRhNGZ1nRwVYLRNbLlyO");
                    authRequestDTO.setGrantType("password");
                    authRequestDTO.setClientId(5L);
                    authRequestDTO.setClientSecret(getString(R.string.client_secret));
                    AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, HomeActivity.this);
                    showProgress(true);
                    Call<AuthenticationDTO> authCall = authenticationInterface.getCoreAuthToken(authRequestDTO);
                    authCall.enqueue(HomeActivity.this);
                }else{
                    Toast.makeText(HomeActivity.this,getString(R.string.enumerator_empty),Toast.LENGTH_LONG).show();
                }

            }
        });

//        if (sharedPreferences.getBoolean("FIRST_RUN",true)) {
//
//
//            saveUpdateButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Request Permission only for Android Version Marshmallow and above
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        // Check Phone Permission
//                        if (ContextCompat.checkSelfPermission(HomeActivity.this,
//                                android.Manifest.permission.READ_PHONE_STATE)
//                                != PackageManager.PERMISSION_GRANTED) {
//
//                    //request permission
//                    ActivityCompat.requestPermissions(HomeActivity.this,
//                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
//                            MY_PERMISSIONS_REQUEST);
//
//                } else {
//
//                    // Permission already granted
//                    firstTimeSetup();
//                }
//            } else {
//
//                        // Call methods directly for Android versions lower than Marshmallow
//                        firstTimeSetup();
//                    }
//                }
//            });
//        } else {
//            checkForUpdate();
//            mainSection.removeView(firstRunSection);
//            long currentVersionCode = sharedPreferences.getLong("VERSION_NUM", 0);
//            if (BuildConfig.VERSION_CODE > currentVersionCode) {
//                Toast.makeText(context, res.getString(R.string.upgrade_message), Toast.LENGTH_LONG).show();
//                Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
//                nl.syncData();
//            }
//        }

    }

    private void doAuthentication(String userID) {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setLanguage(Locale.getDefault().getLanguage());
        authRequestDTO.setApplication("ttmobile");
        authRequestDTO.setUsername(userID);
        //authRequestDTO.setPassword("3751e350-a1d4-4db9-b045-9dc50cf544fb");
        authRequestDTO.setPassword("pA11wX6srkWUpRQcpGjZeYcQuGUlXI6JkJxULTEeNRhNGZ1nRwVYLRNbLlyO");
        authRequestDTO.setGrantType("password");
        authRequestDTO.setClientId(5L);
        authRequestDTO.setClientSecret(getString(R.string.client_secret));
        AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, HomeActivity.this);
        Call<AuthenticationDTO> authCall = authenticationInterface.getCoreAuthToken(authRequestDTO);
        authCall.enqueue(HomeActivity.this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        checkTime();
    }

    private static Account CreateSyncAcount() {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) staticContext.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        }

        return newAccount;
    }

    private void checkTime() {
        try {
            int auto_time_enabled = 0;
            int auto_time_zone_enabled = 0;
            if (Build.VERSION.SDK_INT <= 16) {
                auto_time_enabled = Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME, 0);
                auto_time_zone_enabled = Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME_ZONE);
            } else {
                auto_time_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                auto_time_zone_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
            }
            if (auto_time_enabled == 0 || auto_time_zone_enabled == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(res.getString(R.string.set_auto_time_message))
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                                startActivityForResult(intent, 6575);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                autotimeEnabled = true;
                bindButtons();
            }
        } catch (Settings.SettingNotFoundException e) {
            FirebaseCrash.report(new Exception("HomeActivity: Error getting phone auto time settings", e));
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6575) {
            checkTime();
        }
    }

    private void checkForUpdate() {
        if (nl.isPhoneOnline()) {
            @SuppressLint("HandlerLeak") Handler checkVersionHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 4) {
                        final JSONObject response = (JSONObject) msg.obj;
                        String columnname = "message" + Utilities.getLocaleSuffix(res);
                        if (columnname.equalsIgnoreCase("message_en")) {
                            columnname = "message";
                        }
                        if (response.has("download_url")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(true);
                            builder.setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            if (networkQueue.getCount() == 0) {
                                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_generic_message)))
                                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.optString("download_url", "https://ofisstaging.usertestingarea.com/downloads/app-debug.apk")));
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                                if (daysLeftToUpdate >= 0) {
                                                    String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_upload_first_message)))
                                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startAllItemSync();
                                            }
                                        })
                                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                                if (daysLeftToUpdate >= 0) {
                                                    String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                            }
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                }
            };

            try {
                nl.checkVersion(checkVersionHandler);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("HomeActivity: Error requesting phone version check", e));
                e.printStackTrace();
            }
        }
    }

    private void firstTimeSetup() {

        if (enumeratorID.getText().length() > 0) {
            Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
            Utilities.saveToSharedPrefs(context, "ENUMERATOR_ID", enumeratorID.getText().toString(), null);
            nl.syncData();
            //nl.postFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        } else {
            Toast.makeText(context, R.string.Please_fill_all_fields, Toast.LENGTH_SHORT).show();
        }


    }

    public static void completeFirstSetup() {
        Utilities.saveToSharedPrefs(staticContext, "FIRST_RUN", "", false);
        Animation mSlideInTop = AnimationUtils.loadAnimation(staticContext, R.anim.in_top);
        Animation mSlideOutBottom = AnimationUtils.loadAnimation(staticContext, R.anim.out_bottom);
        if (settingsButton != null) {
            settingsButton.setVisible(true);
        }
        if (tasksButton != null) {
            tasksButton.setVisible(true);
        }
        boolean can_transact = Utilities.getBoolFromSharedPrefs(staticContext, "can_transact", false);

        if (!can_transact) {
            recordTransactionWrapper.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recordTrainingWrapper.getLayoutParams();
            lp.setMargins(0,0,0,0);
        }

        firstRunSection.startAnimation(mSlideOutBottom);
        topSection.startAnimation(mSlideInTop);

        mSlideOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainSection.removeView(firstRunSection);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void bindNetworkQueue() {
        TextView nomodulestosubmittext = (TextView) findViewById(R.id.nomodulestosubmittext);
        networkQueue = dl.getNetworkQueue();
        if (networkQueue.getCount() > 0) {
            queueCount = (TextView) findViewById(R.id.networkQueueCount);
            String queueTotal = Integer.toString(networkQueue.getCount());
            queueCount.setText(queueTotal);

            nomodulestosubmittext.setVisibility(View.GONE);
            String[] adapterCols = new String[]{"_id", OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID};
            int[] adapterRowViews = new int[]{R.id.networkQueueId, R.id.networkQueueName, R.id.networkQueuePoiType, R.id.networkQueueOlamFarmerId};
            customListAdapter_networkQueue = new CustomListAdapter_NetworkQueue(context, networkQueue, adapterCols, adapterRowViews, me);
            setListAdapter(customListAdapter_networkQueue);

            if (Utilities.daysToUpdate(context) <= 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(res.getString(R.string.ofis_update_overdue))
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

        } else {
            getListView().setAdapter(null);
            if (Utilities.daysToUpdate(context) <= 0) {
                nomodulestosubmittext.setText(R.string.ofis_update_overdue);
                nomodulestosubmittext.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

    }

    private void startAllItemSync() {
        if (nl.isPhoneOnline()) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), true);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                    }
                }
            });

            Handler syncHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    }
                }
            };
            farmerSynced = true;
            nl.getNetworkQueue(null);
            nl.syncNetworkQueue(null, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public void startItemSync(int itemId) {
        if (nl.isPhoneOnline()) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), false);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                    }
                }
            });

            Handler syncHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    } else {
                        finishItemSync(msg);
                    }
                }
            };
            farmerSynced = true;
            nl.getNetworkQueue(itemId);
            nl.syncNetworkQueue(itemId, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private void finishItemSync(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                {
                    String pdUpdate = msg.obj.toString();
                    m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                    Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                }
                networkQueue.close();
                networkQueue = dl.getNetworkQueue();
                customListAdapter_networkQueue.swapCursor(networkQueue);
                String queueTotal = Integer.toString(networkQueue.getCount());
                queueCount.setText(queueTotal);
                if (networkQueue.getCount() == 0) {
                    TextView nomodulestosubmittext = (TextView) findViewById(R.id.nomodulestosubmittext);
                    nomodulestosubmittext.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void bindButtons() {
        Button addNewFarmer = (Button) findViewById(R.id.AddNewFarmer);
        Button completeModule = (Button) findViewById(R.id.CompleteQuestionnaireModule);
        Button addSocialInfra = (Button) findViewById(R.id.AddSocialInfrastructure);
        Button recordTransaction = (Button) findViewById(R.id.RecordTransaction);
        Button recordTraining = (Button) findViewById(R.id.RecordFarmerTraining);
        ImageButton syncAll = (ImageButton) findViewById(R.id.syncAll);
        boolean can_transact = Utilities.getBoolFromSharedPrefs(context, "can_transact", false);

        syncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (networkQueue.getCount() > 0) {
                startAllItemSync();
            }
            }
        });

        if (Utilities.daysToUpdate(context) > 0 && autotimeEnabled) {

            addNewFarmer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddFarmerActivity.class);
                    intent.putExtra(EDIT_FARMER_ID, (long) 0);
                    intent.putExtra(EDIT_MODE, false);
                    context.startActivity(intent);
                }
            });

            completeModule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FindFarmerActivity.class);
                    context.startActivity(intent);
                }
            });

            addSocialInfra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddInfrastructureActivity.editMode = false;
                    AddInfrastructureActivity.infrastructureID = 0;
                    Intent intent = new Intent(context, AddInfrastructureActivity.class);
                    context.startActivity(intent);
                }
            });

            if (can_transact || sharedPreferences.getBoolean("FIRST_RUN",true)) {
                recordTransaction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TransactionItems.class);
                        context.startActivity(intent);
                    }
                });
            } else {
                recordTransactionWrapper.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recordTrainingWrapper.getLayoutParams();
                lp.setMargins(0,0,0,0);
            }

            recordTraining.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TrainingRecords.class);
                    context.startActivity(intent);
                }
            });

        } else {
            addNewFarmer.setAlpha(.5f);
            addNewFarmer.setClickable(false);
            completeModule.setAlpha(.5f);
            completeModule.setClickable(false);
            addSocialInfra.setAlpha(.5f);
            addSocialInfra.setClickable(false);
            recordTransaction.setAlpha(.5f);
            recordTransaction.setClickable(false);
            recordTraining.setAlpha(.5f);
            recordTraining.setClickable(false);
        }
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        // carry out relevant action based on item clicked
        if (networkQueue.getCount() > 1) {
            networkQueue.moveToPosition(position-1);
        }
        String itemType = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE));
        if (itemType.equalsIgnoreCase("infrastructure")) {
            Intent intent = new Intent(this, AddInfrastructureActivity.class);
            AddInfrastructureActivity.infrastructureID = id;
            AddInfrastructureActivity.editMode = true;
            startActivity(intent);
        } else if (itemType.equalsIgnoreCase("farmer")) {
            Intent intent = new Intent(this, AddFarmerActivity.class);
            intent.putExtra(EDIT_FARMER_ID, (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
            intent.putExtra(EDIT_MODE, true);
            startActivity(intent);
        } else if (itemType.equalsIgnoreCase("farmlocation")) {
            Intent intent = new Intent(this, ViewFarmerActivity.class);
            ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
            startActivity(intent);
        }  else if (itemType.equalsIgnoreCase("farmpolygon")) {
            Intent recordPolygonIntent = new Intent(this, RecordPolygonActivity.class);
            final int lFarmID = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID));
            int count = SharedPreferenceUtils.getInstance(getApplicationContext()).getPlotCount(""+lFarmID);
            recordPolygonIntent.putExtra(FARMER_ID, networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            recordPolygonIntent.putExtra(FARM_ID, lFarmID);
            recordPolygonIntent.putExtra(OLAM_FARMER_ID, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            recordPolygonIntent.putExtra(FARMER_NAME, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
            recordPolygonIntent.putExtra(PLOT_COUNT, count);
            recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
            startActivity(recordPolygonIntent);
        } else if (itemType.equalsIgnoreCase("module")) {
            Intent intent = new Intent(this, ViewFarmerActivity.class);
            ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
            startActivity(intent);
        } else if (itemType.equalsIgnoreCase("fmpreview") || itemType.equalsIgnoreCase("fullfmpreview")) {
            Intent intent = new Intent(this, ViewFarmerActivity.class);
            ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
            startActivity(intent);
        } else if (itemType.equalsIgnoreCase("transaction")) {
            Intent intent = new Intent(context, TransactionItem.class);
            intent.putExtra(EDIT_TRANSACTION_ITEM_ID, networkQueue.getLong(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID)));
            intent.putExtra(EDIT_MODE, true);
            intent.putExtra(READ_ONLY_MODE, true);
            startActivity(intent);
        } else if (itemType.equalsIgnoreCase("trainingrecord")) {
            Intent intent = new Intent(context, TrainingRecord.class);
            intent.putExtra(EDIT_TRAININGRECORD_ID, networkQueue.getLong(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID)));
            intent.putExtra(EDIT_MODE, true);
            intent.putExtra(READ_ONLY_MODE, true);
            startActivity(intent);
        }else if (itemType.equalsIgnoreCase("farmer_photo")) {
            //JIRA req OW-505
            Intent intent = new Intent(this, ViewFarmerActivity.class);
            ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (autotimeEnabled) {
            getMenuInflater().inflate(R.menu.menu_home, menu);

            if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
                settingsButton = menu.findItem(R.id.action_settings);
                settingsButton.setVisible(false);
                tasksButton = menu.findItem(R.id.action_tasks);
                tasksButton.setVisible(false);
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        if (!networkQueue.isClosed()) {
            networkQueue.close();
        }
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
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName())));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(HomeActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
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
                            firstTimeSetup();
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
    public void onResponse(Call<AuthenticationDTO> call, Response<AuthenticationDTO> response) {
        showProgress(false);
        if (response.isSuccessful()) {
            AuthenticationDTO authenticationDTO = response.body();
            authenticationDTO.setWillExpireAt((System.currentTimeMillis() / 1000) + authenticationDTO.getExpiresIn() - 60);
            new AuthenticationHelper(this).setAuthenticationDTO(authenticationDTO);
            new AuthenticationHelper(this).setUserName(enumeratorID.getText().toString());
          //  new AppUtils().AppStatic="LogIn";
            // Do initial check for App
            Utilities.isAppAlive=true;
            doInitialCheck();
        } else {

            Answers.getInstance().logLogin(new LoginEvent()
                    .putMethod("first")
                    .putCustomAttribute("reason", "wrong credentials")
                    .putSuccess(false));
        }
    }

    @Override
    public void onFailure(Call<AuthenticationDTO> call, Throwable t) {
        showProgress(false);
              Toast.makeText(HomeActivity.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod("first")
                .putCustomAttribute("reason", "network error")
                .putSuccess(false));
    }

    private void doInitialCheck() {

        if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
            // Request Permission only for Android Version Marshmallow and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check Phone Permission
                if (ContextCompat.checkSelfPermission(HomeActivity.this,
                        android.Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

                    //request permission
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST);

                } else {

                    // Permission already granted
                    firstTimeSetup();
                }
            } else {

                // Call methods directly for Android versions lower than Marshmallow
                firstTimeSetup();
            }
        } else {
            checkForUpdate();
            mainSection.removeView(firstRunSection);
            long currentVersionCode = sharedPreferences.getLong("VERSION_NUM", 0);
            if (BuildConfig.VERSION_CODE > currentVersionCode) {
                Toast.makeText(context, res.getString(R.string.upgrade_message), Toast.LENGTH_LONG).show();
                Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
                nl.syncData();
            }
        }

        Account mAccount = CreateSyncAcount();

        ContentResolver.setSyncAutomatically(
                mAccount,
                AUTHORITY,
                true
        );

        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL
        );
    }

    private void showProgress(boolean show) {
        if (show && mDialogLoading == null) {
            mDialogLoading = ProgressDialog.show(this, "", getString(R.string.loggin_In), true);
            mDialogLoading.setCancelable(false);
            mDialogLoading.show();
        } else if (mDialogLoading != null) {
            mDialogLoading.dismiss();
            mDialogLoading = null;
        }
    }
}
