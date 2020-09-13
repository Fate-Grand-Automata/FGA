package com.mathewsachin.fategrandautomata.ui.prefs

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import mu.KotlinLogging
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

private val logger = KotlinLogging.logger {}

@AndroidEntryPoint
class MainSettingsFragment : PreferenceFragmentCompat() {
    val goToAutoSkill = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { m -> m }) {
            val action = MainFragmentDirections
                .actionMainFragmentToAutoSkillListFragment()

            findNavController().navigate(action)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            it.setOnPreferenceClickListener {
                val action = MainFragmentDirections
                    .actionMainFragmentToRefillSettingsFragment()

                findNavController().navigate(action)

                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_auto_skill))?.let {
            it.setOnPreferenceClickListener {
                val permissions = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                goToAutoSkill.launch(permissions)

                true
            }
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_more))?.let {
            it.setOnPreferenceClickListener {
                val action = MainFragmentDirections
                    .actionMainFragmentToMoreSettingsFragment()

                findNavController().navigate(action)

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
    }
}
