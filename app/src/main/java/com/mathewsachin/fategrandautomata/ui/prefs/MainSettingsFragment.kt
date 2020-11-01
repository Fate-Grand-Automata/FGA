package com.mathewsachin.fategrandautomata.ui.prefs

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class MainSettingsFragment : PreferenceFragmentCompat() {
    val goToBattleConfigList = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { m -> m }) {
            val action = MainFragmentDirections
                .actionMainFragmentToBattleConfigListFragment()

            nav(action)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            it.setOnPreferenceClickListener {
                val action = MainFragmentDirections
                    .actionMainFragmentToRefillSettingsFragment()

                nav(action)

                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_battle_config))?.let {
            it.setOnPreferenceClickListener {
                val permissions = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                goToBattleConfigList.launch(permissions)

                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_more))?.let {
            it.setOnPreferenceClickListener {
                val action = MainFragmentDirections
                    .actionMainFragmentToMoreSettingsFragment()

                nav(action)

                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            vm.refillMessage.observe(viewLifecycleOwner) { msg ->
                it.summary = msg
            }
        }

        findPreference<ListPreference>(getString(R.string.pref_script_mode))?.let {
            vm.scriptMode.observe(viewLifecycleOwner) { mode ->
                it.value = mode.toString()
            }
        }
    }
}
