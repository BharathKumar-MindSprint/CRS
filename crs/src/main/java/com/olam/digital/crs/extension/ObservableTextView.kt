package com.olam.digital.crs.extension

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class ObservableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var listener: ((String) -> Unit)? = null

    fun setOnTextChangeListener(l: (String) -> Unit) {
        listener = l
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        listener?.invoke(text.toString())
    }
}