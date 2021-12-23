package com.olam.digital.ofis;

import android.view.View;

import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.model.FarmerLeadDTO;

public interface OnItemFarmerLeadListener {

    void onFarmerLeadClick(View view, int position, FarmerLeadDTO farmerLeadDTO);
}
