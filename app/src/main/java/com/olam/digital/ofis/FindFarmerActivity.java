package com.olam.digital.ofis;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import androidx.core.app.NavUtils;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import com.olam.digital.ofis.Common.Adapter.MultiSelectionAdapter;
import com.olam.digital.ofis.Common.Model.MultiSpinnerStatus;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.model.Information;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Programme;

import java.util.ArrayList;
import java.util.List;


public class FindFarmerActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    private Context context;
    private Resources res;
    private Cursor farmerList;
    private DataLayer dl;
    private CustomListAdapter_Farmer customListAdapter_farmerAdapter;
    private EditText ed;
    private TextView advancedFilter;
    private Spinner farmerGroupSpinner, sectionSpinner, countrySpinner, productSpinner, regionSpinner, districtSpinner, placeSpinner, partnerSpinner, programSpinner,certificationSpinner;

    private LinearLayout advancedFilterLayout;
    private long selectedCountry=0, selectedProduct = 0;

    private List<Integer> selectedRegions = new ArrayList<Integer>();
    private List<Integer> selectedSections = new ArrayList<Integer>();

    private List<Integer> selectedFarmerGroups = new ArrayList<Integer>();

    private List<Integer> selectedDistricts = new ArrayList<Integer>();

    private List<Integer> selectedPlaces = new ArrayList<Integer>();

    private List<Integer> selectedProgrammes = new ArrayList<Integer>();

    private List<Integer> selectedPartners = new ArrayList<Integer>();
    private List<Integer> selectedCertifications = new ArrayList<Integer>();


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
    ArrayList<MultiSpinnerStatus> certificationStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> farmerGroupStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> sectionStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> programStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> partnerStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> placeStatus = new ArrayList<>();
    ArrayList<MultiSpinnerStatus> districtStatus = new ArrayList<>();

    ArrayList<MultiSpinnerStatus> regionStatus = new ArrayList<>();


    private OFISRoomDB ofisRoomDB;

    ListView listView;
    private InputFilter filter = (source, start, end, dest, dstart, dend) -> {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView=findViewById(R.id.list);
        context = this;
        res = getResources();

        dl = new DataLayer(res, context);

        farmerGroupSpinner = (Spinner) findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (Spinner) findViewById(R.id.spinner_section);
        countrySpinner = (Spinner) findViewById(R.id.spinner_country);
        productSpinner = (Spinner) findViewById(R.id.spinner_product);
        regionSpinner = (Spinner) findViewById(R.id.spinner_region);
        districtSpinner = (Spinner) findViewById(R.id.spinner_district);
        placeSpinner = (Spinner) findViewById(R.id.spinner_place);
        partnerSpinner = (Spinner) findViewById(R.id.spinner_partner);
        programSpinner =(Spinner) findViewById(R.id.spinner_program);
        certificationSpinner = (Spinner) findViewById(R.id.spinner_certification);
        advancedFilter = (TextView) findViewById(R.id.advancedFilters);
        advancedFilterLayout = (LinearLayout) findViewById(R.id.advancedFilterLayout);
        ed = (EditText) findViewById(R.id.findFarmerSearchBox);
        ed.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        ed.setFilters(new InputFilter[] { filter });
        Intent intent = this.getIntent();
        transactionMode = intent.getBooleanExtra(TRANSACTION_MODE, false);
        backtoFinder = intent.getBooleanExtra(BACK_TO_FINDER, false);
        transactionItemId = intent.getLongExtra(EDIT_TRANSACTION_ITEM_ID, 0);
        buyingAgentId = intent.getLongExtra(BUYING_AGENT_CONTEXT, 0);
        ofisRoomDB = OFISRoomDB.getDbInstance(context);
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

    private void bindFarmerList() {
        if (transactionMode) {
            farmerList = dl.getFindFarmerOrBuyingAgentList(buyingAgentId, searchQuery, 0, 0);
        } else {
            farmerList = dl.searchFarmerList(searchQuery, selectedCountry, selectedProduct,
                    selectedRegions, selectedDistricts, selectedPlaces, selectedPartners,
                    selectedProgrammes, selectedFarmerGroups, selectedSections, selectedCertifications);
        }
    }

    public void expandOrCollapse(View view){
        boolean visibilityresult = advancedFilterLayout.getVisibility() == View.GONE;
        advancedFilter.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibilityresult ? R.drawable.ic_task_drop_down_expand : R.drawable.ic_drop_down_collapse , 0);
        advancedFilterLayout.setVisibility(visibilityresult ? View.VISIBLE : View.GONE);
    }

    private void showFarmerList() {
        if (farmerList.getCount() > 0) {
            String[] adapterCols = new String[]{
                    "name",
                    "olam_farmer_id",
                    "country_id",
                    "odFarmerId"
            };
            int[] adapterRowViews = new int[]{R.id.farmerListFarmerName, R.id.farmerListOlamFarmerId,R.id.farmerListODFarmerId};
            customListAdapter_farmerAdapter = new CustomListAdapter_Farmer(context, farmerList, adapterCols, adapterRowViews, res, transactionMode);
           // setListAdapter(customListAdapter_farmerAdapter);
            listView.setAdapter(customListAdapter_farmerAdapter);
        }
    }

    private void setUpSearchFunctionality()
    {
        //Bind Countries
        Cursor countries = dl.getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        dl.bindDataDrivenSpinner(countrySpinner, countries, context, R.string.spinner_country_default, null, false);

        Cursor products = dl.getSpinnerData(OFISDBContract.ProductsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.ProductsEntry.COLUMN_NAME_PRODUCT_NAME);
        dl.bindDataDrivenSpinner(productSpinner, products, context, R.string.spinner_product_default, null, false);

        bindRegion();
        bindDistrict();
        bindPlace();
        bindProgram();
        bindPartner();
        bindFarmerGroups();
        bindCertificationNameSpinner();
        bindSection();

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 //Surround the certification spinner code in product code condition

                selectedCountry = id;
                //selectedProduct = 0;
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProduct = id;
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

    public void bindRegion(){
        //Bind Farmer Groups
        Cursor regionCursor =dl.getInfoFromTable(OFISDBContract.RegionsEntry.TABLE_NAME);
        //dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        List<Information> regionArrayList
                = new ArrayList<>();
        if (regionCursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                regionArrayList.add(new Information(
                        regionCursor.getInt(0),
                        regionCursor.getString(1)));
            } while (regionCursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        regionCursor.close();
        if(regionArrayList.size() > 0){
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_region_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            regionStatus.add(multiSpinnerStatusCn);
        }
        for (int i = 0; i < regionArrayList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(regionArrayList.get(i).getId());
            multiSpinnerStatus.setTitle(regionArrayList.get(i).getName());
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            regionStatus.add(multiSpinnerStatus);
        }

        MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                regionStatus,regionSpinner,null);
        myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
            @Override
            public void itemChanged(int id, boolean isChecked) {
                if(isChecked) {
                    selectedRegions.add(id);
                }else {
                    selectedRegions.remove(selectedRegions.indexOf(id));
                }
                doSearch();
            }
        });
        regionSpinner.setAdapter(myAdapter);

    }

    public void bindDistrict(){
        //Bind District
        Cursor districtCursor =dl.getInfoFromTable(OFISDBContract.DistrictsEntry.TABLE_NAME);
        List<Information> districtArrayList
                = new ArrayList<>();
        if (districtCursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                districtArrayList.add(new Information(
                        districtCursor.getInt(0),
                        districtCursor.getString(1)));
            } while (districtCursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        districtCursor.close();
        if(districtArrayList.size() > 0){
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_district_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            districtStatus.add(multiSpinnerStatusCn);
        }
        for (int i = 0; i < districtArrayList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(districtArrayList.get(i).getId());
            multiSpinnerStatus.setTitle(districtArrayList.get(i).getName());
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            districtStatus.add(multiSpinnerStatus);
        }

        MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                districtStatus,farmerGroupSpinner,null);

        myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
            @Override
            public void itemChanged(int id, boolean isChecked) {
                if(isChecked) {
                    selectedDistricts.add(id);
                }else {
                    selectedDistricts.remove(selectedDistricts.indexOf(id));
                }
                doSearch();
            }
        });

        districtSpinner.setAdapter(myAdapter);
    }

    public void bindPlace(){
        //Bind Place
        Cursor placeCursor =dl.getInfoFromTable(OFISDBContract.PlacesEntry.TABLE_NAME);
        List<Information> placeArrayList
                = new ArrayList<>();
        if (placeCursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                placeArrayList.add(new Information(
                        placeCursor.getInt(0),
                        placeCursor.getString(1)));
            } while (placeCursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        placeCursor.close();
        if(placeArrayList.size() > 0){
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_place_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            placeStatus.add(multiSpinnerStatusCn);
        }
        for (int i = 0; i < placeArrayList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(placeArrayList.get(i).getId());
            multiSpinnerStatus.setTitle(placeArrayList.get(i).getName());
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            placeStatus.add(multiSpinnerStatus);
        }

        MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                placeStatus,placeSpinner,null);
        myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
            @Override
            public void itemChanged(int id, boolean isChecked) {
                if(isChecked) {
                    selectedPlaces.add(id);
                }else {
                    selectedPlaces.remove(selectedPlaces.indexOf(id));
                }
                doSearch();
            }
        });
        placeSpinner.setAdapter(myAdapter);
    }
    public void bindPartner(){
        //Bind Partners
        List<Partners> partnersList = ofisRoomDB.partnersDao().getPartners();
        if(ofisRoomDB.partnersDao().getPartners().size()>0) {
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_partner_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            partnerStatus.add(multiSpinnerStatusCn);
            for (int i = 0; i < partnersList.size(); i++) {
                MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
                multiSpinnerStatus.setId(partnersList.get(i).id);
                multiSpinnerStatus.setTitle(partnersList.get(i).name);
                multiSpinnerStatus.setSelected(false);
                multiSpinnerStatus.setEnabled(true);
                partnerStatus.add(multiSpinnerStatus);
            }

            MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                    partnerStatus,partnerSpinner,null);
            myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
                @Override
                public void itemChanged(int id, boolean isChecked) {
                    if(isChecked) {
                        selectedPartners.add(id);
                    }else {
                        selectedPartners.remove(selectedPartners.indexOf(id));
                    }
                    doSearch();
                }
            });
            partnerSpinner.setAdapter(myAdapter);

        }
    }

    public void bindProgram(){
        //Bind Programs
        List<Programme> programmeList = ofisRoomDB.programmeDao().getProgramme();
        if(ofisRoomDB.programmeDao().getProgramme().size()>0) {
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_programme_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            programStatus.add(multiSpinnerStatusCn);
            for (int i = 0; i < programmeList.size(); i++) {
                MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
                multiSpinnerStatus.setId(programmeList.get(i).id);
                multiSpinnerStatus.setTitle(programmeList.get(i).name);
                multiSpinnerStatus.setSelected(false);
                multiSpinnerStatus.setEnabled(true);
                programStatus.add(multiSpinnerStatus);
            }

            MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                    programStatus,programSpinner,null);
            myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
                @Override
                public void itemChanged(int id, boolean isChecked) {
                    if(isChecked) {
                        selectedProgrammes.add(id);
                    }else {
                        selectedProgrammes.remove(selectedProgrammes.indexOf(id));
                    }
                    doSearch();
                }
            });
            programSpinner.setAdapter(myAdapter);

        }
    }

    public void bindFarmerGroups(){
        //Bind Farmer Groups
        Cursor farmerGroupsCursor =dl.getInfoFromTable(OFISDBContract.FarmerGroupsEntry.TABLE_NAME);
                //dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        List<Information> farmerGroupArrayList
                = new ArrayList<>();
        if (farmerGroupsCursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                farmerGroupArrayList.add(new Information(
                        farmerGroupsCursor.getInt(0),
                        farmerGroupsCursor.getString(1)));
            } while (farmerGroupsCursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        farmerGroupsCursor.close();
        if(farmerGroupArrayList.size() > 0){
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_farmergroup_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            farmerGroupStatus.add(multiSpinnerStatusCn);
        }
        for (int i = 0; i < farmerGroupArrayList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(farmerGroupArrayList.get(i).getId());
            multiSpinnerStatus.setTitle(farmerGroupArrayList.get(i).getName());
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            farmerGroupStatus.add(multiSpinnerStatus);
        }

        MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                farmerGroupStatus,farmerGroupSpinner,null);
        myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
            @Override
            public void itemChanged(int id, boolean isChecked) {
                if(isChecked) {
                    selectedFarmerGroups.add(id);
                }else {
                    selectedFarmerGroups.remove(selectedFarmerGroups.indexOf(id));
                }
                doSearch();
            }
        });
        farmerGroupSpinner.setAdapter(myAdapter);
    }

    public void bindSection(){
        //Bind sections
        Cursor sectionCursor =dl.getInfoFromTable(OFISDBContract.SectionsEntry.TABLE_NAME);
        List<Information> sectionArrayList
                = new ArrayList<>();
        if (sectionCursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                sectionArrayList.add(new Information(
                        sectionCursor.getInt(0),
                        sectionCursor.getString(1)));
            } while (sectionCursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        sectionCursor.close();
        if(sectionArrayList.size() > 0){
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.spinner_section_default));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            sectionStatus.add(multiSpinnerStatusCn);
        }
        for (int i = 0; i < sectionArrayList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(sectionArrayList.get(i).getId());
            multiSpinnerStatus.setTitle(sectionArrayList.get(i).getName());
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            sectionStatus.add(multiSpinnerStatus);
        }

        MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                sectionStatus,sectionSpinner,null);
        myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
            @Override
            public void itemChanged(int id, boolean isChecked) {
                if(isChecked) {
                    selectedSections.add(id);
                }else {
                    selectedSections.remove(selectedSections.indexOf(id));
                }
                doSearch();
            }
        });
        sectionSpinner.setAdapter(myAdapter);
    }
    public void bindCertificationNameSpinner(){
        List<Certifications> certificationsList = ofisRoomDB.certificationsDao().getCertifications();
        if(ofisRoomDB.certificationsDao().getCertifications().size() >0) {
            MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
            multiSpinnerStatusCn.setId(0);
            multiSpinnerStatusCn.setTitle(res.getString(R.string.certification_name));
            multiSpinnerStatusCn.setSelected(false);
            multiSpinnerStatusCn.setEnabled(true);
            certificationStatus.add(multiSpinnerStatusCn);
            for (int i = 0; i < certificationsList.size(); i++) {
                MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
                multiSpinnerStatus.setId(certificationsList.get(i).certificationId);
                multiSpinnerStatus.setTitle(certificationsList.get(i).certificationName);
                multiSpinnerStatus.setSelected(false);
                multiSpinnerStatus.setEnabled(true);
                certificationStatus.add(multiSpinnerStatus);
            }

            MultiSelectionAdapter myAdapter = new MultiSelectionAdapter(FindFarmerActivity.this, 0,
                    certificationStatus,certificationSpinner,null);
            myAdapter.setOnItemChangedListener(new MultiSelectionAdapter.OnItemChangedListener() {
                @Override
                public void itemChanged(int id, boolean isChecked) {
                    if(isChecked) {
                        selectedCertifications.add(id);
                    }else {
                        selectedCertifications.remove(selectedCertifications.indexOf(id));
                    }
                    doSearch();
                }
            });
            certificationSpinner.setAdapter(myAdapter);

        }else{
            certificationSpinner.setVisibility(View.INVISIBLE);
        }
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
