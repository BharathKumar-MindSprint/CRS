package com.olam.digital.ofis.tasks;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private EditText edit;

    @SuppressLint("ValidFragment")
    public SelectDateFragment(EditText edit) {
        this.edit = edit;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        populateSetDate(edit, yy, mm+1, dd);
    }

    public void populateSetDate(EditText edit, int year, int month, int day)
    {
        edit.setText( day + "." + month + "." + year);
    }
}
