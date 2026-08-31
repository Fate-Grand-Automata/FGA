package io.github.fate_grand_automata.di.vm

import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.main.Route

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelProvidesModule {
    val SavedStateHandle.configKey: String get() =
        this[Route.BattleConfig.idArg]
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