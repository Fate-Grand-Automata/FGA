package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R

class MoreSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
    }
}
