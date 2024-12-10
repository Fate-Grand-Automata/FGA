package io.github.fate_grand_automata.util

import android.content.Context
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayHelper @Inject constructor(
    @ApplicationContext val context: Context,
    windowManager: WindowManager
) {
    @Suppress("DEPRECATION")
    private val display = windowManager.defaultDisplay

    val metrics: DisplayMetrics
        get() = if (display != null) {
            DisplayMetrics().also {
                @Suppress("DEPRECATION")
                display.getRealMetrics(it)
            }
        } else {
            Resources.getSystem().displayMetrics
        }

    val rotation: Int
        get() = if (display != null) {
            display.rotation
        } else {
            val displayManager = context.getSystemService(DisplayManager::class.java)
            val displayM = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            displayM.rotation
        }
}
