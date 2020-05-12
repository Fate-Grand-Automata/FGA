package com.mathewsachin.fategrandautomata.util

import android.app.Application
import org.opencv.android.OpenCVLoader

class AutomataApplication : Application() {
    companion object{
        lateinit var Instance: Application
    }

    override fun onCreate() {
        super.onCreate()

        Instance = this
        OpenCVLoader.initDebug()
    }
}