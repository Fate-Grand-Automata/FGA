package com.mathewsachin.fategrandautomata.dagger

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Environment
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.PreferencesImpl
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.ImageLoader
import com.mathewsachin.libautomata.ExitManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module(subcomponents = [ScriptRunnerServiceComponent::class])
abstract class AppModule {
    @Module
    companion object {
        @Singleton
        @Provides
        fun provideStorageDirs(): StorageDirs =
            StorageDirs(
                File(
                    Environment.getExternalStorageDirectory(),
                    "Fate-Grand-Automata"
                )
            )

        @Singleton
        @Provides
        fun provideMediaProjectionManager(context: Context) =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        @Singleton
        @Provides
        fun provideExitManager() = ExitManager()

        @Singleton
        @Provides
        fun provideGesturePrefs(prefs: IPreferences) = prefs.gestures
    }

    @Singleton
    @Binds
    abstract fun bindImageLoader(imageLoader: ImageLoader): IImageLoader

    @Singleton
    @Binds
    abstract fun bindPrefs(prefs: PreferencesImpl): IPreferences
}