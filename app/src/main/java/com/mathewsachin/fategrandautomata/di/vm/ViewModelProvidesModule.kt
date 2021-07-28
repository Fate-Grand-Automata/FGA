package com.mathewsachin.fategrandautomata.di.vm

import androidx.lifecycle.SavedStateHandle
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.main.NavConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelProvidesModule {
    val SavedStateHandle.configKey: String get() =
        this[NavConstants.battleConfigIdKey]
            ?: throw kotlin.Exception("Couldn't get Battle Config key")

    @ViewModelScoped
    @Provides
    fun provideBattleConfig(
        prefs: IPreferences,
        savedState: SavedStateHandle
    ) = prefs.forBattleConfig(savedState.configKey)

    @ViewModelScoped
    @Provides
    fun provideBattleConfigCore(
        prefs: PrefsCore,
        savedState: SavedStateHandle
    ) = prefs.forBattleConfig(savedState.configKey)
}