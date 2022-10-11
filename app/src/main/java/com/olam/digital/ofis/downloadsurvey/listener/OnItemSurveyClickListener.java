package com.olam.digital.ofis.downloadsurvey.listener;

import android.view.View;

import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;

public interface OnItemSurveyClickListener {

    void onModuleClick(View view, int position, ModuleDTO moduleDTO);
}
