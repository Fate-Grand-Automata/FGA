package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
    }
}

fun PreferenceFragmentCompat.findServantList() = findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_servant))
fun PreferenceFragmentCompat.findCeList() = findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_ce))
fun PreferenceFragmentCompat.findFriendNamesList() = findPreference<MultiSelectListPreference>(getString(R.string.pref_support_friend_names))

fun PreferenceFragmentCompat.preferredSupportOnCreate() {
    findServantList()?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }

    findCeList()?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }

    findFriendNamesList()?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }
}

private fun MultiSelectListPreference.populateFriendOrCe(ImgFolder: File) {
    val entries = ImgFolder.listFiles()
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

fun PreferenceFragmentCompat.preferredSupportOnResume() {
    findServantList()?.apply {
        val entries = supportServantImgFolder.listFiles()
            .map { it.name }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
            .toTypedArray()

        this.entryValues = entries
        this.entries = entries
    }

    findCeList()?.apply {
        populateFriendOrCe(supportCeFolder)
    }

    findFriendNamesList()?.apply {
        populateFriendOrCe(supportFriendFolder)
    }
}