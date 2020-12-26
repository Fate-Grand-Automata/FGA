package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(ScriptComponent::class)
class ScriptProvidesModule {
    @ScriptScope
    @Provides
    fun provideExitManager() = ExitManager()

    @ScriptScope
    @Provides
    fun provideGameAreaManager(platformImpl: IPlatformImpl, prefs: IPreferences): GameAreaManager =
        FgoGameAreaManager(platformImpl, prefs)
}