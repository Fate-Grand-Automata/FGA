package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.*
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
    }
}

fun PreferenceFragmentCompat.findServantList() =
    findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_servant))

fun PreferenceFragmentCompat.findCeList() =
    findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_ce))

fun PreferenceFragmentCompat.findFriendNamesList() =
    findPreference<MultiSelectListPreference>(getString(R.string.pref_support_friend_names))

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

    fun adjust(selectionMode: String) {
        adjustVisibility(enumValueOf(selectionMode))
    }

    findPreference<ListPreference>(getString(R.string.pref_support_mode))?.let {
        it.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                adjust(newValue)
            }
            true
        }

        adjust(it.value)
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

fun PreferenceFragmentCompat.adjustVisibility(selectionMode: SupportSelectionModeEnum) {
    val servants = findServantList() ?: return
    val ces = findCeList() ?: return
    val ceMlb = findPreference<Preference>(getString(R.string.pref_support_pref_ce_mlb)) ?: return
    val friendNames = findFriendNamesList() ?: return
    val fallback = findPreference<Preference>(getString(R.string.pref_support_fallback)) ?: return
    val friendsOnly =
        findPreference<Preference>(getString(R.string.pref_support_friends_only)) ?: return

    val modePreferred = selectionMode == SupportSelectionModeEnum.Preferred
    val modeFriend = selectionMode == SupportSelectionModeEnum.Friend

    servants.isVisible = modePreferred
    ces.isVisible = modePreferred
    ceMlb.isVisible = modePreferred
    friendNames.isVisible = modeFriend
    fallback.isVisible = modePreferred || modeFriend
    friendsOnly.isVisible = modePreferred || modeFriend
}

fun PreferenceFragmentCompat.preferredSupportOnResume() {
    val servants = findServantList() ?: return
    val ces = findCeList() ?: return
    val friendNames = findFriendNamesList() ?: return

    servants.apply {
        val entries = supportServantImgFolder.listFiles()
            .map { it.name }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
            .toTypedArray()

        this.entryValues = entries
        this.entries = entries
    }

    ces.apply {
        populateFriendOrCe(supportCeFolder)
    }

    friendNames.apply {
        populateFriendOrCe(supportFriendFolder)
    }
}