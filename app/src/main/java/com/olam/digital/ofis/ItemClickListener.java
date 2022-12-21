package com.olam.digital.ofis;

import android.view.View;
import com.olam.digital.ofis.model.DocumentList;

public interface ItemClickListener {
    void onClick(View view, int position, DocumentList.data documentList);
}
