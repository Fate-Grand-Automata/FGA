package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.modules.limitBrokenCharacter
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

class SupportPreferences {
    val friendNames: String get() {
        val friendSet = getStringSetPref(R.string.pref_support_friend_names)

        val friendImgFolderName = supportFriendFolder.name

        val friendNames = friendSet
            .map { "${friendImgFolderName}/$it" }

        return friendNames.joinToString()
    }

    val getAutoSkillPreferredServantCount: Int get() {
        val prefs = getPrefsForSelectedAutoSkill()
            ?: return 0

        val servants = getStringSetPref(R.string.pref_support_pref_servant, prefs)

        return servants.size
    }

    val getAutoSkillPreferredCEsCount: Int get() {
        val prefs = getPrefsForSelectedAutoSkill()
            ?: return 0

        val ces = getStringSetPref(R.string.pref_support_pref_ce, prefs)

        return ces.size
    }

    val preferredServants: String get() {
        val prefs = (if (getAutoSkillPreferredServantCount > 0) {
            getPrefsForSelectedAutoSkill()
        } else defaultPrefs) ?: return ""

        val servantSet = getStringSetPref(R.string.pref_support_pref_servant, Prefs = prefs)

        val servants = mutableListOf<String>()

        for (servantEntry in servantSet)
        {
            val dir = File(supportServantImgFolder, servantEntry)

            if (dir.isDirectory && dir.exists())
            {
                val fileNames = dir.listFiles()
                    .filter { it.isFile }
                    // Give priority to later ascensions
                    .sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
                    .map { "${servantEntry}/${it.name}" }

                servants.addAll(fileNames)
            }
            else servants.add(servantEntry)
        }

        val servantImgFolderName = supportServantImgFolder.name

        return servants.joinToString { "${servantImgFolderName}/$it" }
    }

    val preferredCEs: String get() {
        val prefs = (if (getAutoSkillPreferredCEsCount > 0) {
            getPrefsForSelectedAutoSkill()
        } else defaultPrefs) ?: return ""

        val ceSet = getStringSetPref(R.string.pref_support_pref_ce, Prefs = prefs)

        val ceImgFolderName = supportCeFolder.name

        var ces = ceSet
            .map { "${ceImgFolderName}/$it" }

        val mlb = getBoolPref(R.string.pref_support_pref_ce_mlb, Prefs = prefs)

        if (mlb) {
            ces = ces.map { "${limitBrokenCharacter}${it}" }
        }

        return ces.joinToString()
    }

    val friendsOnly get() = getBoolPref(R.string.pref_support_friends_only)

    val swipesPerUpdate get() = getIntPref(R.string.pref_support_swipes_per_update)

    val maxUpdates get() = getIntPref(R.string.pref_support_max_updates)

    val selectionMode: SupportSelectionModeEnum get() {
        val servants = getAutoSkillPreferredServantCount
        val ces = getAutoSkillPreferredCEsCount

        if (servants > 0 || ces > 0) {
            return SupportSelectionModeEnum.Preferred
        }

        return getEnumPref(R.string.pref_support_mode, SupportSelectionModeEnum.First)
    }

    val fallbackTo get() = getEnumPref(R.string.pref_support_fallback, SupportSelectionModeEnum.Manual)

    // 0.77 is a weird fix for MLB icon getting only 0.78 similarity sometimes
    val mlbSimilarity get() = getIntPref(R.string.pref_mlb_similarity, 77) / 100.0
}