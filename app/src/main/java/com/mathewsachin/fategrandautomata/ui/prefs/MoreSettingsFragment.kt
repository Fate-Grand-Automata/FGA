package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.appComponent
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class MoreSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        findPreference<Preference>(getString(prefKeys.pref_nav_fine_tune))?.let {
            it.setOnPreferenceClickListener {
                val action = MoreSettingsFragmentDirections
                    .actionMoreSettingsFragmentToFineTuneSettingsFragment()

                findNavController().navigate(action)

                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels { viewModelFactory }

        // Since GameServer can be updated from other parts of code,
        // we need to trigger a forced UI update here
        findPreference<ListPreference>(getString(prefKeys.pref_gameserver))?.let {
            vm.gameServer.observe(viewLifecycleOwner) { gameServer ->
                it.value = gameServer.toString()
            }
        }
    }
}
