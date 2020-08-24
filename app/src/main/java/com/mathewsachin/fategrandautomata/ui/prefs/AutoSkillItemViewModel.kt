package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.combine

class AutoSkillItemViewModel @ViewModelInject constructor(
    val preferences: IPreferences,
    val prefsCore: PrefsCore
) : ViewModel() {
    var key: String = ""

    private val prefs by lazy { prefsCore.forAutoSkillConfig(key) }

    val cardPriority by lazy {
        prefs
            .cardPriority
            .asFlow()
            .asLiveData()
    }

    val skillCommand by lazy {
        prefs
            .skillCommand
            .asFlow()
            .asLiveData()
    }

    val skillLevels by lazy {
        combine(
            prefs.support.skill1Max.asFlow(),
            prefs.support.skill2Max.asFlow(),
            prefs.support.skill3Max.asFlow()
        ) { s1, s2, s3 ->
            listOf(s1, s2, s3)
                .joinToString("/") {
                    if (it) "10" else "x"
                }
        }
            .asLiveData()
    }

    val supportSelectionMode by lazy {
        prefs.support.selectionMode.asFlow()
            .asLiveData()
    }

    val areServantsSelected by lazy {
        combine(
            prefs.support.selectionMode.asFlow(),
            prefs.support.preferredServants.asFlow()
        ) { mode, servants ->
            mode == SupportSelectionModeEnum.Preferred
                    && servants.isNotEmpty()
        }
            .asLiveData()
    }

    val areCEsSelected by lazy {
        combine(
            prefs.support.selectionMode.asFlow(),
            prefs.support.preferredCEs.asFlow()
        ) { mode, ces ->
            mode == SupportSelectionModeEnum.Preferred
                    && ces.isNotEmpty()
        }
            .asLiveData()
    }
}