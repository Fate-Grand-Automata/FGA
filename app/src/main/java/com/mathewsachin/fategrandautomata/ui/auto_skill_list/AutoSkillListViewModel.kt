package com.mathewsachin.fategrandautomata.ui.auto_skill_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.map

class AutoSkillListViewModel @ViewModelInject constructor(
    prefsCore: PrefsCore,
    prefs: IPreferences
) : ViewModel() {
    val autoSkillItems = prefsCore
        .autoSkillList
        .asFlow()
        .map { prefs.autoSkillPreferences }
        .asLiveData()
}