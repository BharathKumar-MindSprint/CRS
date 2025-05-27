package com.olam.digital.ofis.agrisupplier.listener;

import android.view.View;

public interface SupplierDialogBoxClickListener {
    public void onDialogItemClick(View view, int item, int questionId, String proofFormat, CharSequence[] options);
}
