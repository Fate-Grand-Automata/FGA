package io.github.fate_grand_automata.ui.spam

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.NpGaugeEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.enums.StarConditionEnum
import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.AutoSkillCommand
import io.github.fate_grand_automata.scripts.models.NpSpamConfig
import io.github.fate_grand_automata.scripts.models.ServantSpamConfig
import io.github.fate_grand_automata.scripts.models.SkillSpamConfig
import io.github.fate_grand_automata.scripts.models.SkillSpamTarget
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerEntry
import javax.inject.Inject

@HiltViewModel
class SpamScreenViewModel @Inject constructor(
    val battleConfig: IBattleConfig,
    val battleConfigCore: BattleConfigCore
) : ViewModel() {
    private val spamConfig = battleConfig.spam

    data class NpSpamState(
        val spamMode: MutableState<SpamEnum>,
        val waves: MutableState<Set<Int>>
    )

    data class SkillSpamState(
        val spamMode: MutableState<SpamEnum>,
        val npMode: MutableState<NpGaugeEnum>,
        val starCond: MutableState<StarConditionEnum>,
        @Deprecated("Use act instead")
        val target: MutableState<SkillSpamTarget>,
        val waves: MutableState<Set<Int>>,
        val act: MutableState<AutoSkillAction?>,
        val priority: MutableIntState,
        val repeatLimit: MutableIntState
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
                        npMode = mutableStateOf(skill.np),
                        starCond = mutableStateOf(skill.star),
                        target = mutableStateOf(skill.target),
                        waves = mutableStateOf(skill.waves),
                        act = mutableStateOf(AutoSkillCommand.parse(skill.act).stages.flatten().flatten().firstOrNull()),
                        priority = mutableIntStateOf(skill.priority),
                        repeatLimit = mutableIntStateOf(skill.maxRepeatCount.coerceIn(1, 99))
                    )
                }
            )
        }

    data class SpamPreset(val name: String, val action: (List<SpamState>) -> Unit)

    private fun applyPreset(state: List<SpamState>, spamMode: SpamEnum) {
        val allWaves = setOf(1, 2, 3)

        var priority = 0

        state.forEach { servant ->
            servant.np.spamMode.value = spamMode
            servant.np.waves.value = allWaves

            servant.skills.forEach { skill ->
                skill.spamMode.value = spamMode
                skill.npMode.value = NpGaugeEnum.None
                skill.starCond.value = StarConditionEnum.None
                skill.target.value = SkillSpamTarget.Self
                skill.waves.value = allWaves
                skill.act.value = null
                skill.priority.intValue = priority++
                skill.repeatLimit.intValue = 1
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

    fun onSkillDragged(newList: List<SkillWithServantRef>) {
        newList.forEachIndexed { index, skill ->
            skill.state.priority.intValue = index
        }
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
                        np = skill.npMode.value,
                        star = skill.starCond.value,
                        target = skill.target.value,
                        waves = skill.waves.value,
                        act = skill.act.value
                            ?.let { actValue -> SkillMakerEntry.Action(actValue).toString() }
                            ?: "",
                        priority = skill.priority.intValue,
                        maxRepeatCount = skill.repeatLimit.intValue.coerceIn(1, 99)
                    )
                }
            )
        }
    }
}