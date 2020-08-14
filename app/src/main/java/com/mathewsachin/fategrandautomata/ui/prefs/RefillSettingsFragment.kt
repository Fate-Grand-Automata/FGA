package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.appComponent
import com.mathewsachin.fategrandautomata.util.makeNumeric
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class RefillSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(prefKeys.pref_refill_repetitions))?.makeNumeric()
    }

    override fun onResume() {
        super.onResume()

        findPreference<EditTextPreference>(getString(R.string.pref_refill_repetitions))?.let {
            it.text = preferences.refill.repetitions.toString()
        }
    }
}
