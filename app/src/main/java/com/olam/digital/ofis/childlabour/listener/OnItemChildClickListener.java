package com.olam.digital.ofis.childlabour.listener;

import android.view.View;

import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;

public interface OnItemChildClickListener {

    void onChildClick(View view, int position, ChildrenDTO childrenDTO);
}
