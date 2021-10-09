package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.NPSpamEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SkillSpamEnum

enum class SkillSpamTarget {
    None,
    Self,
    Slot1,
    Slot2,
    Slot3,
    Left,
    Right
}

data class SkillSpamConfig(
    val waves: Set<Int> = (1..3).toSet(),
    val spam: SkillSpamEnum = SkillSpamEnum.None,
    val target: SkillSpamTarget = SkillSpamTarget.None
)

data class NpSpamConfig(
    val waves: Set<Int> = (1..3).toSet(),
    val spam: NPSpamEnum = NPSpamEnum.None
)

data class ServantSpamConfig(
    val skills: List<SkillSpamConfig> = (1..3).map { SkillSpamConfig() },
    val np: NpSpamConfig = NpSpamConfig()
)