package com.mathewsachin.fategrandautomata.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.CutoutManager
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appComponent.inject(this)

        setSupportActionBar(toolbar)

        val navController: NavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Only once
        if (savedInstanceState == null) {
            checkPermissions()
            ignoreBatteryOptimizations()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
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
}
