package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.adapter.ChildLabourRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.util.List;

public class RegisterChildActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView childRecyclerView;
    private LinearLayout addChildButton;
    private  LinearLayout childVisitDetailButton;
    private ChildLabourRecyclerAdapter childLabourRecyclerAdapter;
    Long farmerId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);
        initComponents();
        initEvents();
        farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        populateChildList();

    }

    private void populateChildList() {
        ChildLabourLogic childLabourLogic=new ChildLabourLogic();
        List<ChildrenDTO> childrenList=childLabourLogic.getChildDetailsById(this,farmerId);
        if(childrenList!=null && childrenList.size()>0){
            childLabourRecyclerAdapter = new ChildLabourRecyclerAdapter(childrenList,this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            childRecyclerView.setLayoutManager(reLayoutManager);
            childRecyclerView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
            childRecyclerView.setAdapter(childLabourRecyclerAdapter);

            childLabourRecyclerAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onChildClick(View view, int position,ChildrenDTO childrenDTO) {
                    // Edit functionalities
                    Toast.makeText(RegisterChildActivity.this,String.valueOf(position),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void initEvents() {
        addChildButton.setOnClickListener(this);
        childVisitDetailButton.setOnClickListener(this);
    }

    private void initComponents() {
        childRecyclerView=findViewById(R.id.registerChildRecycleView);
        addChildButton=findViewById(R.id.addChildButton);
        childVisitDetailButton=findViewById(R.id.childVisitDetailButton);
    }

    @Override
    public void onClick(View v) {

        Bundle childBundle=new Bundle();
        childBundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerId));

        if(v.getId() == R.id.addChildButton){
            Intent newChildIntent=new Intent(RegisterChildActivity.this,AddNewChildActivity.class);
            newChildIntent.putExtras(childBundle);
            startActivity(newChildIntent);
        }else if(v.getId()==R.id.childVisitDetailButton){
            Intent childVisitIntent=new Intent(RegisterChildActivity.this,ChildVisitDetailActivity.class);
            childVisitIntent.putExtras(childBundle);
            startActivity(childVisitIntent);
        }
    }
}
