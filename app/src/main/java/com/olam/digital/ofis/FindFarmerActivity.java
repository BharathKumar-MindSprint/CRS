package com.olam.digital.ofis;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.olam.digital.ofis.downloadsurvey.ViewPlotSurveyActivity;
import com.olam.digital.ofis.model.Information;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Programme;

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
    private LinearLayout farmerTopSection;
    private MultiSpinnerSearch farmerGroupSpinner, sectionSpinner, regionSpinner, districtSpinner, placeSpinner, partnerSpinner, programSpinner,certificationSpinner;

    private Spinner countrySpinner, productSpinner;
    private LinearLayout advancedFilterLayout;
    private ScrollView advancedFilters;
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
    ArrayList<KeyPairBoolData> certificationStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> farmerGroupStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> sectionStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> programStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> partnerStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> placeStatus = new ArrayList<>();
    ArrayList<KeyPairBoolData> districtStatus = new ArrayList<>();

    ArrayList<KeyPairBoolData> regionStatus = new ArrayList<>();

    public static String notificationType;
    private ImageButton photoSearchButton;
    private TextView farmerNotFoundText;
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

        farmerGroupSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_section);
        countrySpinner = (Spinner) findViewById(R.id.spinner_country);
        productSpinner = (Spinner) findViewById(R.id.spinner_product);
        regionSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_region);
        districtSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_district);
        placeSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_place);
        partnerSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_partner);
        programSpinner =(MultiSpinnerSearch) findViewById(R.id.spinner_program);
        certificationSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner_certification);
        advancedFilter = (TextView) findViewById(R.id.advancedFilters);
        advancedFilterLayout = (LinearLayout) findViewById(R.id.advancedFilterLayout);
        farmerTopSection = (LinearLayout) findViewById(R.id.topSection);
        advancedFilters = (ScrollView) findViewById(R.id.svAdvancedFilters);
        farmerNotFoundText = (TextView) findViewById(R.id.tvNoFarmerFound);
        photoSearchButton = (ImageButton) findViewById(R.id.photoSearchButton);
        ed = (EditText) findViewById(R.id.findFarmerSearchBox);
        ed.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        ed.setFilters(new InputFilter[] { filter });
        Intent intent = this.getIntent();
        transactionMode = intent.getBooleanExtra(TRANSACTION_MODE, false);
        backtoFinder = intent.getBooleanExtra(BACK_TO_FINDER, false);
        transactionItemId = intent.getLongExtra(EDIT_TRANSACTION_ITEM_ID, 0);
        buyingAgentId = intent.getLongExtra(BUYING_AGENT_CONTEXT, 0);
        notificationType = intent.getStringExtra(Utilities.NOTIFICATION_TYPE);
        ofisRoomDB = OFISRoomDB.getDbInstance(context);
        if (transactionMode) {
            this.setTitle(R.string.title_activity_find_transactee);
            TextView pageTitle = (TextView) findViewById(R.id.pageTitle);
            pageTitle.setText(res.getString(R.string.select_transactee));

            ed.setHint(R.string.farmer_or_buying_agent_name);
        }

        if (notificationType == null) {
            notificationType = Utilities.notificationType;
            Utilities.notificationType = null;
        }

        bindFarmerList();
        showFarmerList();

        if (notificationType == null) {
            ed.setHint(res.getString(R.string.Farmer_Name));
            setUpSearchFunctionality();
        }
        else {
            advancedFilter.setVisibility(View.GONE);
            advancedFilters.setVisibility(View.GONE);
            photoSearchButton.setVisibility(View.GONE);

            setUpSearchFunctionalityForNotification();
        }
        listView.setOnItemClickListener(this);
    }

    private void bindFarmerList() {
        if (transactionMode) {
            farmerList = dl.getFindFarmerOrBuyingAgentList(buyingAgentId, searchQuery, 0, 0);
        } else if (notificationType == null) {
            farmerList = dl.searchFarmerList(searchQuery, selectedCountry, selectedProduct,
                    selectedRegions, selectedDistricts, selectedPlaces, selectedPartners,
                    selectedProgrammes, selectedFarmerGroups, selectedSections, selectedCertifications);
        }
        else {
            farmerList = ofisRoomDB.onGroundSurveyNotificationDao().getFarmersBySurveyType(notificationType, searchQuery, selectedCountry, selectedProduct);
        }

        if (farmerList.getCount() > 0) {
            listView.setVisibility(View.VISIBLE);
            farmerNotFoundText.setVisibility(View.GONE);
        }
        else {
            listView.setVisibility(View.GONE);
            farmerNotFoundText.setVisibility(View.VISIBLE);
        }
    }

    public void expandOrCollapse(View view){
        boolean visibilityresult = advancedFilterLayout.getVisibility() == View.GONE;
        advancedFilter.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibilityresult ? R.drawable.ic_task_drop_down_expand : R.drawable.ic_drop_down_collapse , 0);
        advancedFilterLayout.setVisibility(visibilityresult ? View.VISIBLE : View.GONE);
        if (visibilityresult){
            farmerTopSection.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1000));

        }else {
            farmerTopSection.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

        }
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
            listView.setAdapter(customListAdapter_farmerAdapter);
        }
    }

    private void setUpSearchFunctionality() {
        //Bind Countries
        Cursor countries = dl.getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        dl.bindDataDrivenSpinner(countrySpinner, countries, context, R.string.spinner_country_view_farmer, null, false);

        Cursor products = dl.getSpinnerData(OFISDBContract.ProductsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.ProductsEntry.COLUMN_NAME_PRODUCT_NAME);
        dl.bindDataDrivenSpinner(productSpinner, products, context, R.string.spinner_product_view_farmer, null, false);

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

    private void setUpSearchFunctionalityForNotification() {
        List<Integer> farmerGroupIds = ofisRoomDB.onGroundSurveyNotificationDao().getFarmerGroupsBasedOnSurveyType(notificationType);
        Cursor farmerGroups = dl.getComplianceFarmerGroupsCursor(farmerGroupIds);
        dl.bindDataDrivenSpinner(countrySpinner, farmerGroups, context, R.string.spinner_farmergroup_default, null, false);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor sections = dl.getSpinnerData(OFISDBContract.SectionsEntry.TABLE_NAME, true, OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID, ((int) id), true, OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME);
                if (position == 0) {
                    sections = dl.getSectionByFarmerGroup(farmerGroupIds);
                }
                dl.bindDataDrivenSpinner(productSpinner, sections, context, R.string.spinner_section_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

                selectedCountry = id;
                selectedProduct = 0;
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
                searchQuery = String.valueOf(ed.getText());
                doSearch();
            }
        });

    }

    public void bindSpinnerSpec(MultiSpinnerSearch multiSpinnerSearch, String hintText){

        multiSpinnerSearch.setSearchEnabled(true);

        // A text that will display in search hint.
        multiSpinnerSearch.setSearchHint(hintText);

        // Set text that will display when search result not found...
        multiSpinnerSearch.setEmptyTitle("Not Data Found!");

        // If you will set the limit, this button will not display automatically.
        multiSpinnerSearch.setShowSelectAllButton(false);

        //A text that will display in clear text button
        multiSpinnerSearch.setClearText(null);
    }

    public void bindRegion(){
        bindSpinnerSpec(regionSpinner,res.getString(R.string.spinner_region_default));
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
        for (int i = 0; i < regionArrayList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(regionArrayList.get(i).getId());
            keyPairBoolData.setName(regionArrayList.get(i).getName());
            keyPairBoolData.setSelected(false);
            regionStatus.add(keyPairBoolData);
        }

        regionSpinner.setItems(regionStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedRegions.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                        selectedRegions.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedRegions.remove(selectedRegions.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                    }

                }
                doSearch();
            }
        });
    }

    public void bindDistrict(){
        bindSpinnerSpec(districtSpinner,res.getString(R.string.spinner_district_default));
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
        for (int i = 0; i < districtArrayList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(districtArrayList.get(i).getId());
            keyPairBoolData.setName(districtArrayList.get(i).getName());
            keyPairBoolData.setSelected(false);
            districtStatus.add(keyPairBoolData);
        }

        districtSpinner.setItems(districtStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedDistricts.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                        selectedDistricts.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedDistricts.remove(selectedDistricts.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                    }
                }
                doSearch();
            }
        });
    }

    public void bindPlace(){
        bindSpinnerSpec(placeSpinner,res.getString(R.string.spinner_place_default));
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
        for (int i = 0; i < placeArrayList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(placeArrayList.get(i).getId());
            keyPairBoolData.setName(placeArrayList.get(i).getName());
            keyPairBoolData.setSelected(false);
            placeStatus.add(keyPairBoolData);
        }

        placeSpinner.setItems(placeStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedPlaces.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                        selectedPlaces.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedPlaces.remove(selectedPlaces.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                    }
                }
                doSearch();
            }
        });
    }
    public void bindPartner(){

        bindSpinnerSpec(partnerSpinner,res.getString(R.string.spinner_partner_default));
        //Bind Partners
        List<Partners> partnersList = ofisRoomDB.partnersDao().getPartners();
        if(ofisRoomDB.partnersDao().getPartners().size()>0) {
            for (int i = 0; i < partnersList.size(); i++) {
                KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
                keyPairBoolData.setId(partnersList.get(i).id);
                keyPairBoolData.setName(partnersList.get(i).name);
                keyPairBoolData.setSelected(false);
                partnerStatus.add(keyPairBoolData);
            }

            partnerSpinner.setItems(partnerStatus, new MultiSpinnerListener() {
                @Override
                public void onItemsSelected(List<KeyPairBoolData> items) {
                    selectedPartners.clear();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                            selectedPartners.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                        }else {
                            selectedPartners.remove(selectedPartners.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                        }
                    }
                    doSearch();
                }
            });
        }
    }

    public void bindProgram(){
        bindSpinnerSpec(programSpinner,res.getString(R.string.spinner_programme_default));
        //Bind Programs
        List<Programme> programmeList = ofisRoomDB.programmeDao().getProgramme();
        if(ofisRoomDB.programmeDao().getProgramme().size()>0) {

            for (int i = 0; i < programmeList.size(); i++) {
                KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
                keyPairBoolData.setId(programmeList.get(i).id);
                keyPairBoolData.setName(programmeList.get(i).name);
                keyPairBoolData.setSelected(false);
                programStatus.add(keyPairBoolData);
            }

            programSpinner.setItems(programStatus, new MultiSpinnerListener() {
                @Override
                public void onItemsSelected(List<KeyPairBoolData> items) {
                    selectedProgrammes.clear();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                            selectedProgrammes.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                        }else {
                            selectedProgrammes.remove(selectedProgrammes.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                        }
                    }
                    doSearch();
                }
            });
        }
    }

    public void bindFarmerGroups(){
        bindSpinnerSpec(farmerGroupSpinner,res.getString(R.string.spinner_farmergroup_default));
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
        for (int i = 0; i < farmerGroupArrayList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(farmerGroupArrayList.get(i).getId());
            keyPairBoolData.setName(farmerGroupArrayList.get(i).getName());
            keyPairBoolData.setSelected(false);
            farmerGroupStatus.add(keyPairBoolData);
        }

        farmerGroupSpinner.setItems(farmerGroupStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedFarmerGroups.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                        selectedFarmerGroups.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedFarmerGroups.remove(selectedFarmerGroups.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                    }
                }
                doSearch();
            }
        });
    }

    public void bindSection(){
        bindSpinnerSpec(sectionSpinner,res.getString(R.string.spinner_section_default));
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
        for (int i = 0; i < sectionArrayList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(sectionArrayList.get(i).getId());
            keyPairBoolData.setName(sectionArrayList.get(i).getName());
            keyPairBoolData.setSelected(false);
            sectionStatus.add(keyPairBoolData);
        }

        sectionSpinner.setItems(sectionStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedSections.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                        selectedSections.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedSections.remove(selectedSections.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                    }
                }
                doSearch();
            }
        });
    }
    public void bindCertificationNameSpinner(){
        bindSpinnerSpec(certificationSpinner,res.getString(R.string.certification_name));
        List<Certifications> certificationsList = ofisRoomDB.certificationsDao().getCertifications();
        if(ofisRoomDB.certificationsDao().getCertifications().size() >0) {
            for (int i = 0; i < certificationsList.size(); i++) {
                KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
                keyPairBoolData.setId(certificationsList.get(i).certificationId);
                keyPairBoolData.setName(certificationsList.get(i).certificationName);
                keyPairBoolData.setSelected(false);
                certificationStatus.add(keyPairBoolData);
            }

            certificationSpinner.setItems(certificationStatus, new MultiSpinnerListener() {
                @Override
                public void onItemsSelected(List<KeyPairBoolData> items) {
                    selectedCertifications.clear();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isSelected()) {
//                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                            selectedCertifications.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                        }else {
                            selectedCertifications.remove(selectedCertifications.indexOf(Integer.parseInt(String.valueOf(items.get(i).getId()))));
                        }
                    }
                    doSearch();
                }
            });
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        notificationType = null;
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
            context.startActivity(intent);
        } else if (notificationType == null) {
            ViewFarmerActivity.farmerId = id;
            ViewFarmerActivity.notificationType = null;
            intent = new Intent(context, ViewFarmerActivity.class);
            intent.putExtra(Utilities.IS_FROM_FIND_FARMER, true);
            context.startActivity(intent);
        }
        else {
            ViewFarmerActivity.farmerId = id;
            ViewFarmerActivity.notificationType = notificationType;
            intent = new Intent(context, ViewPlotSurveyActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(Utilities.FARMER_ID, id);
            bundle.putBoolean(res.getString(R.string.dm_plot_survey), true);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}
