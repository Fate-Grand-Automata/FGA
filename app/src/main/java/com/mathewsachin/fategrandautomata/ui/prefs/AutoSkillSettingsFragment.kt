package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.ui.AutoSkillListActivity
import com.mathewsachin.fategrandautomata.util.getAutoSkillEntries

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

    override fun onResume() {
        super.onResume()

        findPreference<ListPreference>(getString(R.string.pref_autoskill_selected))?.apply {
            val autoSkillItems = getAutoSkillEntries()

            this.entryValues = autoSkillItems.map { it.Id }.toTypedArray()
            this.entries = autoSkillItems.map { it.Name }.toTypedArray()

            val actual = getStringPref(R.string.pref_autoskill_selected)
            this.value = "" // Force update
            this.value = actual
        }
    }
}
