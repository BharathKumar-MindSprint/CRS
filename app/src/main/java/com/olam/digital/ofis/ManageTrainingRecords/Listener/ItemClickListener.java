package com.olam.digital.ofis.ManageTrainingRecords.Listener;

import android.view.View;

import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;

import java.util.List;

public interface ItemClickListener {
    void onClick(View view, int position, List<DocumentList.data> documentList);
}
