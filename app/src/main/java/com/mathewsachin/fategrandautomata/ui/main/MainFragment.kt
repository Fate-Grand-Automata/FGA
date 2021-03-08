package com.mathewsachin.fategrandautomata.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.accessibility.ServiceState
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.StartMediaProjection
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import timber.log.info
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    companion object {
        private var mediaProjectionToken: Intent? = null
    }

    val vm: MainSettingsViewModel by activityViewModels()

    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefs: IPreferences

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        serviceToggleBtnOnClick()
    }

    fun goToBattleConfigList() {
        val action = MainFragmentDirections
            .actionMainFragmentToBattleConfigListFragment()

        nav(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    Box {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Preference(
                                    title = stringResource(R.string.p_battle_config),
                                    summary = stringResource(R.string.p_battle_config_summary),
                                    icon = painterResource(R.drawable.ic_formation),
                                    onClick = {
                                        if (vm.ensureRootDir(pickDir, requireContext())) {
                                            goToBattleConfigList()
                                        }
                                    }
                                )
                            }

                            item {
                                Preference(
                                    title = stringResource(R.string.p_nav_troubleshoot),
                                    icon = painterResource(R.drawable.ic_troubleshooting),
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.link_troubleshoot))
                                        )

                                        startActivity(intent)
                                    }
                                )
                            }

                            item {
                                Preference(
                                    title = stringResource(R.string.p_more_options),
                                    icon = painterResource(R.drawable.ic_dots_horizontal),
                                    onClick = {
                                        val action = MainFragmentDirections
                                            .actionMainFragmentToMoreSettingsFragment()

                                        nav(action)
                                    }
                                )
                            }
                        }

                        val serviceStarted by vm.serviceStarted

                        ExtendedFloatingActionButton(
                            text = {
                                Text(
                                    stringResource(if (serviceStarted) R.string.stop_service else R.string.start_service),
                                    color = Color.White
                                )
                            },
                            onClick = { serviceToggleBtnOnClick() },
                            icon = {
                                Icon(
                                    painterResource(if (serviceStarted) R.drawable.ic_close else R.drawable.ic_launch),
                                    contentDescription = "Toggle service",
                                    tint = Color.White
                                )
                            },
                            backgroundColor = colorResource(if (serviceStarted) R.color.colorStopService else R.color.colorPrimary),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(32.dp)
                        )
                    }
                }
            }
        }

    private fun startWithMediaProjection() {
        mediaProjectionToken.let {
            if (it != null) {
                // Cloning the Intent allows reuse.
                // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
                ScriptRunnerService.startService(it.clone() as Intent)
            } else startMediaProjection.launch()
        }
    }

    val startMediaProjection = registerForActivityResult(StartMediaProjection()) { intent ->
        if (intent == null) {
            Timber.info { "MediaProjection cancelled by user" }
            ScriptRunnerService.Instance?.notification?.hide()
        } else {
            mediaProjectionToken = intent
            startWithMediaProjection()
        }
    }

    private fun checkAccessibilityService(): Boolean {
        if (ScriptRunnerService.isAccessibilityServiceRunning())
            return true

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.accessibility_disabled_title)
            .setMessage(R.string.accessibility_disabled_message)
            .setPositiveButton(R.string.accessibility_disabled_go_to_settings) { _, _ ->
                // Open Accessibility Settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                toggling = true
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        return false
    }

    private fun checkCanUseOverlays(): Boolean {
        val context = requireContext()

        if (!Settings.canDrawOverlays(context)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.draw_overlay_disabled_title)
                .setMessage(R.string.draw_overlay_disabled_message)
                .setPositiveButton(R.string.draw_overlay_disabled_go_to_settings) { _, _ ->
                    // Open overlay settings
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    toggling = true
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return false
        }

        return true
    }

    private var toggling = false

    private fun serviceToggleBtnOnClick() {
        toggling = false

        if (!checkCanUseOverlays()
            || !checkAccessibilityService()
        )
            return

        if (!vm.ensureRootDir(pickDir, requireContext())) {
            return
        }

        val instance = ScriptRunnerService.Instance
            ?: return

        when (instance.serviceState) {
            is ServiceState.Started -> ScriptRunnerService.stopService()
            is ServiceState.Stopped -> {
                if (instance.wantsMediaProjectionToken) {
                    instance.notification.show()

                    startWithMediaProjection()
                } else if (ScriptRunnerService.startService()) {
                    instance.notification.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (toggling || (vm.autoStartService && !ScriptRunnerService.isServiceStarted())) {
            serviceToggleBtnOnClick()
        }
    }
}
