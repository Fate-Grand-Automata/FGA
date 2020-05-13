package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate
import com.mathewsachin.fategrandautomata.util.preferredSupportOnResume

const val AutoSkillItemKey = "AutoSkillItemKey"

class AutoSkillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val autoSkillItemKey = arguments?.getString(AutoSkillItemKey)
            ?: throw IllegalArgumentException("Arguments should not be null")

        preferenceManager.sharedPreferencesName = autoSkillItemKey

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        // Delete button
        findPreference<Preference>(getString(R.string.pref_autoskill_delete))?.let {
            it.setOnPreferenceClickListener {
                AlertDialog.Builder(requireActivity())
                    .setMessage("Are you sure you want to delete this configuration?")
                    .setTitle("Confirm Deletion")
                    .setPositiveButton("Delete") { _, _ -> deleteItem(autoSkillItemKey) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        preferredSupportOnResume()
    }

    private fun deleteItem(AutoSkillItemKey: String) {
        activity?.deleteSharedPreferences(AutoSkillItemKey)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        val autoSkillItemsKey = getString(R.string.pref_autoskill_list)
        val autoSkillItems = prefs.getStringSet(autoSkillItemsKey, mutableSetOf())!!
            .toSortedSet()
        autoSkillItems.remove(AutoSkillItemKey)

        prefs.edit(commit = true) {
            putStringSet(autoSkillItemsKey, autoSkillItems)
        }

        unselectItem(AutoSkillItemKey, prefs)

        // We opened a separate activity for AutoSkill item
        activity?.finish()
    }

    private fun unselectItem(AutoSkillItemKey: String, Prefs: SharedPreferences) {
        val selectedAutoSkillKey = getString(R.string.pref_autoskill_selected)
        val selectedAutoSkill = Prefs.getString(selectedAutoSkillKey, "")

        if (selectedAutoSkill == AutoSkillItemKey) {
            Prefs.edit(commit = true) { remove(selectedAutoSkillKey) }
        }
    }
}
