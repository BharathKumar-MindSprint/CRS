package com.olam.digital.crs.dynamicAdapter

import android.text.InputFilter
import android.text.Spanned
import timber.log.Timber

/**
 * ✅ ULTRA-SIMPLE numeric input filter - accepts only digits 0-9
 */
class SimpleNumericInputFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // ✅ STEP 1: DELETE/BACKSPACE = source is null or empty
        if (source == null || source.isEmpty()) {
            Timber.d("DELETE ALLOWED ✅")
            return null  // ← Accept delete
        }

        // STEP 2: Check what user typed
        val userTyped = source.subSequence(start, end).toString()
        Timber.d("USER TYPED: '$userTyped'")

        // STEP 3: Only accept digits
        for (char in userTyped) {
            if (!char.isDigit()) {
                Timber.d("REJECTED: '$char' is not digit")
                return ""
            }
        }

        // STEP 4: Accept all digits
        Timber.d("ACCEPTED: '$userTyped'")
        return null
    }
}