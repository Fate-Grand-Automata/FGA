package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.StorageDirs
import java.io.File
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
    }
}

fun EditTextPreference.makeMultiLine() {
    setOnBindEditTextListener {
        it.isSingleLine = false
    }
}

fun PreferenceFragmentCompat.findServantList() =
    findPreference<MultiSelectListPreference>(getString(prefKeys.pref_support_pref_servant))

fun PreferenceFragmentCompat.findCeList() =
    findPreference<MultiSelectListPreference>(getString(prefKeys.pref_support_pref_ce))

fun PreferenceFragmentCompat.findFriendNamesList() =
    findPreference<MultiSelectListPreference>(getString(prefKeys.pref_support_friend_names))

fun PreferenceFragmentCompat.preferredSupportOnCreate() {
    val servants = findServantList() ?: return
    servants.summaryProvider = SupportMultiSelectListSummaryProvider()

    val ces = findCeList() ?: return
    ces.summaryProvider = SupportMultiSelectListSummaryProvider()

    findFriendNamesList()?.apply {
        summaryProvider = SupportMultiSelectListSummaryProvider()
    }
}

private fun MultiSelectListPreference.populateFriendOrCe(ImgFolder: File) {
    val entries = (ImgFolder.listFiles() ?: emptyArray())
        .filter { it.isFile }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    // actual values
    this.entryValues = entries
        .map { it.name }
        .toTypedArray()

    // labels
    this.entries = entries
        .map { it.nameWithoutExtension }
        .toTypedArray()
}

fun PreferenceFragmentCompat.preferredSupportOnResume(storageDirs: StorageDirs) {
    val servants = findServantList() ?: return
    val ces = findCeList() ?: return
    val friendNames = findFriendNamesList() ?: return

    servants.apply {
        val entries = (storageDirs.supportServantImgFolder.listFiles() ?: emptyArray())
            .map { it.name }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
            .toTypedArray()

        this.entryValues = entries
        this.entries = entries
    }

    ces.apply {
        populateFriendOrCe(storageDirs.supportCeFolder)
    }

    friendNames.apply {
        populateFriendOrCe(storageDirs.supportFriendFolder)
    }
}