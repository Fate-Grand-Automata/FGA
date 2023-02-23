package com.mathewsachin.fategrandautomata.ui.skill_maker

import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.scripts.models.Skill

sealed class SkillMakerNav {
    object Main : SkillMakerNav()
    object MasterSkills : SkillMakerNav()
    object Atk : SkillMakerNav()
    object OrderChange : SkillMakerNav()
    data class SkillTarget(val skill: Skill) : SkillMakerNav()
    data class Emiya(val skill: Skill) : SkillMakerNav()
    data class SpaceIshtar(val skill: Skill) : SkillMakerNav()
    data class Kukulcan(val skill: Skill) : SkillMakerNav()
    data class KukulcanTarget(val skill: Skill, val firstTarget: ServantTarget) : SkillMakerNav()
}