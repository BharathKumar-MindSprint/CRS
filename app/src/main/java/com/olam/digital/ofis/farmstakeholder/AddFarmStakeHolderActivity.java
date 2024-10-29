package com.olam.digital.ofis.farmstakeholder;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.AddFarmerActivity;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.StakeHolderDto;

import java.util.List;

public class AddFarmStakeHolderActivity extends BaseActivity {

    public DataLayer dl;
    private Long farmerId;
    private String farmerName;
    private String farmHoldingCode;
    private StakeHolderRecyclerAdapter stakeHolderRecyclerAdapter;
    private RecyclerView stakeHolderRecyclerView;
    private EditText etFarmHoldingName, etFarmHoldingCode, etFarmerName;
    private LinearLayout llAddStakeHolder;
    private Cursor farmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm_stake_holder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dl = new DataLayer(res, context);

        farmerId = getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        farmerName = getIntent().getExtras().getString("olamFarmerName", res.getString(R.string.farmer));
        farmHoldingCode = getIntent().getExtras().getString("farmHoldingCode", res.getString(R.string.farm_holding_code));

        stakeHolderRecyclerView = findViewById(R.id.stakeHolderRecyclerView);
        etFarmerName = findViewById(R.id.etFarmerName);
        etFarmHoldingName = findViewById(R.id.etFarmHoldingName);
        etFarmHoldingCode = findViewById(R.id.etFarmHoldingCode);
        llAddStakeHolder = findViewById(R.id.llAddStakeHolder);

        initComponents();
        initListener();
        loadStakeHolderList();
    }

    private void initComponents() {
        etFarmerName.setText(farmerName);
        etFarmHoldingCode.setText(farmHoldingCode);
        etFarmHoldingName.setText(farmerName);
    }

    private void initListener() {
        llAddStakeHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Utilities.STAKE_HOLDER_ID, null);
                bundle.putBoolean(Utilities.STAKE_HOLDER_REGISTRATION, true);
                bundle.putLong(Utilities.FARMER_ID, farmerId);
                bundle.putString(Utilities.FARM_HOLDING_CODE, farmHoldingCode);
                bundle.putString(Utilities.FARMER_NAME, farmerName);
                startActivity(new Intent(AddFarmStakeHolderActivity.this, AddFarmerActivity.class).putExtras(bundle));
            }
        });
    }
    
    private void loadStakeHolderList() {
        List<StakeHolderDto> stakeHolderList = dl.getAllStakeHolderListByFarmerId(farmerId);

        if (!stakeHolderList.isEmpty()) {
            stakeHolderRecyclerAdapter = new StakeHolderRecyclerAdapter(stakeHolderList,this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            stakeHolderRecyclerView.setLayoutManager(reLayoutManager);
            stakeHolderRecyclerView.setItemAnimator(new DefaultItemAnimator());
            stakeHolderRecyclerView.setAdapter(stakeHolderRecyclerAdapter);
            stakeHolderRecyclerView.scrollToPosition(stakeHolderRecyclerAdapter.getItemCount() - 1);

            stakeHolderRecyclerAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onStakeHolderClick(View view, int position, StakeHolderDto stakeHolderDto) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Utilities.STAKE_HOLDER_ID, stakeHolderDto.getStakeHolderId());
                    bundle.putString(Utilities.STAKE_HOLDER_REGISTRATION, "true");
                    startActivity(new Intent(AddFarmStakeHolderActivity.this, AddFarmerActivity.class).putExtras(bundle));
                }
            });
        }
    }
}