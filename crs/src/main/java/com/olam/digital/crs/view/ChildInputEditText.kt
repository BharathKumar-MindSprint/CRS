package com.olam.digital.crs.view

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import timber.log.Timber

/**
 * EditText for use inside nested RecyclerViews.
 * Ensures IME input connection stays alive even when parent loses focus.
 */
class ChildInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        Timber.d("✅ ChildInputEditText CREATED")
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        Timber.d("✅ onCreateInputConnection called")
        return super.onCreateInputConnection(outAttrs)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        Timber.d("onWindowFocusChanged: $hasWindowFocus")
        if (hasWindowFocus && isFocused) {
            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            Timber.d("Calling restartInput")
            imm.restartInput(this)
        }
    }
}