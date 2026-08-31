package io.github.fate_grand_automata.ui.main

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.ui.pref_support.SupportViewModel
import io.github.fate_grand_automata.util.CutoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    @Inject
    lateinit var prefsCore: PrefsCore

    val vm: MainScreenViewModel by viewModels()
    val supportVm: SupportViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateRequestCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForAppUpdates()

        setContent {
            FgaApp(
                vm = vm,
                supportVm = supportVm
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        cutoutManager.applyCutout(this)
    }

    override fun onStart() {
        super.onStart()
        vm.activityStarted()
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            val msg = applicationContext.getString(R.string.update_download_success)
            Toast.makeText(
                applicationContext,
                msg,
                Toast.LENGTH_LONG
            ).show()
            lifecycleScope.launch {
                delay(5.seconds)
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun checkForAppUpdates() {
        if (!prefsCore.checkForUpdates.get()) {
            Timber.d("Update check disabled by the user")
            return
        }

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            if (isUpdateAvailable && info.isFlexibleUpdateAllowed) {
                Timber.i("New version is available: ${info.availableVersionCode()}")
                startUpdateFlow(info)
            } else {
                Timber.d("No update available")
            }
        }
    }

    /**
     * The [android.app.PendingIntent] backing an [AppUpdateInfo] is one-shot, so starting the flow
     * with an already consumed one throws. Play Core declares this as a checked exception, which
     * Kotlin doesn't force us to handle, so swallow it explicitly instead of crashing.
     */
    private fun startUpdateFlow(info: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                info,
                this,
                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                updateRequestCode
            )
        } catch (e: IntentSender.SendIntentException) {
            Timber.w(e, "Couldn't start the update flow")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == updateRequestCode) {
            if (resultCode != RESULT_OK) {
                Timber.w("Something went wrong updating...")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}