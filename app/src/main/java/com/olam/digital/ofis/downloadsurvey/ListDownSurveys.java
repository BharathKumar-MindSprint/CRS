package com.olam.digital.ofis.downloadsurvey;
import static com.olam.digital.ofis.Common.Utils.NetworkUtils.isNetworkAvailable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.adapter.FilterModuleRecyclerAdapter;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemSurveyClickListener;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.PolygonFarmer;

import java.util.ArrayList;
import java.util.Objects;

public class ListDownSurveys extends BaseActivity {

    private RecyclerView moduleListRecyclerView;
    private FilterModuleRecyclerAdapter filterModuleRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        populateModuleList();
    }

    private void populateModuleList() {
        try {
            ArrayList<ModuleDTO> duplicateList = dl.viewModuleList(context);
            ArrayList<ModuleDTO> moduleList = dl.removeDuplicateModule(duplicateList);
            if (moduleList != null && moduleList.size() > 0) {
                filterModuleRecyclerAdapter = new FilterModuleRecyclerAdapter(moduleList, this);
                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                moduleListRecyclerView.setLayoutManager(reLayoutManager);
                DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_seperator_divider));
                moduleListRecyclerView.addItemDecoration(itemDecoration);

                moduleListRecyclerView.setItemAnimator(new DefaultItemAnimator());
                ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
                moduleListRecyclerView.setAdapter(filterModuleRecyclerAdapter);
                moduleListRecyclerView.scrollToPosition(filterModuleRecyclerAdapter.getItemCount() - 1);
                filterModuleRecyclerAdapter.setOnItemClickListener(new OnItemSurveyClickListener() {
                    @Override
                    public void onModuleClick(View view, int position, ModuleDTO moduleDTO) {

                        if (Objects.equals(moduleDTO.getShowType(), Utilities.TYPE_POLYGON)) {
                            if (isNetworkAvailable(context)) {
                                startActivity(new Intent(ListDownSurveys.this, PolygonFarmer.class));
                            } else {
                                Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("module", moduleDTO);
                            startActivity(new Intent(ListDownSurveys.this, DownloadSurveyData.class).putExtras(bundle));
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init() {
        dl = new DataLayer(res, this);
        moduleListRecyclerView = (RecyclerView) findViewById(R.id.listModule);
    }






}

