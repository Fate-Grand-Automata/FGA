package io.github.fate_grand_automata.ui.skill_maker

import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.skill_maker.SkillSlot.First
import io.github.fate_grand_automata.ui.skill_maker.SkillSlot.Second
import io.github.fate_grand_automata.ui.skill_maker.SkillSlot.Third

/**
 * Enum class for skill slots
 *
 * @property First first skill slot
 * @property Second second skill slot
 * @property Third third skill slot
 * @property ANY any skill slot
 */
enum class SkillSlot {
    First,
    Second,
    Third,

    ANY,

    ;

    fun matches(slot: SkillSlot): Boolean = this == ANY || this == slot
}

fun Skill.slot() = when (this) {
    in Skill.Servant.skill1 -> First
    in Skill.Servant.skill2 -> Second
    in Skill.Servant.skill3 -> Third
    else -> null
}
