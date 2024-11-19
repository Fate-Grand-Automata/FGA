package io.github.fate_grand_automata.ui.skill_maker

import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill

sealed class SkillMakerNav {
    data object Main : SkillMakerNav()
    data object MasterSkills : SkillMakerNav()
    data object Atk : SkillMakerNav()
    data object OrderChange : SkillMakerNav()
    data class SkillTarget(val skill: Skill) : SkillMakerNav()
    data class TwoTargets(val skill: Skill) : SkillMakerNav()
    data class ThreeTargets(val skill: Skill) : SkillMakerNav()
    data class Choice2(val skill: Skill, val slot: SkillSlot) : SkillMakerNav()
    data class Choice2Target(val skill: Skill, val firstTarget: ServantTarget) : SkillMakerNav()
    data class Choice3(val skill: Skill, val slot: SkillSlot) : SkillMakerNav()
}