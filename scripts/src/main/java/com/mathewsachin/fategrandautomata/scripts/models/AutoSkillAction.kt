package com.mathewsachin.fategrandautomata.scripts.models

sealed class AutoSkillAction {
    data class Atk constructor(val nps: Set<CommandCard.NP>, val cardsBeforeNP: Int) : AutoSkillAction() {
        init {
            require(cardsBeforeNP in 0..2) { "Only 0, 1 or 2 cards can be used before NP" }
        }

        operator fun plus(other: Atk) =
            Atk(nps + other.nps, cardsBeforeNP + other.cardsBeforeNP)

        fun toNPUsage() =
            NPUsage(nps, cardsBeforeNP)

        companion object {
            fun noOp() = Atk(emptySet(), 0)

            fun np(np: CommandCard.NP) = Atk(setOf(np), 0)

            fun cardsBeforeNP(cards: Int) = Atk(emptySet(), cards)
        }
    }

    class ServantSkill(val skill: Skill.Servant, val targets: List<ServantTarget>?) : AutoSkillAction()

    class MasterSkill(val skill: Skill.Master, val target: ServantTarget?) : AutoSkillAction()

    class TargetEnemy(val enemy: EnemyTarget) : AutoSkillAction()

    class OrderChange(
        val starting: OrderChangeMember.Starting,
        val sub: OrderChangeMember.Sub
    ) : AutoSkillAction()
}