package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate
import com.mathewsachin.fategrandautomata.util.preferredSupportOnResume

class AutoSkillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val autoSkillItemKey = arguments?.getString(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Arguments should not be null")

        preferenceManager.sharedPreferencesName = autoSkillItemKey

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()
    }

    override fun onResume() {
        super.onResume()

        preferredSupportOnResume()
    }
}
