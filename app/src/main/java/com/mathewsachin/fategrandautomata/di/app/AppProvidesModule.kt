package com.mathewsachin.fategrandautomata.di.app

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.view.WindowManager
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.PrefMaker
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppProvidesModule {
    @Singleton
    @Provides
    fun provideStorageDirs(@ApplicationContext context: Context): StorageDirs =
        StorageDirs(
            File(
                Environment.getExternalStorageDirectory(),
                "Fate-Grand-Automata"
            ),
            context.cacheDir
        )

    @Singleton
    @Provides
    fun provideMediaProjectionManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    @Singleton
    @Provides
    fun provideWindowManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Singleton
    @Provides
    fun provideGesturePrefs(prefs: IPreferences) = prefs.gestures

    @Singleton
    @Provides
    fun providePrefMaker(@ApplicationContext context: Context) =
        PrefMaker(
            PreferenceManager.getDefaultSharedPreferences(context),
            context
        )
}