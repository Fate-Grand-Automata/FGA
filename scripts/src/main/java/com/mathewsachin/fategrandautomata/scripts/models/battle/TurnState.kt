package com.mathewsachin.fategrandautomata.scripts.models.battle

import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction

class TurnState {
    var hasClickedAttack = false
    var atk: AutoSkillAction.Atk = AutoSkillAction.Atk.noOp()
}