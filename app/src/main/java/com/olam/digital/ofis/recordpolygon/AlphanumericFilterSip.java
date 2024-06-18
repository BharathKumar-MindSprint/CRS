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

        if (!input.matches("[a-zA-Z0-9 ]+")) {
            return "";
        }

        if (dest.length() + input.length() > maxLength) {
            return "";
        }

        return null;
    }
}