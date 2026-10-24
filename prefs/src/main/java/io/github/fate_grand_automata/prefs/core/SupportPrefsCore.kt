package io.github.fate_grand_automata.prefs.core

import io.github.fate_grand_automata.scripts.enums.BondCEEffectEnum
import io.github.fate_grand_automata.scripts.enums.AppendSkillRequirement
import io.github.fate_grand_automata.scripts.enums.ClassScoreRequirement
import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum

class SupportPrefsCore(
    maker: PrefMaker
) {
    val friendNames = maker.stringSet("support_friend_names_list")
    val preferredServants = maker.stringSet("support_pref_servant_list")
    val mlb = maker.bool("support_pref_ce_mlb")
    val preferredCEs = maker.stringSet("support_pref_ce_list")
    val friendsOnly = maker.bool("support_friends_only")

    val selectionMode = maker.enum(
        "support_mode",
        SupportSelectionModeEnum.Preferred
    )

    val fallbackTo = maker.enum(
        "support_fallback",
        SupportSelectionModeEnum.Manual
    )

    val supportClass = maker.enum(
        "autoskill_support_class",
        SupportClass.None
    )

    val alsoCheckAll = maker.bool("also_check_all")

    val maxAscended = maker.bool("support_max_ascended")

    val skill1Max = maker.bool("support_skill_max_1")
    val skill2Max = maker.bool("support_skill_max_2")
    val skill3Max = maker.bool("support_skill_max_3")

    val appendSkill1 = maker.enum("support_append_skill_1", AppendSkillRequirement.Ignore)
    val appendSkill2 = maker.enum("support_append_skill_2", AppendSkillRequirement.Ignore)
    val appendSkill3 = maker.enum("support_append_skill_3", AppendSkillRequirement.Ignore)
    val appendSkill4 = maker.enum("support_append_skill_4", AppendSkillRequirement.Ignore)
    val appendSkill5 = maker.enum("support_append_skill_5", AppendSkillRequirement.Ignore)
    val classScore = maker.enum("support_class_score", ClassScoreRequirement.Ignore)

    val grandServant = maker.bool("support_grand_servant")
    val bondCEEffect = maker.enum(
        "support_bond_ce_effect",
        BondCEEffectEnum.Ignore
    )
    val requireBothNormalAndRewardMatch = maker.bool("support_require_both_normal_and_reward_match")
}
