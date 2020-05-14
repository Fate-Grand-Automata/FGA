package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate

class AutoSkillSettingsFragment : SupportSettingsBaseFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val autoSkillItemKey = arguments?.getString(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Arguments should not be null")

        preferenceManager.sharedPreferencesName = autoSkillItemKey

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()
    }
}
