package com.mathewsachin.fategrandautomata.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.mathewsachin.fategrandautomata.databinding.ActivityMainBinding
import com.mathewsachin.fategrandautomata.util.CutoutManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
