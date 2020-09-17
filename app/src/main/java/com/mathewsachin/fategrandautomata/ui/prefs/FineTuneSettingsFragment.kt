package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.mathewsachin.fategrandautomata.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FineTuneSettingsFragment : PreferenceFragmentCompat() {
    val vm: FineTuneSettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fine_tune_preferences, rootKey)

        setHasOptionsMenu(true)

        for (pref in vm.fineTunePrefs) {
            findPreference<Preference>(pref.key)?.let {
                it.setDefaultValue(pref.defaultValue)
                it.summary = getString(R.string.p_fine_tune_default, pref.defaultValue)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for ((key, liveData) in vm.liveDataMap) {
            findPreference<SeekBarPreference>(key)?.let {
                liveData.observe(viewLifecycleOwner) { value ->
                    it.value = value
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fine_tune_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset_fine_tune -> {
                vm.resetAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
