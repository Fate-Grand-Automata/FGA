package io.github.fate_grand_automata.util

import android.util.DisplayMetrics
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayHelper @Inject constructor(
    windowManager: WindowManager
) {
    private val display = windowManager.defaultDisplay

    val metrics: DisplayMetrics
        get() =
        DisplayMetrics().also { display.getRealMetrics(it) }

    val rotation get() = display.rotation
}