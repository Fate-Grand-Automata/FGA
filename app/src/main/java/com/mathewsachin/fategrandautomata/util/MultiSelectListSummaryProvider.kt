package com.mathewsachin.fategrandautomata.util

import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference

class MultiSelectListSummaryProvider : Preference.SummaryProvider<MultiSelectListPreference>
{
    override fun provideSummary(preference: MultiSelectListPreference): CharSequence {
        return if (preference.values.size > 0)  { preference.values.joinToString() }
        else "Any"
    }
}