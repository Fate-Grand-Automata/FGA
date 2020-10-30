package com.mathewsachin.fategrandautomata.ui.prefs

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class MainSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var storageProvider: StorageProvider

    val vm: MainSettingsViewModel by activityViewModels()

    fun goToBattleConfigList() {
        val action = MainFragmentDirections
            .actionMainFragmentToBattleConfigListFragment()

        nav(action)
    }

    private val pickDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUrl ->
        if (dirUrl != null) {
            storageProvider.setRoot(dirUrl)

            goToBattleConfigList()
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
                if (!vm.checkRootDir()) {
                    pickDir.launch(Uri.EMPTY)
                } else goToBattleConfigList()

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
