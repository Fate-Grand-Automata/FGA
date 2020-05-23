package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.AutoSkillListActivity

class AutoSkillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.autoskill_preferences, rootKey)

        findPreference<Preference>(getString(R.string.pref_autoskill_manage))?.let {
            it.setOnPreferenceClickListener {
                startActivity(Intent(activity, AutoSkillListActivity::class.java))
                true
            }
        }
    }
}
