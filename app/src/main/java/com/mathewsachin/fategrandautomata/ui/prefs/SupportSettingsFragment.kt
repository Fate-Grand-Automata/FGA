package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.extractSupportImgs
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate
import com.mathewsachin.fategrandautomata.util.preferredSupportOnResume
import kotlinx.coroutines.*

class SupportSettingsFragment : PreferenceFragmentCompat() {
    val scope = MainScope()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.support_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<Preference>(getString(R.string.pref_extract_def_support_imgs))?.let {
            it.setOnPreferenceClickListener {
                scope.launch {
                    extractSupportImgs()
                    Toast.makeText(activity, "Support Images Extracted Successfully", Toast.LENGTH_SHORT).show()
                    preferredSupportOnResume()
                }
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        preferredSupportOnResume()
    }
}
