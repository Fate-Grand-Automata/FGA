package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.makeNumeric

class RefillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(R.string.pref_refill_repetitions))?.makeNumeric()
    }
}
