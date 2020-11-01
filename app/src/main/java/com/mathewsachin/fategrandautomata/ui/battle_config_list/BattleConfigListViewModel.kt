package com.mathewsachin.fategrandautomata.ui.battle_config_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.map

class BattleConfigListViewModel @ViewModelInject constructor(
    prefsCore: PrefsCore,
    prefs: IPreferences
) : ViewModel() {
    val battleConfigItems = prefsCore
        .battleConfigList
        .asFlow()
        .map { prefs.battleConfigs }
        .asLiveData()
}