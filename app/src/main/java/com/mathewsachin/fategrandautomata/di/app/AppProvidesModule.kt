package com.mathewsachin.fategrandautomata.di.app

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.SupportStore
import com.mathewsachin.fategrandautomata.prefs.core.PrefMaker
import com.mathewsachin.fategrandautomata.scripts.IDropScreenshotStore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.LegacyDropScreenshotStore
import com.mathewsachin.fategrandautomata.util.LegacySupportStore
import com.mathewsachin.fategrandautomata.util.ScopedDropScreenshotStore
import com.mathewsachin.fategrandautomata.util.ScopedSupportStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppProvidesModule {
    @Singleton
    @Provides
    fun provideStorageDirs(@ApplicationContext context: Context): StorageDirs =
        StorageDirs(
            File(
                Environment.getExternalStorageDirectory(),
                "Fate-Grand-Automata"
            )
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

    @Singleton
    @Provides
    fun provideDropScreenshotStore(@ApplicationContext context: Context): IDropScreenshotStore =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ScopedDropScreenshotStore(context)
        } else LegacyDropScreenshotStore()

    @Singleton
    @Provides
    fun provideSupportStore(@ApplicationContext context: Context): SupportStore =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ScopedSupportStore(context)
        } else LegacySupportStore()
}