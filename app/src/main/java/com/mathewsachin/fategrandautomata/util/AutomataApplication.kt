package com.mathewsachin.fategrandautomata.util

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import org.slf4j.impl.HandroidLoggerAdapter

@HiltAndroidApp
class AutomataApplication : Application() {
    fun forceDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onCreate() {
        super.onCreate()

        initLogging()

        OpenCVLoader.initDebug()

        // forceDarkMode()
    }

    private fun initLogging() {
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        HandroidLoggerAdapter.ANDROID_API_LEVEL = Build.VERSION.SDK_INT
        HandroidLoggerAdapter.APP_NAME = "FGA"
    }
}