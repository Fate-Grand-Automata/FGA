package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.util.SupportMultiSelectSummaryProvider
import com.mathewsachin.fategrandautomata.util.populateFriendOrCe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PreferredSupportSettingsFragment : PreferenceFragmentCompat() {
    val args: PreferredSupportSettingsFragmentArgs by navArgs()

    @Inject
    lateinit var storageDirs: StorageDirs

    private fun findServantList(): MultiSelectListPreference? =
        findPreference(getString(R.string.pref_support_pref_servant))

    private fun findCeList(): MultiSelectListPreference? =
        findPreference(getString(R.string.pref_support_pref_ce))

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key

        setPreferencesFromResource(R.xml.preferred_support_preferences, rootKey)

        findServantList()?.summaryProvider = SupportMultiSelectSummaryProvider()
        findCeList()?.summaryProvider = SupportMultiSelectSummaryProvider()
    }

    private fun populatedServantAndCE() {
        val servants = findServantList() ?: return
        val ces = findCeList() ?: return

        servants.apply {
            val entries = (storageDirs.supportServantImgFolder.listFiles() ?: emptyArray())
                .map { it.name }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
                .toTypedArray()

            this.entryValues = entries
            this.entries = entries
        }

        ces.apply {
            populateFriendOrCe(storageDirs.supportCeFolder)
        }
    }

    override fun onResume() {
        super.onResume()

        populatedServantAndCE()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        fun prepare(dialogFragment: PreferenceDialogFragmentCompat) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        }

        when (preference.key) {
            getString(R.string.pref_support_pref_ce),
            getString(R.string.pref_support_pref_servant) -> {
                ClearMultiSelectListPreferenceDialog().apply {
                    setKey(preference.key)
                    prepare(this)
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}
