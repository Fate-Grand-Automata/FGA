package com.mathewsachin.fategrandautomata.di.app

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import com.mathewsachin.fategrandautomata.prefs.core.PrefMaker
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppProvidesModule {
    @Provides
    fun provideMediaProjectionManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    @Provides
    fun provideWindowManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context) =
        context.getSystemService(AccessibilityService.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideVibrator(@ApplicationContext context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    @Provides
    fun provideClipboardManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    @Provides
    fun providePowerManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    @Singleton
    @Provides
    fun provideGesturePrefs(prefs: IPreferences) = prefs.gestures

    @Singleton
    @Provides
    fun providePrefMaker(@ApplicationContext context: Context) =
        PrefMaker(
            context.getSharedPreferences(
                "${context.packageName}_preferences",
                Context.MODE_PRIVATE
            )
        )
}