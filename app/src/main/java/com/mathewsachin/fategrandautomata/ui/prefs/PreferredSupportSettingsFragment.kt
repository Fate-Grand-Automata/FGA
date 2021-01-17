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
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
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

    @Inject
    lateinit var prefsCore: PrefsCore

    private lateinit var servantList: MultiSelectListPreference
    private lateinit var ceList: MultiSelectListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key

        val config = prefsCore.forBattleConfig(args.key).support

        prefScreen {
            config.friendsOnly.switch {
                title = R.string.p_battle_config_support_friends_only
                icon = R.drawable.ic_friend
            }

            category {
                key = "servant_category"
                title = R.string.p_battle_config_support_pref_servants

                servantList = config.preferredServants.multiSelect {
                    title = R.string.p_battle_config_support_pref_servants
                    icon = R.drawable.ic_crown
                }.also {
                    it.summaryProvider = SupportMultiSelectSummaryProvider()
                }

                config.maxAscended.switch {
                    title = R.string.p_battle_config_support_max_ascended
                    icon = R.drawable.ic_star
                }

                config.skill1Max.switch {
                    title = R.string.p_max_skill_1
                    icon = R.drawable.ic_wand
                }

                config.skill2Max.switch {
                    title = R.string.p_max_skill_2
                    icon = R.drawable.ic_wand
                }

                config.skill3Max.switch {
                    title = R.string.p_max_skill_3
                    icon = R.drawable.ic_wand
                }
            }

            category {
                key = "ce_category"
                title = R.string.p_battle_config_support_pref_ces

                ceList = config.preferredCEs.multiSelect {
                    title = R.string.p_battle_config_support_pref_ces
                    icon = R.drawable.ic_card
                }.also {
                    it.summaryProvider = SupportMultiSelectSummaryProvider()
                }

                config.mlb.switch {
                    title = R.string.p_battle_config_support_mlb
                    icon = R.drawable.ic_star
                }
            }
        }
    }

    private suspend fun populatedServantAndCE() {
        servantList.apply {
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

        ceList.apply {
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
            @Suppress("DEPRECATION")
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        }

        when (preference.key) {
            servantList.key, ceList.key -> {
                ClearMultiSelectListPreferenceDialog().apply {
                    setKey(preference.key)
                    prepare(this)
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}
