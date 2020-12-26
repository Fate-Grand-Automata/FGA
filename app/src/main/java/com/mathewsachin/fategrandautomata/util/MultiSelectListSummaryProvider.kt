package com.mathewsachin.fategrandautomata.util

import androidx.annotation.StringRes
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.mathewsachin.fategrandautomata.R

class SupportMultiSelectSummaryProvider : MultiSelectSummaryProvider(
    R.string.battle_config_support_any
)

open class MultiSelectSummaryProvider(
    @StringRes val noneText: Int = R.string.p_not_set
) : Preference.SummaryProvider<MultiSelectListPreference> {
    override fun provideSummary(preference: MultiSelectListPreference) = when {
        preference.values.isNotEmpty() -> {
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
        }
        else -> preference.context.getString(noneText)
    }
}