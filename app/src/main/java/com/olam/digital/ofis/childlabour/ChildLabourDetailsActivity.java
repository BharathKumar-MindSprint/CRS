package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.adapter.ChildLabourRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.io.Serializable;
import java.util.List;

public class ChildLabourDetailsActivity extends BaseActivity {

    private RecyclerView childLabourDetailsRecyclerView;
    Long farmerId;
    private ChildLabourRecyclerAdapter childLabourRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_labour_details);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){}
        populateChildList();
    }
    private void populateChildList() {
        ChildLabourLogic childLabourLogic=new ChildLabourLogic();
        List<ChildrenDTO> childrenList=childLabourLogic.getChildDetailsById(this,farmerId);
        if(childrenList!=null && childrenList.size()>0){
            childLabourRecyclerAdapter = new ChildLabourRecyclerAdapter(childrenList,this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            childLabourDetailsRecyclerView.setLayoutManager(reLayoutManager);
            childLabourDetailsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
            childLabourDetailsRecyclerView.setAdapter(childLabourRecyclerAdapter);

            childLabourRecyclerAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onChildClick(View view, int position,ChildrenDTO childrenDTO) {
                    // Moving to Survey Part
                    Bundle bundle=new Bundle();
                    bundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerId));
                    bundle.putString(ChildLabourUtils.CHILD_ID,String.valueOf(childrenDTO.getId()));
                    bundle.putSerializable("children", childrenDTO);

                    startActivity(new Intent(ChildLabourDetailsActivity.this,ViewChildLabourSurveyActivity.class).putExtras(bundle));
                }
            });
        }
    }
    private void initComponents() {
        childLabourDetailsRecyclerView=findViewById(R.id.childLabourDetailsRecycleView);
    }
}
