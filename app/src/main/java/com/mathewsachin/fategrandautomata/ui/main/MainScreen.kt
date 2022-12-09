package com.mathewsachin.fategrandautomata.ui.main

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.BuildConfig
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.TapperService
import com.mathewsachin.fategrandautomata.runner.ScriptRunnerService
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.util.OpenDocTreePersistable

@Composable
fun MainScreen(
    vm: MainScreenViewModel = viewModel(),
    navigate: (MainScreenDestinations) -> Unit
) {
    var dirPicker: ActivityResultLauncher<Uri?>? by remember { mutableStateOf(null) }

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

    fun toggleOverlayService() {
        toggling = false

        toggleOverlayService(
            context = context,
            vm = vm,
            pickDirectory = pickDirectory,
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
        }
    )
}

private fun toggleOverlayService(
    context: Context,
    vm: MainScreenViewModel,
    pickDirectory: () -> Unit,
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
    object Releases : MainScreenDestinations()
    object TroubleshootingGuide : MainScreenDestinations()
    object BattleConfigs : MainScreenDestinations()
    object MoreOptions : MainScreenDestinations()
    object AccessibilitySettings : MainScreenDestinations()
    object OverlaySettings : MainScreenDestinations()
}

@Composable
private fun MainScreenContent(
    navigate: (MainScreenDestinations) -> Unit,
    overlayServiceStarted: Boolean,
    toggleOverlayService: () -> Unit,
    accessibilityServiceStarted: Boolean,
    toggleAccessibilityService: () -> Unit
) {
    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Heading(stringResource(R.string.app_name)) {
                    item {
                        HeadingButton(
                            text = "Build: ${BuildConfig.VERSION_CODE}",
                            onClick = { navigate(MainScreenDestinations.Releases) }
                        )
                    }

                    item {
                        HeadingButton(
                            text = stringResource(R.string.troubleshoot),
                            onClick = { navigate(MainScreenDestinations.TroubleshootingGuide) }
                        )
                    }
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