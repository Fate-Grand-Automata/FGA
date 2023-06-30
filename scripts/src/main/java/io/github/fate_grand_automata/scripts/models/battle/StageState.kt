package io.github.fate_grand_automata.scripts.models.battle

import io.github.fate_grand_automata.scripts.models.EnemyTarget
import io.github.lib_automata.Pattern

class StageState {
    var stageCountSnapshot: Pattern? = null

    var chosenTarget: EnemyTarget? = null
}