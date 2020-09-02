package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.combine

class AutoSkillItemViewModel @ViewModelInject constructor(
    val preferences: IPreferences,
    val prefsCore: PrefsCore,
    @Assisted savedState: SavedStateHandle
) : ViewModel() {
    val autoSkillItemKey: String = savedState[AutoSkillItemSettingsFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get AutoSkill key")

    private val prefs = prefsCore.forAutoSkillConfig(autoSkillItemKey)

    val cardPriority = prefs
        .cardPriority
        .asFlow()
        .asLiveData()

    val skillCommand = prefs
        .skillCommand
        .asFlow()
        .asLiveData()

    val skillLevels =
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

    val supportSelectionMode = prefs
        .support
        .selectionMode
        .asFlow()
        .asLiveData()

    val areServantsSelected =
        combine(
            prefs.support.selectionMode.asFlow(),
            prefs.support.preferredServants.asFlow()
        ) { mode, servants ->
            mode == SupportSelectionModeEnum.Preferred
                    && servants.isNotEmpty()
        }
            .asLiveData()

    val areCEsSelected =
        combine(
            prefs.support.selectionMode.asFlow(),
            prefs.support.preferredCEs.asFlow()
        ) { mode, ces ->
            mode == SupportSelectionModeEnum.Preferred
                    && ces.isNotEmpty()
        }
            .asLiveData()
}