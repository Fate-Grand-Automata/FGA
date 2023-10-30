package io.github.fate_grand_automata.util

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.fate_grand_automata.BuildConfig
import io.github.fate_grand_automata.R
import org.opencv.android.OpenCVLoader
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class AutomataApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogging()

        OpenCVLoader.initDebug()
    }

    private fun initLogging() {
        plantLog(cacheDir)
        if (BuildConfig.DEBUG){
            externalCacheDir?.let { plantLog(it) }
        }
    }

    private fun plantLog(fileDir: File) {
        Timber.plant(FgaTree(File(fileDir, "${getString(R.string.app_name)}.log")))
    }

    private class FgaTree(val file: File) : Timber.DebugTree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return if (BuildConfig.DEBUG) true else priority > Log.INFO
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)

            val logPriority = when(priority){
                1 -> "ERROR"
                2 -> "WARN"
                3 -> "INFO"
                4 -> "DEBUG"
                5 -> "VERBOSE"
                else -> "UNKNOWN"
            }

            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logLine = "$timestamp | $logPriority | $tag: $message\n"

            FileWriter(file, true).use {
                it.append(logLine)
            }

        }


    }
}