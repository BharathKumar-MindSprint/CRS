package com.olam.digital.ofis.agrisupplier.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.MultiSpinnerSearch;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.agrisupplier.adapter.SupplierListAdapter;
import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;
import com.olam.digital.ofis.agrisupplier.dto.SupplierTypeDto;
import com.olam.digital.ofis.agrisupplier.listener.SupplierItemClickListener;
import com.olam.digital.ofis.model.Information;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FindSupplierAndSurveyActivity extends BaseListActivity {

    private ConstraintLayout advancedFilterLayout;
    private TextView tvAdvancedFilters;
    private RecyclerView rvSupplierList;
    private TextView tvSupplierNotFound;
    private List<SupplierDto> supplierList = new ArrayList<>();
    private Context context;
    private OFISRoomDB ofisRoomDb;
    private DataLayer dl;
    private Resources res;
    private Spinner countrySpinner, productSpinner, supplierType;
    private MultiSpinnerSearch supplierGroup;
    private long selectedCountry = 0, selectedProduct = 0, selectedSupplierType = 0;
    private EditText etSupplierName;
    private String searchQuery = "";

    private SupplierListAdapter adapter;
    ArrayList<KeyPairBoolData> supplierGroupStatus = new ArrayList<>();
    List<Integer> selectedSupplierGroups = new ArrayList<>();

    List<SupplierTypeDto> supplierTypeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_supplier_and_survey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        ofisRoomDb = OFISRoomDB.getDbInstance(context);
        res = getResources();
        dl = new DataLayer(res, context);
        initializeWidgets();

        showSupplierList();
        bindSupplierList();
        setUpSearchAndFilterFunctionality();
    }

    private void initializeWidgets() {
        advancedFilterLayout = findViewById(R.id.clAdvancedFilterLayout);
        tvAdvancedFilters = findViewById(R.id.tvAdvancedFilters);
        rvSupplierList = findViewById(R.id.rvSupplierList);
        tvSupplierNotFound = findViewById(R.id.tvSupplierNotFound);
        countrySpinner = findViewById(R.id.spinner_country);
        productSpinner = findViewById(R.id.spinner_product);
        supplierType = findViewById(R.id.supplierType);
        supplierGroup = findViewById(R.id.supplierGroup);
        etSupplierName = findViewById(R.id.etSupplierName);
        etSupplierName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etSupplierName.setFilters(new InputFilter[] { filter });
    }

    private void bindSupplierList() {
        supplierList = ofisRoomDb.supplierDao().getAllSuppliers(selectedCountry, selectedProduct, selectedSupplierType, selectedSupplierGroups,  selectedSupplierGroups.isEmpty() ? 0 : selectedSupplierGroups.size(), searchQuery);

        if (supplierList.isEmpty()) {
            rvSupplierList.setVisibility(View.GONE);
            tvSupplierNotFound.setVisibility(View.VISIBLE);
        }
        else {
            adapter.updateSupplierList(supplierList);
            rvSupplierList.setVisibility(View.VISIBLE);
            tvSupplierNotFound.setVisibility(View.GONE);
        }

    }

    private void showSupplierList() {
        rvSupplierList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupplierListAdapter(supplierList);
        rvSupplierList.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_divider));
        rvSupplierList.addItemDecoration(decoration);

        adapter.setOnItemClickListener(new SupplierItemClickListener() {
            @Override
            public void onItemClick(SupplierDto supplier) {
                Intent intent = new Intent(context, ViewSupplierActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Utilities.SUPPLIER, supplier);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void setUpSearchAndFilterFunctionality() {
        Cursor countries = dl.getSpinnerData(OFISDBContract.CountriesEntry.TABLE_NAME, true, OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY, 1, true, OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME);
        dl.bindDataDrivenSpinner(countrySpinner, countries, context, R.string.spinner_country_view_farmer, null, false);

        Cursor products = dl.getSpinnerData(OFISDBContract.ProductsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.ProductsEntry.COLUMN_NAME_PRODUCT_NAME);
        dl.bindDataDrivenSpinner(productSpinner, products, context, R.string.spinner_product_view_farmer, null, false);

        bindSupplierType();
        bindSupplierGroups();

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = id;
                bindSupplierList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProduct = id;
                bindSupplierList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etSupplierName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {  }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = String.valueOf(etSupplierName.getText());
                bindSupplierList();
            }
        });
    }

    public void bindSupplierType() {
        supplierTypeList = ofisRoomDb.supplierTypeDao().getAllSupplierType();
        List<String> supplierTypeNames = ofisRoomDb.supplierTypeDao().getAllSupplierTypeNames();
        supplierTypeNames.add(0, res.getString(R.string.supplier_type));

        if (!supplierTypeList.isEmpty()) {
            supplierTypeList.remove(supplierTypeList.size() - 1);
            supplierTypeNames.remove(supplierTypeNames.size() - 1);
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, supplierTypeNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        supplierType.setAdapter(adapter);
        supplierType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                selectedSupplierType = i == 0 ? 0 : supplierTypeList.get(i - 1).getSupplierTypeId();
                bindSupplierGroups();
                bindSupplierList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void bindSupplierGroups(){
        bindSpinnerSpec(supplierGroup,res.getString(R.string.spinner_supplier_group));
        selectedSupplierGroups.clear();
        List<Information> supplierGroupList = ofisRoomDb.supplierGroupDao().getSupplierGroups(selectedSupplierType);
        supplierGroupStatus = new ArrayList<>();

        for (int i = 0; i < supplierGroupList.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(supplierGroupList.get(i).getId());
            keyPairBoolData.setName(supplierGroupList.get(i).getName());
            keyPairBoolData.setSelected(false);
            supplierGroupStatus.add(keyPairBoolData);
        }

        supplierGroup.setItems(supplierGroupStatus, new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                selectedSupplierGroups.clear();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
                        selectedSupplierGroups.add(Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }else {
                        selectedSupplierGroups.remove((Integer) Integer.parseInt(String.valueOf(items.get(i).getId())));
                    }
                }
                bindSupplierList();
            }
        });
    }

    public void bindSpinnerSpec(MultiSpinnerSearch multiSpinnerSearch, String hintText){
        multiSpinnerSearch.setSearchEnabled(true);
        multiSpinnerSearch.setSearchHint(hintText);
        multiSpinnerSearch.setEmptyTitle("Not Data Found!");
        multiSpinnerSearch.setShowSelectAllButton(false);
        multiSpinnerSearch.setClearText(null);
    }

    public void expandOrCollapse(View view){
        boolean visibilityResult = advancedFilterLayout.getVisibility() == View.GONE;
        tvAdvancedFilters.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibilityResult ? R.drawable.ic_task_drop_down_expand : R.drawable.ic_drop_down_collapse , 0);
        advancedFilterLayout.setVisibility(visibilityResult ? View.VISIBLE : View.GONE);
    }

    private final InputFilter filter = (source, start, end, dest, dstart, dend) -> {

        String blockCharacterSet = "~#^|$%&*!'\"";
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

}