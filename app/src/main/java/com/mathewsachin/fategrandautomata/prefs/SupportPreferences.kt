package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.modules.limitBrokenCharacter
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

class SupportPreferences(val prefs: SharedPreferenceDelegation) {
    private val friendNamePref = prefs.stringSet(R.string.pref_support_friend_names)
    val friendSet by friendNamePref

    val friendNames by friendNamePref.map { friendSet ->
        val friendImgFolderName = supportFriendFolder.name

        val friendNames = friendSet
            .map { "${friendImgFolderName}/$it" }

        friendNames.joinToString()
    }

    private val servantPref = prefs.stringSet(R.string.pref_support_pref_servant)
    val servantSet by servantPref

    val preferredServants by servantPref.map { servantSet ->
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

        servants.joinToString { "${servantImgFolderName}/$it" }
    }

    val mlb by prefs.bool(R.string.pref_support_pref_ce_mlb)

    private val cePref = prefs.stringSet(R.string.pref_support_pref_ce)
    val ceSet by cePref

    val preferredCEs by cePref.map { ceSet ->
        val ceImgFolderName = supportCeFolder.name

        var ces = ceSet
            .map { "${ceImgFolderName}/$it" }

        if (mlb) {
            ces = ces.map { "${limitBrokenCharacter}${it}" }
        }

        ces.joinToString()
    }

    val friendsOnly by prefs.bool(R.string.pref_support_friends_only)

    val selectionMode by prefs.enum(R.string.pref_support_mode, SupportSelectionModeEnum.Preferred)

    val fallbackTo by prefs.enum(R.string.pref_support_mode, SupportSelectionModeEnum.Manual)
}