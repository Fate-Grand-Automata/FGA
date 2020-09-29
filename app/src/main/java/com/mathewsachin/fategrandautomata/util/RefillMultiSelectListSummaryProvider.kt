package com.mathewsachin.fategrandautomata.util

import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.mathewsachin.fategrandautomata.R

class RefillMultiSelectListSummaryProvider : Preference.SummaryProvider<MultiSelectListPreference> {
    override fun provideSummary(preference: MultiSelectListPreference): CharSequence {
        return if (preference.values.isNotEmpty()) {
            val selectedLabels = preference.values.map {
                val index = preference.findIndexOfValue(it)

                preference.entries[index]
            }

            selectedLabels.joinToString()
        } else preference.context.getString(R.string.p_refill_none)
    }
}