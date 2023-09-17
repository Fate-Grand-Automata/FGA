package io.github.fate_grand_automata.ui.battle_config_apple

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BattleConfigAppleViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val prefs: IPreferences,
) : ViewModel() {

    val perServerConfigPrefsList = prefsCore
        .serverPrefsList
        .asFlow()
        .map {list ->
            list.map {
                prefsCore.forPerServerConfigPrefs(it)
            }
        }

    val gameServers = prefsCore
        .showGameServer
        .asFlow()

    fun updateGameServers(updateGameServers: List<GameServer>){
        prefs.showGameServers = updateGameServers
    }

}