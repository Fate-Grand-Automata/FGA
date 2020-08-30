package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.models.*

sealed class AutoSkillAction {
    class CardsBeforeNP(val count: Int) : AutoSkillAction() {
        init {
            require(count in 1..2) { "Only 1 or 2 cards can be used before NP" }
        }
    }

    class NP(val np: CommandCard.NP) : AutoSkillAction()

    class ServantSkill(val skill: Skill.Servant, val target: ServantTarget?) : AutoSkillAction()

    class MasterSkill(val skill: Skill.Master, val target: ServantTarget?) : AutoSkillAction()

    class TargetEnemy(val enemy: EnemyTarget) : AutoSkillAction()

    class OrderChange(
        val starting: OrderChangeMember.Starting,
        val sub: OrderChangeMember.Sub
    ) : AutoSkillAction()

    object NoOp : AutoSkillAction()
}