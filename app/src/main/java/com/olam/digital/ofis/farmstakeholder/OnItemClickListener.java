package com.olam.digital.ofis.farmstakeholder;

import android.view.View;

import com.olam.digital.ofis.childlabour.dto.StakeHolderDto;

public interface OnItemClickListener {

    void onStakeHolderClick(View view, int position, StakeHolderDto stakeHolderDto);
}
