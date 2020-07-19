package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.ui.AutoSkillListActivity
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class MainSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            it.fragment = RefillSettingsFragment::class.java.name
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_auto_skill))?.let {
            it.setOnPreferenceClickListener {
                startActivity(Intent(activity, AutoSkillListActivity::class.java))
                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_more))?.let {
            it.fragment = MoreSettingsFragment::class.java.name
        }
    }

    override fun onResume() {
        super.onResume()

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            val prefs = Preferences.refill
            it.summary = when (prefs.enabled) {
                true -> "${prefs.resource} x${prefs.repetitions}"
                false -> "OFF"
            }
        }
    }
}
