package com.olam.digital.ofis.recordpolygon;
import android.text.InputFilter;
import android.text.Spanned;

public class AlphanumericFilterSip implements InputFilter {
    private final int maxLength;

    public AlphanumericFilterSip(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String input = source.toString();

        // Check if the input is alphanumeric and within the character limit
        if (input.matches("[a-zA-Z0-9]+") && (dest.length() + input.length() <= maxLength)) {
            return null; // Accept the input
        }

        // Reject the input
        return "";
    }
}