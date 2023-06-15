package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import static org.apache.commons.lang3.StringUtils.isNumeric;

import android.text.InputFilter;
import android.text.Spanned;

public class NumberInputFilter implements InputFilter {
    private double minValue;
    private double maxValue;

    public NumberInputFilter(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String input = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

        if (input.isEmpty()) {
            return null; // Allow empty input
        }

        if (input.equals("-") && minValue < 0) {
            return null; // Reject the input if minValue is negative
        }

        if (!isNumeric(input)) {
            return ""; // Reject the input if it contains non-numeric characters
        }

        if (containsAlphabets(input)) {
            return null; // Reject the input if it contains alphabetic characters
        }

//        for (int i = input.length() - 1; i >= 0; i--) {
//            char c = input.charAt(i);
//            if (!Character.isDigit(c)) {
//                return ""; // Reject the input if it contains alphabetic characters
//            }
//        }

        int number;

        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return ""; // Reject the input if it is not a valid integer
        }

        if (number >= minValue && number <= maxValue) {
            return null; // Accept number within the specified range
        } else {
            return ""; // Reject the input if it is outside the specified range
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+"); // Checks if the string contains only digits (with an optional negative sign)
    }

    private boolean containsAlphabets(String str) {
        return str.matches(".*[a-zA-Z]+.*"); // Checks if the string contains any alphabetic characters
    }
}
