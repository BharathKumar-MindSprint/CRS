package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import static com.olam.digital.ofis.Common.Utils.NetworkUtils.isNetworkAvailable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.app.AlertDialog;
import android.database.Cursor;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.EditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseListActivity;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleCursorToDTO;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.FarmerPolygonDto;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall.PolygonHistoryResponseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Set;

public class PolygonFarmer extends BaseListActivity {

    private EditText findFarmerSearchBox;
    private RecyclerView listFarmer;

    private DataLayer dl;
    private DownloadPolygonAdapter downloadPolygonAdapter;

    private TextView farmerNotFoundText, selectedPlotCount;
    private Button downloadDataPolygon;
    Cursor getFarmerWithPlots;
    List<FarmerPolygonDto> farmerListDto;
    private String searchQuery = "";

    private ProgressDialog progressDialog;
    private long selectedCountry=0, selectedProduct = 0;

    private List<Integer> selectedRegions = new ArrayList<Integer>();
    private List<Integer> selectedSections = new ArrayList<Integer>();

    private List<Integer> selectedFarmerGroups = new ArrayList<Integer>();

    private List<Integer> selectedDistricts = new ArrayList<Integer>();

    private List<Integer> selectedPlaces = new ArrayList<Integer>();

    private List<Integer> selectedProgrammes = new ArrayList<Integer>();

    private List<Integer> selectedPartners = new ArrayList<Integer>();
    private List<Integer> selectedCertifications = new ArrayList<Integer>();

    private Context context;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        res = getResources();
        dl = new DataLayer(res, context);

        findFarmerSearchBox = (EditText) findViewById(R.id.findFarmerSearchBoxPolygon);
        downloadDataPolygon = (Button) findViewById(R.id.downloadDataPolygon);
        listFarmer = (RecyclerView) findViewById(R.id.listFarmer);
        farmerNotFoundText = (TextView) findViewById(R.id.noFarmerFound);
        selectedPlotCount = (TextView) findViewById(R.id.selected_plot_count);
        findFarmerSearchBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        findFarmerSearchBox.setFilters(new InputFilter[] { Utilities.filter });
        findFarmerSearchBox.setHint(getResources().getString(R.string.Farmer_Name));

        dl = new DataLayer(getResources(), this);
        selectedPlotCount.setText(getString(R.string.selected_plot) + 0);

        bindFarmerList();
        if (!farmerListDto.isEmpty()) {
            listFarmer.setLayoutManager(new LinearLayoutManager(this));
            downloadPolygonAdapter = new DownloadPolygonAdapter(farmerListDto, this, dl);
            listFarmer.setAdapter(downloadPolygonAdapter);
            downloadPolygonAdapter.setOnSelectedPlotSizesChangeListener(new DownloadPolygonAdapter.OnSelectedPlotSizesChangeListener() {
                @Override
                public void onSelectedPlotSizesChanged(Map<Long, Long> updatedPlotSizes) {
                    updateSelectedPlotCount(updatedPlotSizes.size());
                }
            });
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
                    getPlotDetails(selectedPlotSizes.keySet());
                }
            }
        });
    }

    public void updateSelectedPlotCount(int size) {
        selectedPlotCount.setText(getString(R.string.selected_plot) + size);
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
        @Override
        public void run() {
            if (downloadPolygonAdapter != null) {
                downloadPolygonAdapter.swapCursor(farmerListDto);
            }
        }
    };

    private void getPlotDetails(Set<Long> plotIds) {
        if (isNetworkAvailable(context)) {
            ManageRecords retrofit = RetrofitClient.getRetrofit().create(ManageRecords.class);
            retrofit2.Call<PolygonHistoryResponseModel> call = retrofit.getPolygonDataByPlotId(plotIds, 1);
            showDialogPlot(true, false, null);
            try {
                call.enqueue(new Callback<PolygonHistoryResponseModel>() {
                    @Override
                    public void onResponse(Call<PolygonHistoryResponseModel> call, Response<PolygonHistoryResponseModel> response) {
                        if (response.isSuccessful()) {
                            PolygonHistoryResponseModel plotDetails = response.body();
                            if (plotDetails != null) {
                                dl.updatePositionData(plotDetails.getPlot());
                            }
                            showDialogPlot(false, false, null);
                        } else {
                            showDialogPlot(false, true, getResources().getString(R.string.item_sync_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<PolygonHistoryResponseModel> call, Throwable t) {
                        showDialogPlot(false, true, getResources().getString(R.string.item_sync_error));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public void showDialogPlot(boolean isDownloading, boolean isError, String errorMessage) {
        if (isDownloading) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.downloading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.dialog_confirmation, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(alertLayout);
            alertDialogBuilder.setCancelable(false);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button doneButton = alertLayout.findViewById(R.id.alertDoneButton);

            if (isError) {
                TextView alertTitle = alertLayout.findViewById(R.id.alertTitle);
                TextView alertMessage = alertLayout.findViewById(R.id.alertMessage);
                alertMessage.setTextColor(getResources().getColor(R.color.ofis_text_field));
                alertTitle.setText(getResources().getString(R.string.download_polygon_data));
                alertMessage.setText(errorMessage);
            }

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isError) {
                        startActivity(new Intent(PolygonFarmer.this, HomeActivity.class));
                    }
                    alertDialog.dismiss();
                }
            });
        }
    }
}