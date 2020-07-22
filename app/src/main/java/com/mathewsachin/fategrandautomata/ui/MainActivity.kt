package com.mathewsachin.fategrandautomata.ui

import android.Manifest
import android.content.Intent
import android.icu.util.VersionInfo
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsFragment
import com.mathewsachin.fategrandautomata.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject
    lateinit var cutoutManager: CutoutManager

    private val requestMediaProjection = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appComponent.inject(this)

        setSupportActionBar(toolbar)

        service_toggle_btn.setOnClickListener { serviceToggleBtnOnClick() }

        // Only once
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_pref_frame, MainSettingsFragment())
                .commit()

            checkPermissions()
            ignoreBatteryOptimizations()
        }

        GlobalScope.launch {
            try {
                checkForUpdates()
            } catch (e: Exception) {
                Log.e(::checkForUpdates.name, "Update check failed", e)
            }
        }
    }

    suspend fun checkForUpdates() {
        if (isDevelopmentBuild) {
            return
        }

        val latestTag = getLatestReleaseTag()

        val latestVersion = VersionInfo
            .getInstance(latestTag.substring(1))

        if (latestVersion > currentVersion) {
            val parentView = findViewById<View>(android.R.id.content)

            Snackbar
                .make(parentView, "Update available: $latestTag", Snackbar.LENGTH_INDEFINITE)
                .setAction("WEBSITE") { _ ->
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(websiteLink)
                    )
                    startActivity(intent)
                }
                .show()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        cutoutManager.applyCutout(this)
    }

    private fun ignoreBatteryOptimizations() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            return
        }

        startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName")
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestMediaProjection) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                ScriptRunnerService.Instance?.notification?.hide()
                return
            }

            ScriptRunnerService.Instance?.start(data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkPermissions() {
        val permissionsToCheck = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissionsToCheck
            .filter {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PermissionChecker.PERMISSION_GRANTED
            }
            .toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 0)
        }
    }

    private fun checkAccessibilityService(): Boolean {
        if (ScriptRunnerService.Instance != null)
            return true

        AlertDialog.Builder(this)
            .setTitle("Accessibility Disabled")
            .setMessage("Turn on accessibility for this app from System settings. If it is already On, turn it OFF and start again.")
            .setPositiveButton("Go To Settings") { _, _ ->
                // Open Accessibility Settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()

        return false
    }

    private fun checkCanUseOverlays(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            val i = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(i)
            return false
        }

        return true
    }

    private fun serviceToggleBtnOnClick() {
        if (!checkAccessibilityService())
            return

        if (!checkCanUseOverlays())
            return

        val instance = ScriptRunnerService.Instance
            ?: return

        if (instance.serviceStarted) {
            instance.stop()
        } else {
            if (instance.wantsMediaProjectionToken) {
                instance.notification.show()

                // This initiates a prompt dialog for the user to confirm screen projection.
                startActivityForResult(
                    instance.mediaProjectionManager.createScreenCaptureIntent(),
                    requestMediaProjection
                )
            } else if (instance.start()) {
                instance.notification.show()
            }
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager
            .fragmentFactory
            .instantiate(classLoader, pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)

        supportFragmentManager
            .beginTransaction()
            /*.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)*/
            .replace(R.id.main_pref_frame, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }
}
