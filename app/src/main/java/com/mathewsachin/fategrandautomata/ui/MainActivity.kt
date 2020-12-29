package com.mathewsachin.fategrandautomata.ui

import android.os.Bundle
import android.os.PowerManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.ActivityMainBinding
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsViewModel
import com.mathewsachin.fategrandautomata.util.CutoutManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    @Inject
    lateinit var powerManager: PowerManager

    private lateinit var appBarConfiguration: AppBarConfiguration
    val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    val navController get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
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
