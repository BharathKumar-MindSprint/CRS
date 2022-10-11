package com.olam.digital.ofis.downloadsurvey.listener;

import android.view.View;

import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;

public interface OnItemDownloadedSurveyClickListener {

    void onModuleClick(View view, int position, AnswerRequestDTO moduleDTO);
}
