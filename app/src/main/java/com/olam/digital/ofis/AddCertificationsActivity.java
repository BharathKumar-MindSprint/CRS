package com.olam.digital.ofis;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.olam.digital.ofis.Common.Adapter.MultiSpinnerAdapter;
import com.olam.digital.ofis.Common.Model.MultiSpinnerStatus;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Certifications;

import java.util.ArrayList;
import java.util.List;

public class AddCertificationsActivity extends BaseActivity implements View.OnClickListener {

    Spinner certificationNameSpinner;
    Button certification_save;
    EditText ed_other_certification;
    ArrayList<MultiSpinnerStatus> listStatus = new ArrayList<>();
    private OFISRoomDB ofisRoomDB;
    private Long farmerId;
    private String farmerName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_add_certifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
    }

    private void initComponents() {
        this.setTitle(getString(R.string.update_certification));
        certificationNameSpinner = (Spinner) findViewById(R.id.spinner_certificationName);
        certification_save = (Button) findViewById(R.id.certification_save);
        ed_other_certification = (EditText) findViewById(R.id.ed_other_certification);
        certification_save.setOnClickListener(this);
        try {
            farmerId = getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
            farmerName = getIntent().getExtras().getString("FARMER_NAME");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //roomdb
        ofisRoomDB = OFISRoomDB.getDbInstance(context);

        bindCertificationNameSpinner();
    }

    private void bindCertificationNameSpinner() {
        List<Certifications> certificationsList = ofisRoomDB.certificationsDao().getCertifications();
        MultiSpinnerStatus multiSpinnerStatusCn = new MultiSpinnerStatus();
        multiSpinnerStatusCn.setId(0);
        multiSpinnerStatusCn.setTitle(res.getString(R.string.certification_name));
        multiSpinnerStatusCn.setSelected(false);
        multiSpinnerStatusCn.setEnabled(true);
        listStatus.add(multiSpinnerStatusCn);
        for (int i = 0; i < certificationsList.size(); i++) {
            MultiSpinnerStatus multiSpinnerStatus = new MultiSpinnerStatus();
            multiSpinnerStatus.setId(certificationsList.get(i).certificationId);
            multiSpinnerStatus.setTitle(certificationsList.get(i).certificationName);
            multiSpinnerStatus.setSelected(false);
            multiSpinnerStatus.setEnabled(true);
            listStatus.add(multiSpinnerStatus);
        }

        MultiSpinnerStatus multiSpinnerStatusothers = new MultiSpinnerStatus();
        multiSpinnerStatusothers.setId(listStatus.size() + 1);
        multiSpinnerStatusothers.setTitle(res.getString(R.string.others));
        multiSpinnerStatusothers.setSelected(false);
        multiSpinnerStatusothers.setEnabled(true);
        listStatus.add(multiSpinnerStatusothers);
        MultiSpinnerAdapter myAdapter = new MultiSpinnerAdapter(AddCertificationsActivity.this, 0,
                listStatus, certificationNameSpinner, ed_other_certification);
        certificationNameSpinner.setAdapter(myAdapter);
        setSpinner(myAdapter);
    }

    private void setSpinner(MultiSpinnerAdapter mAdapter) {
        if (dl.isCertificationavailable(farmerId)) {
            Cursor nids = dl.getCertificationsbyfarmerid(farmerId);
            try {
                if (nids.getCount() > 0) {
                    if (nids.moveToFirst()) {
                        String cIds = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID));
                        String ctext = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT));
                        if (ctext != null && !ctext.isEmpty()) {
                            ed_other_certification.setText(ctext);
                        }
                        if (cIds != null) {
                            String[] cIdArr = cIds.split(",");
                            for (int i = 0; i < listStatus.size(); i++) {
                                for (int arr = 0; arr < cIdArr.length; arr++) {
                                    if (listStatus.get(i).getId() == Integer.parseInt(cIdArr[arr])) {
                                        listStatus.get(i).setSelected(true);
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                nids.close();
            }

        }
        mAdapter.notifyDataSetChanged();
        certificationNameSpinner.setTag(listStatus);
    }

    private Boolean getIsCertification(List<MultiSpinnerStatus> multiSpinnerStatuses) {
        Boolean status = false;
        for (int i = 0; i < multiSpinnerStatuses.size(); i++) {
            if (multiSpinnerStatuses.get(i).isSelected()) {
                status = true;
            }
            if (multiSpinnerStatuses.get(i).getTitle().equals(res.getString(R.string.others))) {
                if (multiSpinnerStatuses.get(i).isSelected()) {
                    if (ed_other_certification.getText() == null || ed_other_certification.getText().toString().trim().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return status;

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.certification_save) {
            if (certificationNameSpinner.getTag() == null) {
                Toast.makeText(this, getResources().getString(R.string.please_enter_certification_details), Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder certificationIds = new StringBuilder();
                List<MultiSpinnerStatus> multiSpinnerStatus = (ArrayList<MultiSpinnerStatus>) certificationNameSpinner.getTag();
                multiSpinnerStatus = (List<MultiSpinnerStatus>) certificationNameSpinner.getTag();
                Boolean isChecked = getIsCertification(multiSpinnerStatus);
                if (!isChecked) {
                    Toast.makeText(this, getResources().getString(R.string.please_enter_certification_details), Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < multiSpinnerStatus.size(); i++) {
                        if (multiSpinnerStatus.get(i).isSelected()) {
                            if (certificationIds.toString().isEmpty()) {
                                certificationIds = new StringBuilder(String.valueOf(multiSpinnerStatus.get(i).getId()));
                            } else {
                                certificationIds.append(",").append(multiSpinnerStatus.get(i).getId());
                            }
                        }
                    }

                    dl.updateCertificationsByFarmerId(farmerId, certificationIds.toString(), ed_other_certification.getText().toString().trim(), farmerName);
                    onBackPressed();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}