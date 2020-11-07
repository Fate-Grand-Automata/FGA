package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.util.SupportMultiSelectSummaryProvider
import com.mathewsachin.fategrandautomata.util.populateFriendOrCe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@AndroidEntryPoint
class PreferredSupportSettingsFragment : PreferenceFragmentCompat() {
    val args: PreferredSupportSettingsFragmentArgs by navArgs()

    @Inject
    lateinit var storageProvider: IStorageProvider

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

    private suspend fun populatedServantAndCE() {
        val servants = findServantList() ?: return
        val ces = findCeList() ?: return

        servants.apply {
            val entries = try {
                withContext(Dispatchers.IO) {
                    storageProvider.list(SupportImageKind.Servant)
                        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
                        .toTypedArray()
                }
            } catch (e: Exception) {
                val msg = "Couldn't access Support images"

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Timber.error(e) { msg }

                emptyArray()
            }

            this.entryValues = entries
            this.entries = entries
        }

        ces.apply {
            populateFriendOrCe(storageProvider, SupportImageKind.CE)
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            populatedServantAndCE()
        }
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
