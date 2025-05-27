package com.olam.digital.ofis.agrisupplier.ui;

import static com.olam.digital.ofis.agrisupplier.ui.FindSupplierAndSurveyActivity.getSupplierTypeByLanguage;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.agrisupplier.adapter.SupplierSurveyListAdapter;
import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyDto;
import com.olam.digital.ofis.agrisupplier.dto.SupplierTypeDto;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierWithGroups;
import com.olam.digital.ofis.agrisupplier.listener.SupplierSurveyItemClickListener;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import java.util.ArrayList;
import java.util.List;

public class ViewSupplierActivity extends BaseListActivity {
    private Context context;
    private String supplierName;
    private Long supplierId;
    private Resources res;

    private SupplierDto supplier;
    private List<SupplierSurveyDto> supplierSurveys;
    private List<Integer> supplierGroupIds = new ArrayList<>();
    private OFISRoomDB ofisRoomDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_view_supplier);
        context = this;
        res = getResources();
        supplier = getIntent().getParcelableExtra(Utilities.SUPPLIER);
        ofisRoomDB = OFISRoomDB.getDbInstance(context);

        if (supplier == null) {
            return;
        }

        setUiWidgets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        intiSurveys();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUiWidgets() {
        TextView supplierName = findViewById(R.id.tvSupplierName);
        TextView supplierId = findViewById(R.id.tvSupplierId);
        TextView phoneNumber = findViewById(R.id.tvPhoneNumber);
        TextView supplierGroupName = findViewById(R.id.tvSupplierGroup);
        TextView supplierType = findViewById(R.id.tvSupplierType);
        TextView viewSupplierInfo = findViewById(R.id.viewSupplierInfo);
        ConstraintLayout clViewSupplierInfo = findViewById(R.id.clSupplierInfo);

        SupplierWithGroups supplierGroup = ofisRoomDB.supplierDao().getSupplierWithGroups(supplier.getId());
        StringBuilder groupNames = getGroupNames(supplierGroup);
        SupplierTypeDto supplierTypeDto = ofisRoomDB.supplierTypeDao().getSupplierTypeNameById(supplier.getSupplierTypeId());
        String mobileNumber = supplier.getPrimaryPhoneNumber() + ", " + supplier.getSecondaryPhoneNumber();

        supplierName.setText(supplier.getSupplierName());
        supplierId.setText(supplier.getSupplierId());
        setBoldLabelText(phoneNumber, res.getString(R.string.PhoneNumberHint), mobileNumber);
        setBoldLabelText(supplierGroupName, res.getString(R.string.supplier_group), groupNames.toString());
        setBoldLabelText(supplierType, res.getString(R.string.supplier_type), getSupplierTypeByLanguage(supplierTypeDto));

        viewSupplierInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isExpanded = clViewSupplierInfo.getVisibility() == View.VISIBLE;
                clViewSupplierInfo.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                viewSupplierInfo.setCompoundDrawablesWithIntrinsicBounds(0,0, isExpanded ? R.drawable.ic_drop_down_collapse : R.drawable.ic_task_drop_down_expand, 0);
            }
        });


    }

    @NonNull
    private StringBuilder getGroupNames(SupplierWithGroups supplierGroup) {
        StringBuilder groupNames = new StringBuilder();
        for (SupplierGroup group : supplierGroup.supplierGroups) {
            groupNames.append(group.supplierGroupName).append(", ");
            supplierGroupIds.add(group.supplierGroupId);
        }

        if (groupNames.length() > 0) {
            groupNames.setLength(groupNames.length() - 2);
        }
        return groupNames;
    }

    private void setBoldLabelText(TextView textView, String label, String value) {
        String delimiter = " : ";
        String fullText = label + delimiter + value;

        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length() + delimiter.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ssb);
    }

    private void intiSurveys() {
        supplierSurveys = ofisRoomDB.dmMainModuleDao().getSupplierSurveys(supplier.getSupplierTypeId(), supplierGroupIds, supplier.getId());
        RecyclerView rvSupplierSurvey = findViewById(R.id.rvSupplierSurvey);
        rvSupplierSurvey.setLayoutManager(new LinearLayoutManager(this));
        SupplierSurveyListAdapter adapter = new SupplierSurveyListAdapter(supplierSurveys, ofisRoomDB, res, context, supplier);
        rvSupplierSurvey.setAdapter(adapter);

        adapter.setOnItemClickListener(new SupplierSurveyItemClickListener() {
            @Override
            public void onItemClick(SupplierSurveyDto supplierSurveyDto) {
                Intent intent = new Intent(context, SupplierSurveyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Utilities.SUPPLIER, supplierSurveyDto);
                Long supplierId = supplier.getId();
                bundle.putString("SupplierId", "S-" + supplierId);
                bundle.putString("SupplierName", supplier.getSupplierName());
                bundle.putString("SupplierDisplayId", supplier.getSupplierId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}