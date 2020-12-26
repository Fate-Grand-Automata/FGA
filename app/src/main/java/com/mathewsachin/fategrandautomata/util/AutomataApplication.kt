package com.mathewsachin.fategrandautomata.util

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import timber.log.LogcatTree
import timber.log.Timber

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
        val tree = LogcatTree("FGA").let {
            if (BuildConfig.DEBUG)
                it
            else it.withCompliantLogging()
        }

        Timber.plant(tree)
    }
}