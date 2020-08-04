package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.preference.MultiSelectListPreference
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat

class ClearMultiSelectListPreferenceDialog : MultiSelectListPreferenceDialogFragmentCompat() {
    fun setKey(key: String) {
        arguments = bundleOf(ARG_KEY to key)
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        builder.setNeutralButton("Clear") { _, _ ->
            (preference as MultiSelectListPreference)
                .values = emptySet()
        }
    }
}