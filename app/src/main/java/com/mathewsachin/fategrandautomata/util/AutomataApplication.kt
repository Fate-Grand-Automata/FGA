package com.mathewsachin.fategrandautomata.util

import android.app.Application
import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.PreferencesImpl
import com.mathewsachin.fategrandautomata.scripts.initImageLoader
import com.mathewsachin.fategrandautomata.scripts.prefs.initPrefs
import org.opencv.android.OpenCVLoader
import java.io.File

val storageDirs = StorageDirs(
    File(
        Environment.getExternalStorageDirectory(),
        "Fate-Grand-Automata"
    )
)

class AutomataApplication : Application() {
    companion object {
        lateinit var Instance: Application
    }

    fun forceDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onCreate() {
        super.onCreate()

        Instance = this
        OpenCVLoader.initDebug()
        initPrefs(
            PreferencesImpl(
                this,
                storageDirs
            )
        )

        initImageLoader(ImageLoader(storageDirs, this))

        // forceDarkMode()
    }
}