package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.adapter.ChildLabourRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.util.List;

public class ChildLabourDetailsActivity extends BaseActivity {

    private RecyclerView childLabourDetailsRecyclerView;
    Long farmerId;
    private ChildLabourRecyclerAdapter childLabourRecyclerAdapter;
    TextView noChildRegisterInfoTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_labour_details);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){}
        populateChildList();
        childLimit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void childLimit() {
        if (childLabourRecyclerAdapter!=null && childLabourRecyclerAdapter.getItemCount()> 0){
              noChildRegisterInfoTv.setVisibility(View.GONE);
        }
    }

    // method to populate the children list from db
    private void populateChildList() {
        ChildLabourLogic childLabourLogic=new ChildLabourLogic();
        List<ChildrenDTO> childrenList=childLabourLogic.getChildDetailsById(this,farmerId);
        if(childrenList!=null && childrenList.size()>0){
            childLabourRecyclerAdapter = new ChildLabourRecyclerAdapter(childrenList,this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            childLabourDetailsRecyclerView.setLayoutManager(reLayoutManager);
            // Newly Added
            DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_seperator_divider));
            childLabourDetailsRecyclerView.addItemDecoration(itemDecoration);

            childLabourDetailsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
            childLabourDetailsRecyclerView.setAdapter(childLabourRecyclerAdapter);
            childLabourDetailsRecyclerView.scrollToPosition(childLabourRecyclerAdapter.getItemCount() - 1);

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
        noChildRegisterInfoTv=findViewById(R.id.noChildInfoTv);
    }
}
