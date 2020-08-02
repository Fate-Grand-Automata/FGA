package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.ui.UpdateCheckViewModel
import com.mathewsachin.fategrandautomata.util.UpdateCheckResult
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.coroutines.launch
import mu.KotlinLogging
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

private val logger = KotlinLogging.logger {}

class MainSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
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

    override fun onResume() {
        super.onResume()

        val updateCheckViewModel: UpdateCheckViewModel by activityViewModels()

        lifecycleScope.launch {
            checkForUpdates(updateCheckViewModel)
        }

        findPreference<Preference>(getString(prefKeys.pref_nav_refill))?.let {
            val prefs = preferences.refill
            it.summary = when (prefs.enabled) {
                true -> "${prefs.resource} x${prefs.repetitions}"
                false -> "OFF"
            }
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
