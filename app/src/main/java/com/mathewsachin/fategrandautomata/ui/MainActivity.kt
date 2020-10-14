package com.mathewsachin.fategrandautomata.ui

import android.os.Bundle
import android.os.PowerManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsViewModel
import com.mathewsachin.fategrandautomata.util.CutoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    @Inject
    lateinit var cutoutManager: CutoutManager

    @Inject
    lateinit var powerManager: PowerManager

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        val navController: NavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
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

    override fun onStart() {
        super.onStart()

        val vm: MainSettingsViewModel by viewModels()
        vm.activityStarted()
    }
}
