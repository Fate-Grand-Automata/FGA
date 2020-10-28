package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.util.initWith
import com.mathewsachin.fategrandautomata.util.makeNumeric
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class RefillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(prefKeys.pref_refill_repetitions))?.makeNumeric()
        findPreference<EditTextPreference>(getString(R.string.pref_limit_runs))?.makeNumeric()
        findPreference<EditTextPreference>(getString(R.string.pref_limit_mats))?.makeNumeric()

        findPreference<MultiSelectListPreference>(getString(R.string.pref_refill_resource))
            ?.initWith<RefillResourceEnum> { it.stringRes }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        findPreference<EditTextPreference>(getString(R.string.pref_refill_repetitions))?.let {
            vm.refillRepetitions.observe(viewLifecycleOwner) { repetitions ->
                it.text = repetitions.toString()
            }
        }

        findPreference<MultiSelectListPreference>(getString(R.string.pref_refill_resource))?.let {
            vm.refillResources.observe(viewLifecycleOwner) { refillResourcesMsg ->
                it.summary = refillResourcesMsg
            }
        }
    }
}
