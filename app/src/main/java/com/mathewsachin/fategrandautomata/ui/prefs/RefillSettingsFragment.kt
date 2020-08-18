package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.appComponent
import com.mathewsachin.fategrandautomata.util.makeNumeric
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class RefillSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(prefKeys.pref_refill_repetitions))?.makeNumeric()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels { viewModelFactory }

        findPreference<EditTextPreference>(getString(R.string.pref_refill_repetitions))?.let {
            vm.refillRepetitions.observe(viewLifecycleOwner) { repetitions ->
                it.text = repetitions.toString()
            }
        }
    }
}
