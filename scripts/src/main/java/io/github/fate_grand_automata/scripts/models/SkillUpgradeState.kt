package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.modules.SkillLevel
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

/**
 * Levels of the three normal skills, index 0 being skill 1. Auto-detection fills in what is on
 * screen, the launcher the targets, and the run that follows reads both. Every run gets a new
 * instance, so nothing carries over to another Servant.
 */
@ScriptScope
class SkillUpgradeState @Inject constructor() {
    var currentLevels: List<SkillLevel> = List(3) { SkillLevel.Locked }

    var targetLevels: List<Int> = List(3) { 0 }

    var isNothingSelected = false
}
