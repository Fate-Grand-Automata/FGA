package io.github.fate_grand_automata.util

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.fate_grand_automata.BuildConfig
import org.opencv.android.OpenCVLoader
import timber.log.Timber

@HiltAndroidApp
class AutomataApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogging()

        OpenCVLoader.initLocal()
    }

    private fun initLogging() {
        Timber.plant(FgaTree())
    }

    private class FgaTree : Timber.DebugTree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return if (BuildConfig.DEBUG) true else priority > Log.INFO
        }
    }
}
