package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.adapter.ChildLabourRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.util.List;

public class RegisterChildActivity extends BaseActivity implements View.OnClickListener{

    private RecyclerView childRecyclerView;
    private RelativeLayout addChildButton;
    private  RelativeLayout childVisitDetailButton;
    private ChildLabourRecyclerAdapter childLabourRecyclerAdapter;
    Long farmerId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        initEvents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){}
        populateChildList();
        childLimit();
    }

    private void childLimit() {
        if (childLabourRecyclerAdapter!=null && childLabourRecyclerAdapter.getItemCount()>= 30){
            addChildButton.setBackgroundColor(ContextCompat.getColor(this, R.color.child_survey_row));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            populateChildList();
        }catch (Exception e){}

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
            childRecyclerView.scrollToPosition(childLabourRecyclerAdapter.getItemCount() - 1);

            childLabourRecyclerAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onChildClick(View view, int position,ChildrenDTO childrenDTO) {

                    if(childrenDTO.getSynced().equalsIgnoreCase("0")){
                        Intent newChildIntent=new Intent(RegisterChildActivity.this,AddNewChildActivity.class);
                        Bundle childBundle=new Bundle();
                        newChildIntent.putExtra("action","edit");
                        childBundle.putSerializable("children",childrenDTO);
                        childBundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerId));
                        newChildIntent.putExtras(childBundle);
                        startActivity(newChildIntent);
                    }
                }
            });
            childLimit();
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
            if (childLabourRecyclerAdapter!=null && childLabourRecyclerAdapter.getItemCount()>= 30){
                Toast.makeText(RegisterChildActivity.this,getResources().getString(R.string.child_max_limit),Toast.LENGTH_LONG).show();
                return;
            }
                Intent newChildIntent = new Intent(RegisterChildActivity.this, AddNewChildActivity.class);
                newChildIntent.putExtras(childBundle);
                newChildIntent.putExtra("action", "new");
                startActivity(newChildIntent);

        }else if(v.getId()==R.id.childVisitDetailButton){
            Intent childVisitIntent=new Intent(RegisterChildActivity.this,ChildVisitDetailActivity.class);
            childVisitIntent.putExtras(childBundle);
            startActivity(childVisitIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }


}
