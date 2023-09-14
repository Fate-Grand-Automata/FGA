package io.github.fate_grand_automata.ui.battle_config_apple

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BattleConfigAppleViewModel @Inject constructor(
    prefsCore: PrefsCore,
    val prefs: IPreferences
): ViewModel() {
    val battleConfigItems = prefsCore
        .battleConfigList
        .asFlow()
        .map { list ->
            list
                .map { key -> prefsCore.forBattleConfig(key) }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.get() })
        }
}