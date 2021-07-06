package com.mathewsachin.fategrandautomata.util

import android.app.Application
import com.mathewsachin.fategrandautomata.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import timber.log.LogcatTree
import timber.log.Timber

@HiltAndroidApp
class AutomataApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogging()

        OpenCVLoader.initDebug()
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