package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.modules.limitBrokenCharacter
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

class SupportPreferences {
    val friendNames: String
        get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return ""

            val friendSet = getStringSetPref(R.string.pref_support_friend_names, Prefs = prefs)

            val friendImgFolderName = supportFriendFolder.name

            val friendNames = friendSet
                .map { "${friendImgFolderName}/$it" }

            return friendNames.joinToString()
        }

    val preferredServants: String
        get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return ""

            val servantSet = getStringSetPref(R.string.pref_support_pref_servant, Prefs = prefs)

            val servants = mutableListOf<String>()

            for (servantEntry in servantSet) {
                val dir = File(supportServantImgFolder, servantEntry)

                if (dir.isDirectory && dir.exists()) {
                    val fileNames = dir.listFiles()
                        .filter { it.isFile }
                        // Give priority to later ascensions
                        .sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
                        .map { "${servantEntry}/${it.name}" }

                    servants.addAll(fileNames)
                } else servants.add(servantEntry)
            }

            val servantImgFolderName = supportServantImgFolder.name

            return servants.joinToString { "${servantImgFolderName}/$it" }
        }

    val preferredCEs: String
        get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return ""

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

    val friendsOnly: Boolean
        get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return false

            return getBoolPref(R.string.pref_support_friends_only, Prefs = prefs)
        }

    val selectionMode: SupportSelectionModeEnum
        get() {
            val default = SupportSelectionModeEnum.Preferred

            val pref = getPrefsForSelectedAutoSkill()
                ?: return default

            return getEnumPref(R.string.pref_support_mode, default, Prefs = pref)
        }

    val fallbackTo: SupportSelectionModeEnum
        get() {
            val default = SupportSelectionModeEnum.Manual

            val pref = getPrefsForSelectedAutoSkill()
                ?: return default

            return getEnumPref(R.string.pref_support_fallback, default, Prefs = pref)
        }
}