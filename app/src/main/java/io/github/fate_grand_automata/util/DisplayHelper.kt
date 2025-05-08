package io.github.fate_grand_automata.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayHelper @Inject constructor(
    @ApplicationContext val context: Context,
    private val windowManager: WindowManager,
) {
    @Suppress("DEPRECATION")
    private val display = windowManager.defaultDisplay

    val metrics: DisplayMetrics
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val bounds = windowManager.currentWindowMetrics.bounds
            val contextMetrics = context.resources.displayMetrics

            val defaultMetrics = DisplayMetrics().apply {
                widthPixels = bounds.width()
                heightPixels = bounds.height()
                density = contextMetrics.density // Fix: Use context's display metrics
                densityDpi = contextMetrics.densityDpi
            }

            defaultMetrics
        } else {
            DisplayMetrics().also {
                @Suppress("DEPRECATION")
                display.getRealMetrics(it)
            }
        }

    val rotation: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = context.getSystemService(DisplayManager::class.java)
            val displayM = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            displayM?.rotation ?: 0
        } else {
            @Suppress("DEPRECATION")
            display?.rotation ?: 0
        }
}