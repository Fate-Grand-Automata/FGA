package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.NpSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.ServantSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpamSettingsViewModel @Inject constructor(
    val preferences: IPreferences,
    savedState: SavedStateHandle
): ViewModel() {
    val battleConfigKey: String = savedState[SpamSettingsFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    private val battleConfig = preferences.forBattleConfig(battleConfigKey)
    private val spamConfig = battleConfig.spam

    var selectedServant by mutableStateOf(0)

    data class NpSpamState(
        val spamMode: MutableState<SpamEnum>,
        val waves: MutableState<Set<Int>>
    )

    data class SkillSpamState(
        val spamMode: MutableState<SpamEnum>,
        val target: MutableState<SkillSpamTarget>,
        val waves: MutableState<Set<Int>>
    )

    data class SpamState(
        val np: NpSpamState,
        val skills: List<SkillSpamState>
    )

    val spamStates = spamConfig
        .map {
            SpamState(
                np = NpSpamState(
                    mutableStateOf(it.np.spam),
                    mutableStateOf(it.np.waves)
                ),
                skills = it.skills.map { skill ->
                    SkillSpamState(
                        spamMode = mutableStateOf(skill.spam),
                        target = mutableStateOf(skill.target),
                        waves = mutableStateOf(skill.waves)
                    )
                }
            )
        }

    fun save() {
        battleConfig.spam = spamStates.map {
            ServantSpamConfig(
                np = NpSpamConfig(
                    spam = it.np.spamMode.value,
                    waves = it.np.waves.value
                ),
                skills = it.skills.map { skill ->
                    SkillSpamConfig(
                        spam = skill.spamMode.value,
                        target = skill.target.value,
                        waves = skill.waves.value
                    )
                }
            )
        }
    }
}