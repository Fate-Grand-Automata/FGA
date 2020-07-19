package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityActivity
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class MoreSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                startActivity(Intent(activity, CardPriorityActivity::class.java))
                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_fine_tune))?.let {
            it.fragment = FineTuneSettingsFragment::class.java.name
        }
    }

    override fun onResume() {
        super.onResume()

        // Since GameServer can be updated from other parts of code,
        // we need to trigger a forced UI update here
        findPreference<ListPreference>(getString(prefKeys.pref_gameserver))?.let {
            it.value = Preferences.gameServer.toString()
        }
    }
}
