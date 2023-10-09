package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import android.text.InputFilter;
import android.text.Spanned;

public class DecimalFilter implements InputFilter {
    private double minValue;
    private double maxValue;

    public DecimalFilter(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Create the resulting string by combining the existing text and the input
            String resultingText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            // Parse the resulting string to a double value
            double input = Double.parseDouble(resultingText);


            // Check if the input is within the valid range
            if (input >= minValue && input <= maxValue) {
                // Input is valid, allow it
                return null;
            }


            // Check if the input reaches the minimum value
            if (resultingText.length() > 0) {
                int inputValue = Integer.parseInt(resultingText);
                if (inputValue < minValue) {
                    return null; // Allow the input but prevent further input until minimum value is reached
                }
            }

        } catch (NumberFormatException ignored) {
            // Ignore the exception and let the input through
        }

        // Input is invalid, reject it
        return "";
    }
}

