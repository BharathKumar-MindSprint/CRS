package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseListActivity;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleCursorToDTO;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.FarmerPolygonDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PolygonFarmer extends BaseListActivity {

    private EditText findFarmerSearchBox;
    private RecyclerView listFarmer;

    private DataLayer dl;
    private DownloadPolygonAdapter downloadPolygonAdapter;

    private TextView farmerNotFoundText;
    private Button downloadDataPolygon;
    Cursor getFarmerWithPlots;

    List<FarmerPolygonDto> farmerListDto;
    private String searchQuery = "";
    private long selectedCountry=0, selectedProduct = 0;

    private List<Integer> selectedRegions = new ArrayList<Integer>();
    private List<Integer> selectedSections = new ArrayList<Integer>();

    private List<Integer> selectedFarmerGroups = new ArrayList<Integer>();

    private List<Integer> selectedDistricts = new ArrayList<Integer>();

    private List<Integer> selectedPlaces = new ArrayList<Integer>();

    private List<Integer> selectedProgrammes = new ArrayList<Integer>();

    private List<Integer> selectedPartners = new ArrayList<Integer>();
    private List<Integer> selectedCertifications = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findFarmerSearchBox = (EditText) findViewById(R.id.findFarmerSearchBoxPolygon);
        downloadDataPolygon = (Button) findViewById(R.id.downloadDataPolygon);
        listFarmer = (RecyclerView) findViewById(R.id.listFarmer);
        farmerNotFoundText = (TextView) findViewById(R.id.noFarmerFound);
        findFarmerSearchBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        findFarmerSearchBox.setFilters(new InputFilter[] { Utilities.filter });
        findFarmerSearchBox.setHint(getResources().getString(R.string.Farmer_Name));

        dl = new DataLayer(getResources(), this);

        bindFarmerList();
        if (!farmerListDto.isEmpty()) {
            listFarmer.setLayoutManager(new LinearLayoutManager(this));
            downloadPolygonAdapter = new DownloadPolygonAdapter(farmerListDto, this, dl);
            listFarmer.setAdapter(downloadPolygonAdapter);
        }

        findFarmerSearchBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {  }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                searchQuery = String.valueOf(findFarmerSearchBox.getText());
                doSearch();
            }
        });

        downloadDataPolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<Long, Long> selectedPlotSizes = downloadPolygonAdapter.getSelectedPlotSizes();
                if (selectedPlotSizes.isEmpty()) {
                    Toast.makeText(PolygonFarmer.this, getResources().getString(R.string.no_plot_selected), Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Long id : selectedPlotSizes.keySet()) {
                        sb.append(id).append(",");
                    }
                    String finalString = sb.toString().replaceAll(",$", "");
                    Log.d("Final String", finalString);
                }
            }
        });

    }

    private void bindFarmerList() {
        getFarmerWithPlots = dl.searchFarmerList(searchQuery, selectedCountry, selectedProduct,
                selectedRegions, selectedDistricts, selectedPlaces, selectedPartners,
                selectedProgrammes, selectedFarmerGroups, selectedSections, selectedCertifications, true);


        farmerListDto = ModuleCursorToDTO.cursorToDtoFarmerList(getFarmerWithPlots);

        if (!farmerListDto.isEmpty()) {
            listFarmer.setVisibility(View.VISIBLE);
            farmerNotFoundText.setVisibility(View.GONE);
        }
        else {
            listFarmer.setVisibility(View.GONE);
            farmerNotFoundText.setVisibility(View.VISIBLE);
        }
    }


    private void doSearch()
    {
        getFarmerWithPlots.close();
        bindFarmerList();
        runOnUiThread(returnRes);
    }

    private final Runnable returnRes = new Runnable() {

        public void run() {
            if (downloadPolygonAdapter != null) {
                downloadPolygonAdapter.swapCursor(farmerListDto);
            }

        }
    };
}