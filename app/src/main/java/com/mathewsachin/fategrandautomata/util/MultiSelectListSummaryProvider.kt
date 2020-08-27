package com.mathewsachin.fategrandautomata.util

import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference

class MultiSelectListSummaryProvider : Preference.SummaryProvider<MultiSelectListPreference> {
    override fun provideSummary(preference: MultiSelectListPreference): CharSequence {
        return if (preference.values.isNotEmpty()) {
            val selectedLabels = preference.values.map {
                val index = preference.findIndexOfValue(it)

                // Index out of Bounds exception can happen if the selected images were deleted
                if (preference.entries?.isNotEmpty() == true
                    && index in preference.entries.indices
                ) {
                    preference.entries[index]
                } else it
            }

            selectedLabels.joinToString()
        } else "Any"
    }
}