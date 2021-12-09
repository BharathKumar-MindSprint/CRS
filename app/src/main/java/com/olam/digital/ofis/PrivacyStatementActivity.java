package com.olam.digital.ofis;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PrivacyStatementActivity extends BasePrivacyActivity {
    PrivacyStatementExpandableAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listPrivacyHeader;
    private int lastPosition = -1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_statement);
        initWidgets();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_privacy_statement));

        setUpAdapter();
        View header=getLayoutInflater().inflate(R.layout.privacy_statement_header, null);
        expListView.addHeaderView(header);

        expListView.setOnGroupExpandListener(groupPosition -> {
            if (lastPosition != -1
                    && groupPosition != lastPosition) {
                expListView.collapseGroup(lastPosition);
            }
            lastPosition = groupPosition;
        });

    }

    private void setUpAdapter() {
        listPrivacyHeader.add(getResources().getString(R.string.general_principels));
        listPrivacyHeader.add(getResources().getString(R.string.ofi_info));
        listPrivacyHeader.add(getResources().getString(R.string.cookies_header));
        listPrivacyHeader.add(getResources().getString(R.string.ofi_personal_info));
        listPrivacyHeader.add(getResources().getString(R.string.sharing_info));
        listPrivacyHeader.add(getResources().getString(R.string.crossbordertrasfer));
        listPrivacyHeader.add(getResources().getString(R.string.security_personal));
        listPrivacyHeader.add(getResources().getString(R.string.retention_personal));
        listPrivacyHeader.add(getResources().getString(R.string.children_ps));
        listPrivacyHeader.add(getResources().getString(R.string.links_to_website));
        listPrivacyHeader.add(getResources().getString(R.string.RandC));
        listPrivacyHeader.add(getResources().getString(R.string.gdpr));
        listPrivacyHeader.add(getResources().getString(R.string.addional_info));
        listPrivacyHeader.add(getResources().getString(R.string.Privacy_change));

        listAdapter = new PrivacyStatementExpandableAdapter(this, listPrivacyHeader);
        expListView.setAdapter(listAdapter);
    }

    private void initWidgets() {
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);
        listPrivacyHeader=new ArrayList<String>();
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}
