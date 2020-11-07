package com.mathewsachin.fategrandautomata.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.accessibility.ServiceState
import com.mathewsachin.fategrandautomata.databinding.ContentMainBinding
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsViewModel
import com.mathewsachin.fategrandautomata.util.StorageProvider
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

    private val pickDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUrl ->
        if (dirUrl != null) {
            storageProvider.setRoot(dirUrl)

            serviceToggleBtnOnClick()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ContentMainBinding.inflate(inflater)
            .also {
                it.vm = vm
                it.lifecycleOwner = viewLifecycleOwner

                it.serviceToggleBtn.setOnClickListener { serviceToggleBtnOnClick() }
            }
            .root

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
