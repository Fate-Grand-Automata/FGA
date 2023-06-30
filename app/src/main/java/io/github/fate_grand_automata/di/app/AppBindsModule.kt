package io.github.fate_grand_automata.di.app

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.fate_grand_automata.IStorageProvider
import io.github.fate_grand_automata.prefs.PreferencesImpl
import io.github.fate_grand_automata.scripts.IImageLoader
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.util.ImageLoader
import io.github.fate_grand_automata.util.StorageProvider
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
    fun bindStorageProvider(storageProvider: StorageProvider): IStorageProvider
}