package com.mathewsachin.fategrandautomata.prefs.core

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum

class SupportPrefsCore(
    maker: PrefMaker,
    val storageDirs: StorageDirs
) {
    val friendNames = maker.stringSet(R.string.pref_support_friend_names)

    val preferredServants = maker.stringSet(R.string.pref_support_pref_servant)

    val mlb = maker.bool(R.string.pref_support_pref_ce_mlb)

    val preferredCEs = maker.stringSet(R.string.pref_support_pref_ce)

    val friendsOnly = maker.bool(R.string.pref_support_friends_only)

    val selectionMode = maker.enum(
        R.string.pref_support_mode,
        SupportSelectionModeEnum.Preferred
    )

    val fallbackTo = maker.enum(
        R.string.pref_support_fallback,
        SupportSelectionModeEnum.Manual
    )

    val supportClass = maker.enum(
        R.string.pref_autoskill_support_class,
        SupportClass.None
    )
}