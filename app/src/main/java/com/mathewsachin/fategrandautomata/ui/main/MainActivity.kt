package com.mathewsachin.fategrandautomata.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.mathewsachin.fategrandautomata.ui.pref_support.SupportViewModel
import com.mathewsachin.fategrandautomata.util.CutoutManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var cutoutManager: CutoutManager

    val vm: MainScreenViewModel by viewModels()
    val supportVm: SupportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

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
}