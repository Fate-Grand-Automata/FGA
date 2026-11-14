package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.models.SkillUpgradeState
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Reads the Skill Enhancement screen during auto-detection, so the launcher can show the current
 * skill levels.
 */
@ScriptScope
class AutoSkillSetup @Inject constructor(
    api: IFgoAutomataApi,
    private val enhancementScreen: EnhancementScreen,
    private val skillLevelReader: SkillLevelReader,
    private val state: SkillUpgradeState,
) : IFgoAutomataApi by api {

    /**
     * Must be called outside of `useSameSnapIn`, so that each retry gets a fresh screenshot.
     */
    fun readSkillScreen() {
        state.isNothingSelected = enhancementScreen.isNothingSelected()
        if (state.isNothingSelected) {
            return
        }

        state.currentLevels = (1..3).map { readWithRetries(it) }
    }

    private fun readWithRetries(skillNumber: Int): SkillLevel {
        var level = skillLevelReader.read(skillNumber)
        repeat(2) {
            if (level != SkillLevel.Unreadable) {
                return level
            }

            0.5.seconds.wait()
            level = skillLevelReader.read(skillNumber)
        }

        return level
    }
}
