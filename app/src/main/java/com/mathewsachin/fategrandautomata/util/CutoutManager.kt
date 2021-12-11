package com.mathewsachin.fategrandautomata.util

import android.app.Activity
import android.os.Build
import android.view.Surface
import com.mathewsachin.fategrandautomata.prefs.core.GameAreaMode
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CutoutManager @Inject constructor(
    private val display: DisplayHelper,
    private val prefs: IPreferences,
    private val prefsCore: PrefsCore,
) {
    private data class Cutout(val L: Int = 0, val T: Int = 0, val R: Int = 0, val B: Int = 0) {
        companion object {
            val NoCutouts = Cutout()
        }
    }

    private var cutoutFound = false
    private var cutoutValue = Cutout.NoCutouts

    private var systemBarCutouts = Cutout.NoCutouts

    fun applyCutout(activity: Activity) {
        if (cutoutFound) {
            return
        }

        // Android P added support for display cutouts
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cutoutFound = true
            return
        }

        // measure the status and navigation bar for Xperia
        activity.window.decorView.rootWindowInsets.let {
            systemBarCutouts = Cutout(
                it.systemWindowInsetLeft, it.systemWindowInsetTop,
                it.systemWindowInsetRight, it.systemWindowInsetBottom
            )
        }

        val displayCutout = activity.window.decorView.rootWindowInsets.displayCutout
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
        when {
            // CN may or may not cover notch area
            prefs.isNewUI && prefs.gameServer != GameServerEnum.Cn -> true
            else -> prefs.ignoreNotchCalculation
        }

    private fun adjustCutoutForRotation(cutout: Cutout, rotation: Int): Cutout {
        if (shouldIgnoreNotch() || cutout == Cutout.NoCutouts) {
            return Cutout.NoCutouts
        }

        val (l, t, r, b) = cutout

        // Consider current orientation of screen
        return when (rotation) {
            Surface.ROTATION_90 -> Cutout(t, r, b, l)
            Surface.ROTATION_180 -> Cutout(r, b, l, t)
            Surface.ROTATION_270 -> Cutout(b, l, t, r)
            else -> cutout
        }
    }

    private fun getScreenSize(): Size {
        val metrics = display.metrics

        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    fun getCutoutAppliedRegion(): Region {
        var (w, h) = getScreenSize()

        val (l, t, r, b) = when (prefsCore.gameAreaMode.get()) {
            GameAreaMode.Default -> adjustCutoutForRotation(cutoutValue, display.rotation)
            GameAreaMode.Xperia -> adjustCutoutForRotation(systemBarCutouts, display.rotation)
            GameAreaMode.Duo -> Cutout(192, 0, 192, 0)
        }
        // remove notch from dimensions
        w -= l + r
        h -= t + b

        return Region(l, t, w, h)
    }
}
