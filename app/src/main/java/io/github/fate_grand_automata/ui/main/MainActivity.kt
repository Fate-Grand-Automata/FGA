package io.github.fate_grand_automata.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import io.github.fate_grand_automata.ui.pref_support.SupportViewModel
import io.github.fate_grand_automata.util.CutoutManager
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    val vm: MainScreenViewModel by viewModels()
    val supportVm: SupportViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FgaApp(
                vm = vm,
                supportVm = supportVm
            )
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        cutoutManager.applyCutout(this)
    }

    override fun onStart() {
        super.onStart()
        vm.activityStarted()
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Timber.i("New version is available: ${it.availableVersionCode()}")
                appUpdateManager.startUpdateFlowForResult(it, this, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE), 100)
            } else {
                Timber.d("No update available")
            }
        }
    }
}