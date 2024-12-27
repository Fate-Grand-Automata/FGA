package io.github.fate_grand_automata.util

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.fate_grand_automata.BuildConfig
import io.github.fate_grand_automata.util.Notifications
import org.opencv.android.OpenCVLoader
import timber.log.Timber

@HiltAndroidApp
class AutomataApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogging()

        OpenCVLoader.initLocal()

        setupNotificationChannels()
    }

    private fun initLogging() {
        Timber.plant(FgaTree())
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create notification channels")
        }
    }

    private class FgaTree : Timber.DebugTree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return if (BuildConfig.DEBUG) true else priority > Log.INFO
        }
    }
}
