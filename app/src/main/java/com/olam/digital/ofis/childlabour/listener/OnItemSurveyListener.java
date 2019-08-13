package com.olam.digital.ofis.childlabour.listener;

import android.view.View;

import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;

public interface OnItemSurveyListener {

    void onChildClick(View view, int position, ChildModuleDto childModuleDto);
}
