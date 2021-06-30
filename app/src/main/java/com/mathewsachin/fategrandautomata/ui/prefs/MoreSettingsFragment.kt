package com.mathewsachin.fategrandautomata.ui.prefs

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefs: PrefsCore

    private lateinit var navStorage: Preference
    private lateinit var recordScreen: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefScreen {
            category {
                key = "battle_category"
                title = R.string.p_script_mode_battle

                prefs.skillConfirmation.switch {
                    title = R.string.p_skill_confirmation
                    icon = R.drawable.ic_radio
                }

                prefs.gameServerRaw.list {
                    title = R.string.p_game_server
                    icon = R.drawable.ic_earth
                }.apply {
                    entries =
                        (listOf(R.string.p_game_server_auto_detect) + enumValues<GameServerEnum>().map { it.displayStringRes })
                            .map { getString(it) }
                            .toTypedArray()

                    entryValues =
                        (listOf(PrefsCore.GameServerAutoDetect) + enumValues<GameServerEnum>().map { it.name })
                            .toTypedArray()
                }

                prefs.storySkip.switch {
                    title = R.string.p_story_skip
                    icon = R.drawable.ic_fast_forward
                }

                prefs.withdrawEnabled.switch {
                    title = R.string.p_enable_withdraw
                    icon = R.drawable.ic_exit_run
                }

                prefs.stopOnCEDrop.switch {
                    title = R.string.p_stop_on_ce_drop
                    summary = R.string.p_experimental
                    icon = R.drawable.ic_card
                }

                prefs.stopOnCEGet.switch {
                    title = R.string.p_stop_on_ce_get
                    summary = R.string.p_stop_on_ce_get_summary
                    icon = R.drawable.ic_card
                }

                prefs.screenshotDrops.switch {
                    title = R.string.p_screenshot_drops
                    summary = R.string.p_screenshot_drops_summary
                    icon = R.drawable.ic_screenshot
                }

                prefs.boostItemSelectionMode.list {
                    title = R.string.p_boost_item
                    icon = R.drawable.ic_boost
                }.apply {
                    entries = listOf(R.string.p_boost_item_disabled, R.string.p_boost_item_skip)
                        .map { getString(it) }
                        .toTypedArray() +
                            (1..3).map {
                                context.getString(R.string.p_boost_item_number, it)
                            }

                    entryValues = (-1..3)
                        .map { it.toString() }
                        .toTypedArray()
                }

                prefs.showTextBoxForSkillCmd.switch {
                    title = R.string.p_battle_config_cmd_text
                    summary = R.string.p_battle_config_cmd_text_summary
                    icon = R.drawable.ic_wand
                }
            }

            category {
                key = "wait_ap_regen_category"
                title = R.string.p_wait_ap_regen_text

                prefs.waitAPRegen.switch {
                    title = R.string.p_wait_ap_regen_text
                    summary = R.string.p_wait_ap_regen_text_summary
                    icon = R.drawable.ic_time
                }

                prefs.waitAPRegenMinutes.seekBar {
                    dependency = prefs.waitAPRegen
                    title = R.string.p_wait_ap_regen_minutes_text
                    summary = R.string.p_wait_ap_regen_minutes_text_summary
                    min = 1
                    max = 60
                    icon = R.drawable.ic_counter
                }
            }

            category {
                key = "storage_category"
                title = R.string.p_storage

                navStorage = blank {
                    title = R.string.p_folder
                    icon = R.drawable.ic_folder_edit
                }.also {
                    it.setOnPreferenceClickListener {
                        pickDir.launch(Uri.EMPTY)

                        true
                    }

                    it.summary = storageProvider.rootDirName
                }
            }

            category {
                key = "advanced_category"
                title = R.string.p_advanced

                blank {
                    title = R.string.p_fine_tune
                    icon = R.drawable.ic_tune
                }.setOnPreferenceClickListener {
                    val action = MoreSettingsFragmentDirections
                        .actionMoreSettingsFragmentToFineTuneSettingsFragment()

                    nav(action)

                    true
                }

                prefs.debugMode.switch {
                    title = R.string.p_debug_mode
                    summary = R.string.p_debug_mode_summary
                    icon = R.drawable.ic_bug
                }

                prefs.ignoreNotchCalculation.switch {
                    title = R.string.p_ignore_notch
                    summary = R.string.p_ignore_notch_summary
                    icon = R.drawable.ic_notch
                }

                recordScreen = prefs.recordScreen.switch {
                    title = R.string.p_record_screen
                    summary = R.string.p_record_screen_summary
                    icon = R.drawable.ic_video
                }

                prefs.useRootForScreenshots.switch {
                    title = R.string.p_root_screenshot
                    summary = R.string.p_root_screenshot_summary
                    icon = R.drawable.ic_key
                }

                prefs.autoStartService.switch {
                    title = R.string.p_auto_start_service
                    icon = R.drawable.ic_launch
                }
            }
        }
    }

    val GameServerEnum.displayStringRes
        get() = when (this) {
            GameServerEnum.En -> R.string.game_server_na
            GameServerEnum.Jp -> R.string.game_server_jp
            GameServerEnum.Cn -> R.string.game_server_cn
            GameServerEnum.Tw -> R.string.game_server_tw
            GameServerEnum.Kr -> R.string.game_server_kr
        }

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        navStorage.summary = storageProvider.rootDirName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        vm.useRootForScreenshots.observe(viewLifecycleOwner) { root ->
            recordScreen.isEnabled = !root

            if (root) {
                recordScreen.isChecked = false
            }
        }
    }
}
