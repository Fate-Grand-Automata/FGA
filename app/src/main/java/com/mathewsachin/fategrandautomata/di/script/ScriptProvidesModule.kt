package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.util.FgoNewGameAreaManager
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Provider

@Module
@InstallIn(ScriptComponent::class)
class ScriptProvidesModule {
    @ScriptScope
    @Provides
    fun provideExitManager() = ExitManager()

    @ScriptScope
    @Provides
    fun provideGameAreaManager(
        platformImpl: IPlatformImpl,
        prefsCore: PrefsCore,
        newGameAreaManager: Provider<FgoNewGameAreaManager>
    ): GameAreaManager {
        return if (prefsCore.newGameAreaDetection.get())
            newGameAreaManager.get()
        else FgoGameAreaManager(
            { platformImpl.windowRegion },
            Game.scriptSize,
            Game.imageSize
        )
    }
}