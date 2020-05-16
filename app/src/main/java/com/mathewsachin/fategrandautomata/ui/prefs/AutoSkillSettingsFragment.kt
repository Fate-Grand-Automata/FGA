package com.mathewsachin.fategrandautomata.ui.prefs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringSetPref
import com.mathewsachin.fategrandautomata.ui.AutoSkillListActivity
import com.mathewsachin.fategrandautomata.util.AutoSkillEntry

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
            val autoSkillItems = getStringSetPref(R.string.pref_autoskill_list)
                .map {
                    val sharedPrefs = requireActivity().getSharedPreferences(it, Activity.MODE_PRIVATE)

                    AutoSkillEntry(
                        it,
                        getStringPref(R.string.pref_autoskill_name, "--", Prefs = sharedPrefs)
                    )
                }
                .sortedBy{ it.Name }

            this.entryValues = autoSkillItems.map { it.Id }.toTypedArray()
            this.entries = autoSkillItems.map { it.Name }.toTypedArray()

            val actual = getStringPref(R.string.pref_autoskill_selected)
            this.value = "" // Force update
            this.value = actual
        }
    }
}
