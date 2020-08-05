package com.mathewsachin.fategrandautomata.dagger.app

import android.app.Application
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.view.WindowManager
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
class AppContextModule(val app: Application) {
    @Singleton
    @Provides
    fun provideContext(): Context = app

    @Singleton
    @Provides
    fun provideStorageDirs(context: Context): StorageDirs =
        StorageDirs(
            File(
                Environment.getExternalStorageDirectory(),
                "Fate-Grand-Automata"
            ),
            context.cacheDir
        )

    @Singleton
    @Provides
    fun provideMediaProjectionManager(context: Context) =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    @Singleton
    @Provides
    fun provideWindowManager(context: Context) =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Singleton
    @Provides
    fun provideGesturePrefs(prefs: IPreferences) = prefs.gestures
}