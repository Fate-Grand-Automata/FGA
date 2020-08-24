package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.ui.UpdateCheckViewModel
import com.mathewsachin.fategrandautomata.util.UpdateCheckResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mu.KotlinLogging
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

private val logger = KotlinLogging.logger {}

@AndroidEntryPoint
class MainSettingsFragment : PreferenceFragmentCompat() {
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
                val action = MainFragmentDirections
                    .actionMainFragmentToAutoSkillListFragment()

                findNavController().navigate(action)

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

    override fun onResume() {
        super.onResume()

        val updateCheckViewModel: UpdateCheckViewModel by activityViewModels()

        lifecycleScope.launch {
            checkForUpdates(updateCheckViewModel)
        }
    }


    suspend fun checkForUpdates(updateCheckViewModel: UpdateCheckViewModel) {
        when (val result = updateCheckViewModel.check()) {
            is UpdateCheckResult.Available -> {
                findPreference<Preference>(getString(R.string.pref_nav_update))?.let {
                    it.isVisible = true
                    it.summary = result.version
                }
            }
            is UpdateCheckResult.Failed -> {
                logger.error(
                    "Update check failed",
                    result.e
                )
            }
        }
    }
}
