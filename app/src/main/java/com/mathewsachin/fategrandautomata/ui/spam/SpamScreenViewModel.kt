package com.mathewsachin.fategrandautomata.ui.spam

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.NpSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.ServantSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamConfig
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpamScreenViewModel @Inject constructor(
    val battleConfig: IBattleConfig,
    val battleConfigCore: BattleConfigCore
): ViewModel() {
    private val spamConfig = battleConfig.spam

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

    data class SpamPreset(val name: String, val action: (List<SpamState>) -> Unit)

    private fun applyPreset(state: List<SpamState>, spamMode: SpamEnum) {
        val allWaves = setOf(1, 2, 3)

        state.forEach { servant ->
            servant.np.spamMode.value = spamMode
            servant.np.waves.value = allWaves

            servant.skills.forEach { skill ->
                skill.spamMode.value = spamMode
                skill.target.value = SkillSpamTarget.Self
                skill.waves.value = allWaves
            }
        }
    }

    // TODO: Localize
    val presets = listOf(
        SpamPreset("SPAM ALL") {
            applyPreset(it, SpamEnum.Spam)
        },
        SpamPreset("SPAM ALL DANGER") {
            applyPreset(it, SpamEnum.Danger)
        },
        SpamPreset("NO SPAM") {
            applyPreset(it, SpamEnum.None)
        }
    )

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