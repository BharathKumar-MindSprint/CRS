package com.olam.digital.ofis;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;


public class FindFarmerActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    private Context context;
    private Resources res;
    private Cursor farmerList;
    private DataLayer dl;
    private CustomListAdapter_Farmer customListAdapter_farmerAdapter;
    private EditText ed;
    private Spinner farmerGroupSpinner, sectionSpinner;
    private long selectedFarmerGroup = 0, selectedSection = 0;
    private String searchQuery = "";

    private final static String BUYING_AGENT_CONTEXT = "BUYING_AGENT_CONTEXT";
    private final static String TRANSACTION_MODE = "TRANSACTION_MODE";
    private final static String TRANSACTEE_ID = "TRANSACTEE_ID";
    private final static String TRANSACTEE_TYPE = "TRANSACTEE_TYPE";
    private final static String EDIT_TRANSACTION_ITEM_ID = "EDIT_TRANSACTION_ITEM_ID";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String FARMER_ID = "FARMER_ID";
    private final static String BACK_TO_FINDER = "BACK_TO_FINDER";
    private boolean transactionMode = false, backtoFinder = false;
    private long transactionItemId, buyingAgentId;
    private String blockCharacterSet = "~#^|$%&*!'\"";
    ListView listView;
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_farmer);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView=findViewById(R.id.list);
        context = this;
        res = getResources();

        dl = new DataLayer(res, context);

        farmerGroupSpinner = (Spinner) findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (Spinner) findViewById(R.id.spinner_section);
        ed = (EditText) findViewById(R.id.findFarmerSearchBox);
        ed.setFilters(new InputFilter[] { filter });
        Intent intent = this.getIntent();
        transactionMode = intent.getBooleanExtra(TRANSACTION_MODE, false);
        backtoFinder = intent.getBooleanExtra(BACK_TO_FINDER, false);
        transactionItemId = intent.getLongExtra(EDIT_TRANSACTION_ITEM_ID, 0);
        buyingAgentId = intent.getLongExtra(BUYING_AGENT_CONTEXT, 0);

        if (transactionMode) {
            this.setTitle(R.string.title_activity_find_transactee);
            TextView pageTitle = (TextView) findViewById(R.id.pageTitle);
            pageTitle.setText(res.getString(R.string.select_transactee));

            ed.setHint(R.string.farmer_or_buying_agent_name);
        }

        bindFarmerList();
        showFarmerList();
        setUpSearchFunctionality();
        listView.setOnItemClickListener(this);
    }

//    @Override
//    protected void onListItemClick (ListView l, View v, int position, long id) {
//
//        //we now view this farmer
//        Intent intent;
//
//        if (transactionMode) {
//            TextView transacteetype = (TextView) v.findViewById(R.id.transacteeType);
//            intent = new Intent(context, TransactionItem.class);
//            intent.putExtra(TRANSACTEE_TYPE, transacteetype.getText().toString());
//            intent.putExtra(TRANSACTEE_ID, id);
//            intent.putExtra(EDIT_TRANSACTION_ITEM_ID, transactionItemId);
//            intent.putExtra(BUYING_AGENT_CONTEXT, buyingAgentId);
//            intent.putExtra(BACK_TO_FINDER, backtoFinder);
//            if (transactionItemId > 0) {
//                intent.putExtra(EDIT_MODE, true);
//            }
//            intent.putExtra(FARMER_ID, id);
//        } else {
//            ViewFarmerActivity.farmerId = id;
//            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(id);
//            intent = new Intent(context, ViewFarmerActivity.class);
//        }
//        context.startActivity(intent);
//    }

    private void bindFarmerList() {
        if (transactionMode) {
            farmerList = dl.getFindFarmerOrBuyingAgentList(buyingAgentId, searchQuery, selectedFarmerGroup, selectedSection);
        } else {
            farmerList = dl.getFindFarmerList(searchQuery, selectedFarmerGroup, selectedSection);
        }
    }

    private void showFarmerList() {
        if (farmerList.getCount() > 0) {
            String[] adapterCols = new String[]{
                    "name",
                    "olam_farmer_id"
            };
            int[] adapterRowViews = new int[]{R.id.farmerListFarmerName, R.id.farmerListOlamFarmerId};
            customListAdapter_farmerAdapter = new CustomListAdapter_Farmer(context, farmerList, adapterCols, adapterRowViews, res, transactionMode);
           // setListAdapter(customListAdapter_farmerAdapter);
            listView.setAdapter(customListAdapter_farmerAdapter);
        }
    }

    private void setUpSearchFunctionality()
    {
        Cursor farmerGroups = dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        dl.bindDataDrivenSpinner(farmerGroupSpinner, farmerGroups, context, R.string.spinner_farmergroup_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

        farmerGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor sections = dl.getSpinnerData(OFISDBContract.SectionsEntry.TABLE_NAME, true, OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID, ((int) id), true, OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME);
                dl.bindDataDrivenSpinner(sectionSpinner, sections, context, R.string.spinner_section_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

                selectedFarmerGroup = id;
                selectedSection = 0;
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = id;
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ed.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s) {  }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //searches and populates the list with the appropriate info
                searchQuery = String.valueOf(ed.getText());
                doSearch();
            }
        });

    }

    //searches and populates the list with the appropriate info
    private void doSearch()
    {
        farmerList.close();
        bindFarmerList();
        runOnUiThread(returnRes);//thread redoes list and notifies etc. so it all updates nicely with results of search
    }

    private final Runnable returnRes = new Runnable() {

        public void run() {
            if (customListAdapter_farmerAdapter != null) {
                customListAdapter_farmerAdapter.swapCursor(farmerList);
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_HOME_ACTIVITY, ScreenNameConstants.SCREEN_FIND_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_FIND_FARMER);
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
            if (transactionMode) {
                finish();
            } else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }

        if (id == R.id.action_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        //we now view this farmer
        Intent intent;

        if (transactionMode) {
            TextView transacteetype = (TextView) v.findViewById(R.id.transacteeType);
            intent = new Intent(context, TransactionItem.class);
            intent.putExtra(TRANSACTEE_TYPE, transacteetype.getText().toString());
            intent.putExtra(TRANSACTEE_ID, id);
            intent.putExtra(EDIT_TRANSACTION_ITEM_ID, transactionItemId);
            intent.putExtra(BUYING_AGENT_CONTEXT, buyingAgentId);
            intent.putExtra(BACK_TO_FINDER, backtoFinder);
            if (transactionItemId > 0) {
                intent.putExtra(EDIT_MODE, true);
            }
            intent.putExtra(FARMER_ID, id);
        } else {
            ViewFarmerActivity.farmerId = id;
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(id);
            intent = new Intent(context, ViewFarmerActivity.class);
        }
        context.startActivity(intent);
    }
}
