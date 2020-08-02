package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R

class SkillLevelSettingsFragment : PreferenceFragmentCompat() {
    val args: SkillLevelSettingsFragmentArgs by navArgs()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key

        setPreferencesFromResource(R.xml.skill_level_preferences, rootKey)
    }
}
