package com.olam.digital.ofis

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OFISApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        if (BuildConfig.DEBUG)
        {
            // Timber initialization or other debug-specific setups can go here
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        var context: Context? = null
            private set
    }
}

