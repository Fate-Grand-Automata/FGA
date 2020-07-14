package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityActivity

class MoreSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        findPreference<Preference>(getString(R.string.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                startActivity(Intent(activity, CardPriorityActivity::class.java))
                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_nav_fine_tune))?.let {
            it.fragment = FineTuneSettingsFragment::class.java.name
        }
    }

    override fun onResume() {
        super.onResume()

        findPreference<Preference>(getString(R.string.pref_card_priority))?.let {
            it.summary = getStringPref(R.string.pref_card_priority, defaultCardPriority)
        }
    }
}
