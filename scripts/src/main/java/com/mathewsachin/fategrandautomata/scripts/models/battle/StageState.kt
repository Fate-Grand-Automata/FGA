package com.mathewsachin.fategrandautomata.scripts.models.battle

import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.libautomata.IPattern

class StageState {
    var stageCountSnaphot: IPattern? = null

    var chosenTarget: EnemyTarget? = null
}