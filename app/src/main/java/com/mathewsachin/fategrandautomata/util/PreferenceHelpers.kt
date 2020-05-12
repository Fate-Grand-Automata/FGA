package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
    }
}

fun PreferenceFragmentCompat.preferredSupportOnCreate() {
    val servantList = findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_servant))?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }

    // clear servant list
    findPreference<Preference>(getString(R.string.pref_support_pref_servant_clear))?.let {
        it.setOnPreferenceClickListener {
            servantList?.values = setOf()
            true
        }
    }

    val ceList = findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_ce))?.apply {
        summaryProvider = MultiSelectListSummaryProvider()
    }

    // clear CE list
    findPreference<Preference>(getString(R.string.pref_support_pref_ce_clear))?.let {
        it.setOnPreferenceClickListener {
            ceList?.values = setOf()
            true
        }
    }
}

fun PreferenceFragmentCompat.preferredSupportOnResume() {
    findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_servant))?.apply {
        val entries = supportServantImgFolder.listFiles()
            .map { it.name }
            .sorted()
            .toTypedArray()

        this.entryValues = entries
        this.entries = entries
    }

    findPreference<MultiSelectListPreference>(getString(R.string.pref_support_pref_ce))?.apply {
        val entries = supportCeFolder.listFiles()
            .filter { it.isFile }
            .map { it.name }
            .sorted()
            .toTypedArray()

        this.entryValues = entries
        this.entries = entries
    }
}