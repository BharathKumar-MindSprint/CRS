package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

import com.olam.digital.ofis.R;

public class DecimalFilter implements InputFilter {
    private double minValue;
    private double maxValue;

    private boolean isDecimal;

    private EditText editText;
    private Context context;

    public DecimalFilter(double minValue, double maxValue, EditText editText, boolean isDecimal, Context context) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.editText = editText;
        this.isDecimal = isDecimal;
        this.context = context;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String resultingText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            boolean addingDecimalPoint = source.toString().equals(".") && !dest.toString().contains(".");
            if (addingDecimalPoint) {
                return null;
            }

            double input = Double.parseDouble(resultingText);

            if (input >= minValue && input <= maxValue) {
                return null;
            }

            if (editText != null) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String input = editText.getText().toString();
                        if (!input.isEmpty()) {
                            try {
                                double number = Double.parseDouble(input);
                                if (number >= minValue) {
                                    editText.setError(null);
                                } else {
                                    if (isDecimal) {
                                        editText.setError(String.format(context.getString(R.string.error_range_integer), minValue, maxValue));
                                    } else {
                                        editText.setError(String.format(context.getString(R.string.error_range_decimal), minValue, maxValue));
                                    }
                                }
                            } catch (NumberFormatException e) {

                            }
                        }
                    }
                });
            }

            if (input > maxValue) {
                if (isDecimal) {
                    editText.setError("Enter  range between " + minValue + " to " + maxValue);
                } else {
                    editText.setError("Enter  range between " + (int) Double.parseDouble(String.valueOf(minValue)) + " to " + (int) Double.parseDouble(String.valueOf(maxValue)));
                }
                editText.setError("Range from " + (int) Double.parseDouble(String.valueOf(minValue)) + " to " + (int) Double.parseDouble(String.valueOf(maxValue)));
                return "";
            }

            if (resultingText.length() > 0) {
                double inputValue = Double.parseDouble(resultingText);
                if (inputValue < minValue) {
                    return null;
                }
            }

            return "";

        } catch (NumberFormatException ignored) {
            ignored.printStackTrace();
        }

        return "";
    }
}