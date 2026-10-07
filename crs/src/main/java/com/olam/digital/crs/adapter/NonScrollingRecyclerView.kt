package com.olam.digital.crs.adapter

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/*class NonScrollingRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    *//**
     * Swallow focus notifications so the parent RecyclerView
     * doesn’t auto-scroll this ViewHolder when a child EditText gains focus.
     *//*
    override fun requestChildFocus(child: View, focused: View) {
        // Do nothing — prevents parent scroll
    }

    *//**
     * Block rect-based scroll requests (IME, accessibility).
     *//*
    override fun requestChildRectangleOnScreen(
        child: View,
        rect: Rect,
        immediate: Boolean
    ): Boolean = false

    *//**
     * Pass all touch events straight to children — never intercept.
     *//*
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean = false
}*/



class NonScrollingRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    /**
     * Allow focus changes normally so EditTexts can receive input.
     */
    override fun requestChildFocus(child: View?, focused: View?) {
        super.requestChildFocus(child, focused)
    }

    /**
     * Block rect-based scroll requests (IME, accessibility).
     * This prevents the parent from auto-scrolling when a child gains focus.
     */
    override fun requestChildRectangleOnScreen(
        child: View,
        rect: Rect?,
        immediate: Boolean
    ): Boolean = false

    /**
     * Pass all touch events straight to children — never intercept.
     */
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean = false
}
