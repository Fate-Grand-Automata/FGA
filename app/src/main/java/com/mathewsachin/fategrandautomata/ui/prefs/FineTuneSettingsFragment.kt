package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R

class FineTuneSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fine_tune_preferences, rootKey)
    }
}
