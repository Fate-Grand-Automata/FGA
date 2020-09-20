package com.mathewsachin.fategrandautomata.di.app

import com.mathewsachin.fategrandautomata.prefs.PreferencesImpl
import com.mathewsachin.fategrandautomata.scripts.IDropScreenshotStore
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.ImageLoader
import com.mathewsachin.fategrandautomata.util.LegacyDropScreenshotStore
import com.mathewsachin.fategrandautomata.util.ScriptMessages
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
interface AppBindsModule {
    @Singleton
    @Binds
    fun bindImageLoader(imageLoader: ImageLoader): IImageLoader

    @Singleton
    @Binds
    fun bindPrefs(prefs: PreferencesImpl): IPreferences

    @Singleton
    @Binds
    fun bindScriptMessages(scriptMessages: ScriptMessages): IScriptMessages

    @Singleton
    @Binds
    fun bindsDropScreenshotStore(dropScreenshotStore: LegacyDropScreenshotStore): IDropScreenshotStore
}