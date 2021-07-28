package com.mathewsachin.fategrandautomata.util

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.Region
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CutoutManager @Inject constructor(
    windowManager: WindowManager,
    val prefs: IPreferences
) {
    private val display = windowManager.defaultDisplay

    private data class Cutout(val L: Int = 0, val T: Int = 0, val R: Int = 0, val B: Int = 0) {
        companion object {
            val NoCutouts = Cutout()
        }
    }

    private var cutoutFound = false
    private var cutoutValue = Cutout.NoCutouts

    fun applyCutout(Activity: Activity) {
        if (cutoutFound) {
            return
        }

        // Android P added support for display cutouts
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cutoutFound = true
            return
        }

        val displayCutout = Activity.window.decorView.rootWindowInsets.displayCutout
        if (displayCutout == null) {
            cutoutFound = true
            return
        }

        val cutout = Cutout(
            displayCutout.safeInsetLeft,
            displayCutout.safeInsetTop,
            displayCutout.safeInsetRight,
            displayCutout.safeInsetBottom
        )

        // Check if there is a cutout
        if (cutout != Cutout.NoCutouts) {
            val rotation = display.rotation

            // Store the cutout for Portrait orientation of device
            val (l, t, r, b) = cutout
            cutoutValue = when (rotation) {
                Surface.ROTATION_90 -> Cutout(b, l, t, r)
                Surface.ROTATION_180 -> Cutout(r, b, l, t)
                Surface.ROTATION_270 -> Cutout(t, r, b, l)
                else -> cutout
            }
        }

        cutoutFound = true
        Timber.debug { "Detected display cutout: $cutoutValue" }
    }

    private fun shouldIgnoreNotch() =
        prefs.isNewUI || prefs.ignoreNotchCalculation

    private fun getCutout(Rotation: Int): Cutout {
        if (shouldIgnoreNotch() || cutoutValue == Cutout.NoCutouts) {
            return Cutout.NoCutouts
        }

        val (l, t, r, b) = cutoutValue

        // Consider current orientation of screen
        return when (Rotation) {
            Surface.ROTATION_90 -> Cutout(t, r, b, l)
            Surface.ROTATION_180 -> Cutout(r, b, l, t)
            Surface.ROTATION_270 -> Cutout(b, l, t, r)
            else -> cutoutValue
        }
    }

    fun getCutoutAppliedRegion(): Region {
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        var w = metrics.widthPixels
        var h = metrics.heightPixels

        val cutout = getCutout(display.rotation)
        val (l, t, r, b) = cutout

        // remove notch from dimensions
        w -= l + r
        h -= t + b

        return Region(l, t, w, h)
    }
}