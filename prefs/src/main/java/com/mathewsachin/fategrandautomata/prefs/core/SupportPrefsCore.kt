package com.mathewsachin.fategrandautomata.prefs.core

import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum

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

    val maxAscended = maker.bool("support_max_ascended")

    val skill1Max = maker.bool("support_skill_max_1")
    val skill2Max = maker.bool("support_skill_max_2")
    val skill3Max = maker.bool("support_skill_max_3")
}