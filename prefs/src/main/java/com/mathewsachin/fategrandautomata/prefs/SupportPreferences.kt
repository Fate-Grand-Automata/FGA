package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.SupportPrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.modules.limitBrokenCharacter
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import java.io.File

fun mapFriendNames(
    storageDirs: StorageDirs,
    friendSet: Set<String>
): String {
    val friendImgFolderName = storageDirs.supportFriendFolder.name

    val friendNames = friendSet
        .map { "${friendImgFolderName}/$it" }

    return friendNames.joinToString()
}

fun mapPreferredServants(
    storageDirs: StorageDirs,
    servantSet: Set<String>
): String {
    val servants = mutableListOf<String>()

    for (servantEntry in servantSet) {
        val dir = File(storageDirs.supportServantImgFolder, servantEntry)

        if (dir.isDirectory && dir.exists()) {
            val fileNames = dir.listFiles()
                .filter { it.isFile }
                // Give priority to later ascensions
                .sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
                .map { "${servantEntry}/${it.name}" }

            servants.addAll(fileNames)
        } else servants.add(servantEntry)
    }

    val servantImgFolderName = storageDirs.supportServantImgFolder.name

    return servants.joinToString { "${servantImgFolderName}/$it" }
}

fun mapPreferredCEs(
    storageDirs: StorageDirs,
    ceSet: Set<String>,
    mlb: Boolean
): String {
    val ceImgFolderName = storageDirs.supportCeFolder.name

    var ces = ceSet
        .map { "${ceImgFolderName}/$it" }

    if (mlb) {
        ces = ces.map { "$limitBrokenCharacter${it}" }
    }

    return ces.joinToString()
}

internal class SupportPreferences(
    val prefs: SupportPrefsCore,
    val storageDirs: StorageDirs
) : ISupportPreferences {
    override val friendNames by prefs.friendNames
        .map { mapFriendNames(storageDirs, it) }

    override val preferredServants by prefs.preferredServants
        .map { mapPreferredServants(storageDirs, it) }

    override val mlb by prefs.mlb

    override val preferredCEs by prefs.preferredCEs
        .map { mapPreferredCEs(storageDirs, it, mlb) }

    override val friendsOnly by prefs.friendsOnly

    override val selectionMode by prefs.selectionMode

    override val fallbackTo by prefs.fallbackTo

    override val supportClass by prefs.supportClass

    override val skill1Max by prefs.skill1Max
    override val skill2Max by prefs.skill2Max
    override val skill3Max by prefs.skill3Max
}