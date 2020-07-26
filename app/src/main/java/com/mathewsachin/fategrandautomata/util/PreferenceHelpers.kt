package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.*
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
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
    servants.summaryProvider = MultiSelectListSummaryProvider()

    val ces = findCeList() ?: return
    ces.summaryProvider = MultiSelectListSummaryProvider()

    findFriendNamesList()?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }

    fun adjust(selectionMode: String) =
        adjustVisibility(enumValueOf(selectionMode))

    val supportMode = findPreference<ListPreference>(getString(prefKeys.pref_support_mode))
        ?: return

    supportMode.let {
        it.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                adjust(newValue)
            }
            true
        }

        adjust(it.value)
    }

    val preferenceChangeListener = { _: Any, _: Any ->
        if (supportMode.value.isNotBlank()) {
            GlobalScope.launch {
                // we want this to run only after preference has updated
                // So, we add a delay here
                delay(100)
                adjust(supportMode.value)
            }
        }
        true
    }

    servants.setOnPreferenceChangeListener(preferenceChangeListener)
    ces.setOnPreferenceChangeListener(preferenceChangeListener)
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
    val ceMlb = findPreference<Preference>(getString(prefKeys.pref_support_pref_ce_mlb)) ?: return
    val friendNames = findFriendNamesList() ?: return
    val fallback = findPreference<Preference>(getString(prefKeys.pref_support_fallback)) ?: return
    val friendsOnly =
        findPreference<Preference>(getString(prefKeys.pref_support_friends_only)) ?: return
    val skillLevels =
        findPreference<Preference>(getString(prefKeys.pref_nav_skill_lvl)) ?: return

    val modePreferred = selectionMode == SupportSelectionModeEnum.Preferred
    val modeFriend = selectionMode == SupportSelectionModeEnum.Friend

    servants.isVisible = modePreferred
    ces.isVisible = modePreferred
    ceMlb.isVisible = modePreferred && ces.values.isNotEmpty()
    friendNames.isVisible = modeFriend
    fallback.isVisible = modePreferred || modeFriend
    friendsOnly.isVisible = modePreferred
    skillLevels.isVisible = modePreferred && servants.values.isNotEmpty()
}

fun PreferenceFragmentCompat.preferredSupportOnResume(storageDirs: StorageDirs) {
    val servants = findServantList() ?: return
    val ces = findCeList() ?: return
    val friendNames = findFriendNamesList() ?: return

    servants.apply {
        val entries = storageDirs.supportServantImgFolder.listFiles()
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