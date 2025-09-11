package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.SpamEnum

enum class SkillSpamTarget {
    None,
    Self,
    Slot1,
    Slot2,
    Slot3,
    Left,
    Right,
}

data class SkillSpamConfig(
    val waves: Set<Int> = (1..3).toSet(),
    val spam: SpamEnum = SpamEnum.None,
    val target: SkillSpamTarget = SkillSpamTarget.None,
)

data class NpSpamConfig(
    val waves: Set<Int> = (1..3).toSet(),
    val spam: SpamEnum = SpamEnum.None,
)

data class ServantSpamConfig(
    val skills: List<SkillSpamConfig> = (1..3).map { SkillSpamConfig() },
    val np: NpSpamConfig = NpSpamConfig(),
)

class SpamConfigPerTeamSlot(
    private val config: List<ServantSpamConfig>,
) {
    operator fun get(teamSlot: TeamSlot): ServantSpamConfig =
        config
            .getOrElse(teamSlot.position - 1) { ServantSpamConfig() }
}
