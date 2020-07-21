package com.mathewsachin.fategrandautomata.util

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.dagger.app.AppContextModule
import com.mathewsachin.fategrandautomata.dagger.app.ApplicationComponent
import com.mathewsachin.fategrandautomata.dagger.app.DaggerApplicationComponent
import org.opencv.android.OpenCVLoader

interface AppComponentProvider {
    val appComponent: ApplicationComponent
}

val Context.appComponent get() = (applicationContext as AppComponentProvider).appComponent

class AutomataApplication : Application(), AppComponentProvider {
    fun forceDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override lateinit var appComponent: ApplicationComponent private set

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