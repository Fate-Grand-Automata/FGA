package com.mathewsachin.fategrandautomata.scripts.models.battle

import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.libautomata.Pattern

class StageState {
    var stageCountSnapshot: Pattern? = null

    var chosenTarget: EnemyTarget? = null
}