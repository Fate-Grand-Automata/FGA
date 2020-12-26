package com.mathewsachin.fategrandautomata.di.app

import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.prefs.PreferencesImpl
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ISwipeLocations
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.ImageLoader
import com.mathewsachin.fategrandautomata.util.ScriptMessages
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.SwipeLocations
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
    fun bindSwipeLocations(swipeLocations: SwipeLocations): ISwipeLocations

    @Singleton
    @Binds
    fun bindStorageProvider(storageProvider: StorageProvider): IStorageProvider
}