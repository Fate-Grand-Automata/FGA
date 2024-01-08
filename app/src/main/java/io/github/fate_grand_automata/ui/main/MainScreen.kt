package io.github.fate_grand_automata.ui.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.fate_grand_automata.BuildConfig
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.accessibility.TapperService
import io.github.fate_grand_automata.runner.ScriptRunnerService
import io.github.fate_grand_automata.scripts.prefs.wantsMediaProjectionToken
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.HeadingButton
import io.github.fate_grand_automata.ui.OnResume
import io.github.fate_grand_automata.ui.StartMediaProjection
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.LanguagePref
import io.github.fate_grand_automata.ui.prefs.ListPreference
import io.github.fate_grand_automata.ui.prefs.Preference
import io.github.fate_grand_automata.util.OpenDocTreePersistable

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    vm: MainScreenViewModel = viewModel(),
    navigate: (MainScreenDestinations) -> Unit
) {
    var dirPicker: ActivityResultLauncher<Uri?>? by remember { mutableStateOf(null) }
    val permissionState = rememberPermissionState(permission = POST_NOTIFICATIONS)

    var toggling by rememberSaveable { mutableStateOf(false) }

    val accessibilityDisabledDialog = FgaDialog()
    accessibilityDisabledDialog.build {
        title(stringResource(R.string.accessibility_disabled_title))
        message(stringResource(R.string.accessibility_disabled_message))

        buttons(
            okLabel = stringResource(R.string.accessibility_disabled_go_to_settings),
            onSubmit = {
                navigate(MainScreenDestinations.AccessibilitySettings)
                toggling = true
            }
        )
    }

    val overlayDisabledDialog = FgaDialog()
    overlayDisabledDialog.build {
        title(stringResource(R.string.draw_overlay_disabled_title))
        message(stringResource(R.string.draw_overlay_disabled_message))

        buttons(
            okLabel = stringResource(R.string.accessibility_disabled_go_to_settings),
            onSubmit = {
                navigate(MainScreenDestinations.OverlaySettings)
                toggling = true
            }
        )
    }

    val context = LocalContext.current

    val startMediaProjection = rememberLauncherForActivityResult(StartMediaProjection()) {
        vm.onStartMediaProjectionResult(context, it)
    }

    val pickDirectory: () -> Unit = { dirPicker?.launch(Uri.EMPTY) }

    val requestNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !permissionState.status.isGranted
        ) {
            permissionState.launchPermissionRequest()
        }
    }

    fun toggleOverlayService() {
        toggling = false

        toggleOverlayService(
            context = context,
            vm = vm,
            pickDirectory = pickDirectory,
            requestNotifications = requestNotifications,
            showOverlayDisabled = { overlayDisabledDialog.show() },
            showAccessibilityDisabled = { accessibilityDisabledDialog.show() },
            startMediaProjection = { startMediaProjection.launch() }
        )
    }

    dirPicker = rememberLauncherForActivityResult(OpenDocTreePersistable()) {
        if (it != null) {
            vm.storageProvider.setRoot(it)

            toggleOverlayService()
        }
    }

    OnResume {
        if (toggling || vm.autoStartService) {
            toggleOverlayService()
        }
    }

    val overlayServiceStarted by ScriptRunnerService.serviceStarted
    val accessibilityServiceStarted by TapperService.serviceStarted

    MainScreenContent(
        navigate = {
            if (it is MainScreenDestinations.BattleConfigs) {
                if (vm.ensureRootDir(context, pickDirectory)) {
                    navigate(it)
                }
            } else navigate(it)
        },
        overlayServiceStarted = overlayServiceStarted,
        toggleOverlayService = { toggleOverlayService() },
        accessibilityServiceStarted = accessibilityServiceStarted,
        toggleAccessibilityService = {
            if (accessibilityServiceStarted) {
                TapperService.instance?.disableSelf()
            } else {
                navigate(MainScreenDestinations.AccessibilitySettings)
            }
        },
        languagePref = LanguagePref()
    )
}

private fun toggleOverlayService(
    context: Context,
    vm: MainScreenViewModel,
    pickDirectory: () -> Unit,
    requestNotifications: () -> Unit,
    showOverlayDisabled: () -> Unit,
    showAccessibilityDisabled: () -> Unit,
    startMediaProjection: () -> Unit
) {
    if (!Settings.canDrawOverlays(context)) {
        showOverlayDisabled()
        return
    }

    if (!TapperService.serviceStarted.value) {
        showAccessibilityDisabled()
        return
    }

    if (!vm.ensureRootDir(context, pickDirectory)) {
        return
    }

    requestNotifications()

    if (ScriptRunnerService.serviceStarted.value) {
        ScriptRunnerService.stopService(context)
    } else {
        ScriptRunnerService.startService(context)

        if (vm.prefs.wantsMediaProjectionToken) {
            if (ScriptRunnerService.mediaProjectionToken == null) {
                startMediaProjection()
            }
        }
    }
}

sealed class MainScreenDestinations {
    data object Releases : MainScreenDestinations()

    data object ReleaseNotes: MainScreenDestinations()
    data object TroubleshootingGuide : MainScreenDestinations()
    data object Discord : MainScreenDestinations()
    data object Donate : MainScreenDestinations()
    data object BattleConfigs : MainScreenDestinations()
    data object MoreOptions : MainScreenDestinations()
    data object AccessibilitySettings : MainScreenDestinations()
    data object OverlaySettings : MainScreenDestinations()
}

@Composable
private fun MainScreenContent(
    navigate: (MainScreenDestinations) -> Unit,
    overlayServiceStarted: Boolean,
    toggleOverlayService: () -> Unit,
    accessibilityServiceStarted: Boolean,
    toggleAccessibilityService: () -> Unit,
    languagePref: LanguagePref
) {
    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Heading(stringResource(R.string.app_name)) {
                    HeadingButton(
                        text = "Build: ${BuildConfig.VERSION_CODE}",
                        onClick = { navigate(MainScreenDestinations.Releases) }
                    )

                    HeadingButton(
                        text = stringResource(R.string.troubleshoot),
                        onClick = { navigate(MainScreenDestinations.TroubleshootingGuide) }
                    )

                    HeadingButton(
                        text = stringResource(R.string.discord),
                        icon = icon(R.drawable.ic_discord),
                        onClick = { navigate(MainScreenDestinations.Discord) }
                    )

                    HeadingButton(
                        text = stringResource(R.string.donate),
                        icon = icon(R.drawable.ic_donate),
                        onClick = { navigate(MainScreenDestinations.Donate) }
                    )

                    HeadingButton(
                        text = stringResource(id = R.string.release_notes),
                        onClick = { navigate(MainScreenDestinations.ReleaseNotes) }
                    )

                }

            }

            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Column {
                        Preference(
                            title = stringResource(R.string.p_battle_config),
                            summary = stringResource(R.string.p_battle_config_summary),
                            icon = icon(R.drawable.ic_formation),
                            onClick = { navigate(MainScreenDestinations.BattleConfigs) }
                        )

                        Divider()

                        languagePref.ListPreference(
                            title = stringResource(R.string.p_app_language),
                            icon = icon(Icons.Default.Language),
                            entries = LanguagePref.availableLanguages()
                        )

                        Divider()

                        Preference(
                            title = stringResource(R.string.p_more_options),
                            icon = icon(R.drawable.ic_dots_horizontal),
                            onClick = { navigate(MainScreenDestinations.MoreOptions) }
                        )
                    }
                }
            }

            item {
                AccessibilityServiceBlock(
                    serviceStarted = accessibilityServiceStarted,
                    toggleService = toggleAccessibilityService,
                    overlayServiceStarted = overlayServiceStarted
                )
            }
        }

        OverlayServiceToggleButton(
            serviceStarted = overlayServiceStarted,
            toggleService = toggleOverlayService,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun OverlayServiceToggleButton(
    serviceStarted: Boolean,
    toggleService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (serviceStarted)
            MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.secondary
    )

    val foregroundColor =
        if (serviceStarted)
            MaterialTheme.colorScheme.onError
        else MaterialTheme.colorScheme.onSecondary

    ExtendedFloatingActionButton(
        text = {
            Text(
                stringResource(if (serviceStarted) R.string.stop_service else R.string.start_service),
                color = foregroundColor
            )
        },
        onClick = toggleService,
        icon = {
            Icon(
                painterResource(if (serviceStarted) R.drawable.ic_close else R.drawable.ic_launch),
                contentDescription = "Toggle service",
                tint = foregroundColor
            )
        },
        containerColor = backgroundColor,
        modifier = modifier
            .padding(16.dp)
    )
}

@Composable
private fun AccessibilityServiceBlock(
    serviceStarted: Boolean,
    toggleService: () -> Unit,
    overlayServiceStarted: Boolean
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 5.dp)
        ) {
            Row {
                Text(stringResource(R.string.accessibility_service))

                Text(
                    stringResource(if (serviceStarted) R.string.accessibility_enabled else R.string.accessibility_disabled),
                    modifier = Modifier
                        .padding(start = 5.dp),
                    color = colorResource(if (serviceStarted) R.color.colorQuickResist else R.color.colorBusterResist),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            TextButton(
                onClick = toggleService,
                // Don't allow (at least from the UI) turning OFF accessibility service when overlay service is running
                enabled = !serviceStarted || !overlayServiceStarted
            ) {
                Text(
                    stringResource(if (serviceStarted) R.string.accessibility_disable else R.string.accessibility_enable_in_settings)
                )
            }
        }
    }
}