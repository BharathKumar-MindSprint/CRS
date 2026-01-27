package com.olam.digital.crs.extension

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.olam.digital.crs.model.Resource
import com.olam.digital.crs.model.ResourceState

fun View.show() {
    this.visibility = android.view.View.VISIBLE
}

fun View.hide() {
    this.visibility = android.view.View.GONE
}

fun View.invisible() {
    this.visibility = android.view.View.INVISIBLE
}

fun View.showOrHide(show: Boolean) {
    this.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
}

fun View.showOrInvisible(show: Boolean) {
    this.visibility = if (show) android.view.View.VISIBLE else android.view.View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return this.visibility == android.view.View.VISIBLE
}

fun View.isGone(): Boolean {
    return this.visibility == android.view.View.GONE
}

fun View.isInvisible(): Boolean {
    return this.visibility == android.view.View.INVISIBLE
}

fun View.toggleVisibility() {
    this.visibility = if (this.visibility == android.view.View.VISIBLE) {
        android.view.View.GONE
    } else {
        android.view.View.VISIBLE
    }
}


fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}



fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T? = null) {
    value = Resource(ResourceState.SUCCESS, data)
}

fun <T> MutableLiveData<Resource<T>>.postSuccess(data: T? = null) {
    postValue(Resource(ResourceState.SUCCESS, data))
}

fun <T> MutableLiveData<Resource<T>>.postNoData(data: T? = null) {
    postValue(Resource(ResourceState.NO_DATA, data))
}

fun <T> MutableLiveData<Resource<T>>.setLoading() {
    value = Resource(ResourceState.LOADING, value?.data)
}

fun <T> MutableLiveData<Resource<T>>.postLoading() {
    postValue(Resource(ResourceState.LOADING, value?.data))
}

fun <T> MutableLiveData<Resource<T>>.setError(message: String? = null) {
    value = Resource(ResourceState.ERROR, value?.data, message)
}

fun <T> MutableLiveData<Resource<T>>.postError(message: String? = null) {
    postValue(Resource(ResourceState.ERROR, value?.data, message))
}

 fun String.normalizeToken(): String =
    trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "") // remove spaces, hyphens, underscores, etc.

