package com.mathewsachin.fategrandautomata.ui.prefs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.ui.prefs.compose.*
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreSettingsFragment : Fragment() {
    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefs: PrefsCore

    private val storageSummary: MutableState<String?> = mutableStateOf(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    ScrollableColumn {
                        BattleGroup(prefs)
                        WaitForAPRegenGroup(prefs)

                        StorageGroup(
                            directoryName = storageSummary.value ?: "",
                            onPickDirectory = { pickDir.launch(Uri.EMPTY) }
                        )

                        AdvancedGroup(
                            prefs,
                            goToFineTune = {
                                val action = MoreSettingsFragmentDirections
                                    .actionMoreSettingsFragmentToFineTuneSettingsFragment()

                                nav(action)
                            }
                        )
                    }
                }
            }
        }.also {
            storageSummary.value = storageProvider.rootDirName
        }

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        storageSummary.value = storageProvider.rootDirName
    }
}

@Composable
val Int.boostItemString get() = when (this) {
    -1 -> stringResource(R.string.p_boost_item_disabled)
    0 -> stringResource(R.string.p_boost_item_skip)
    else -> stringResource(R.string.p_boost_item_number, this)
}

val GameServerEnum.displayStringRes
    get() = when (this) {
        GameServerEnum.En -> R.string.game_server_na
        GameServerEnum.Jp -> R.string.game_server_jp
        GameServerEnum.Cn -> R.string.game_server_cn
        GameServerEnum.Tw -> R.string.game_server_tw
        GameServerEnum.Kr -> R.string.game_server_kr
    }

@Composable
fun BattleGroup(
    prefs: PrefsCore
) {
    PreferenceGroup(title = stringResource(R.string.p_script_mode_battle)) {
        prefs.skillConfirmation.SwitchPreference(
            title = stringResource(R.string.p_skill_confirmation),
            icon = vectorResource(R.drawable.ic_radio)
        )

        prefs.gameServerRaw.ListPreference(
            title = stringResource(R.string.p_game_server),
            icon = vectorResource(R.drawable.ic_earth),
            entries =
            mapOf(PrefsCore.GameServerAutoDetect to stringResource(R.string.p_game_server_auto_detect))
                .plus(
                    GameServerEnum.values().associate {
                        it.name to stringResource(it.displayStringRes)
                    }
                )
        )

        prefs.storySkip.SwitchPreference(
            title = stringResource(R.string.p_story_skip),
            icon = vectorResource(R.drawable.ic_fast_forward)
        )

        prefs.withdrawEnabled.SwitchPreference(
            title = stringResource(R.string.p_enable_withdraw),
            icon = vectorResource(R.drawable.ic_exit_run)
        )

        prefs.stopOnCEDrop.SwitchPreference(
            title = stringResource(R.string.p_stop_on_ce_drop),
            icon = vectorResource(R.drawable.ic_card)
        )

        prefs.stopOnCEGet.SwitchPreference(
            title = stringResource(R.string.p_stop_on_ce_get),
            summary = stringResource(R.string.p_stop_on_ce_get_summary),
            icon = vectorResource(R.drawable.ic_card)
        )

        prefs.screenshotDrops.SwitchPreference(
            title = stringResource(R.string.p_screenshot_drops),
            summary = stringResource(R.string.p_screenshot_drops_summary),
            icon = vectorResource(R.drawable.ic_screenshot)
        )

        prefs.boostItemSelectionMode.ListPreference(
            title = stringResource(R.string.p_boost_item),
            icon = vectorResource(R.drawable.ic_boost),
            entries = (-1..3).associateWith { it.boostItemString }
        )
    }
}

@Composable
fun WaitForAPRegenGroup(
    prefs: PrefsCore
) {
    PreferenceGroup(title = stringResource(R.string.p_wait_ap_regen_text)) {
        val waitEnabled by prefs.waitAPRegen.collect()

        Row {
            Checkbox(
                checked = waitEnabled,
                onCheckedChange = { prefs.waitAPRegen.set(it) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
            )

            Preference(
                title = stringResource(R.string.p_wait_ap_regen_text),
                hint = stringResource(R.string.p_wait_ap_regen_text_summary),
                enabled = waitEnabled
            )
        }

        if (waitEnabled) {
            prefs.waitAPRegenMinutes.SeekBarPreference(
                title = stringResource(R.string.p_wait_ap_regen_minutes_text),
                summary = stringResource(R.string.p_wait_ap_regen_minutes_text_summary),
                valueRange = 1..60,
                icon = vectorResource(R.drawable.ic_counter),
                valueRepresentation = { "$it min" }
            )
        }
    }
}

@Composable
fun StorageGroup(
    directoryName: String,
    onPickDirectory: () -> Unit
) {
    PreferenceGroup(title = stringResource(R.string.p_storage)) {
        Preference(
            title = stringResource(R.string.p_folder),
            summary = directoryName,
            icon = vectorResource(R.drawable.ic_folder_edit),
            onClick = onPickDirectory
        )
    }
}

@Composable
fun AdvancedGroup(
    prefs: PrefsCore,
    goToFineTune: () -> Unit
) {
    PreferenceGroup(title = stringResource(R.string.p_advanced)) {
        Preference(
            title = stringResource(R.string.p_fine_tune),
            icon = vectorResource(R.drawable.ic_tune),
            onClick = goToFineTune
        )

        prefs.debugMode.SwitchPreference(
            title = stringResource(R.string.p_debug_mode),
            summary = stringResource(R.string.p_debug_mode_summary),
            icon = vectorResource(R.drawable.ic_bug)
        )

        prefs.ignoreNotchCalculation.SwitchPreference(
            title = stringResource(R.string.p_ignore_notch),
            summary = stringResource(R.string.p_ignore_notch_summary),
            icon = vectorResource(R.drawable.ic_notch)
        )

        val rootForScreenshots by prefs.useRootForScreenshots.collect()

        prefs.recordScreen.SwitchPreference(
            title = stringResource(R.string.p_record_screen),
            summary = stringResource(R.string.p_record_screen_summary),
            icon = vectorResource(R.drawable.ic_video),
            enabled = !rootForScreenshots
        )

        prefs.useRootForScreenshots.SwitchPreference(
            title = stringResource(R.string.p_root_screenshot),
            summary = stringResource(R.string.p_root_screenshot_summary),
            icon = vectorResource(R.drawable.ic_key)
        )

        prefs.autoStartService.SwitchPreference(
            title = stringResource(R.string.p_auto_start_service),
            icon = vectorResource(R.drawable.ic_launch)
        )
    }
}