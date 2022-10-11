package com.olam.digital.ofis.downloadsurvey;

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
import com.olam.digital.ofis.downloadsurvey.adapter.FilterModuleRecyclerAdapter;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemSurveyClickListener;

import java.util.ArrayList;

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
        ArrayList<ModuleDTO> duplicateList = dl.viewModuleList();
        ArrayList<ModuleDTO> moduleList = dl.removeDuplicateModule(duplicateList);
        if(moduleList!=null && moduleList.size()>0){
            filterModuleRecyclerAdapter = new FilterModuleRecyclerAdapter(moduleList,this);
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
                    /*Toast.makeText(ListDownSurveys.this, moduleDTO.getName() +"\n id type:"
                                    + moduleDTO.getModule_id() + "\n"
                                    + moduleDTO.getModuleassignment_type() + "\n"
                                    + moduleDTO.getModuleassignment_id()
                            ,Toast.LENGTH_LONG).show();*/
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("module", moduleDTO);
                    startActivity(new Intent(ListDownSurveys.this, DownloadSurveyData.class).putExtras(bundle));
                }
            });

        }
    }

    private void init() {
        dl = new DataLayer(res, this);
        moduleListRecyclerView = (RecyclerView) findViewById(R.id.listModule);
    }






}

