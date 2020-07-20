package com.mathewsachin.fategrandautomata.util

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.dagger.AppContextModule
import com.mathewsachin.fategrandautomata.dagger.ApplicationComponent
import com.mathewsachin.fategrandautomata.dagger.DaggerApplicationComponent
import org.opencv.android.OpenCVLoader

class AutomataApplication : Application() {
    fun forceDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    lateinit var appComponent: ApplicationComponent private set

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.builder()
            .appContextModule(
                AppContextModule(
                    this
                )
            )
            .build()

        OpenCVLoader.initDebug()

        // forceDarkMode()
    }
}