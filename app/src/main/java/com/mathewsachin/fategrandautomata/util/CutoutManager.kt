package com.mathewsachin.fategrandautomata.util

import android.app.Activity
import android.os.Build
import android.view.Surface
import com.mathewsachin.fategrandautomata.prefs.core.GameAreaMode
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

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

    fun applyCutout(activity: Activity) {
        if (cutoutFound) {
            return
        }

        // Android P added support for display cutouts
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cutoutFound = true
            return
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
        Timber.d("Detected display cutout: $cutoutValue")
    }

    private fun shouldIgnoreNotch() =
        when (prefs.gameServer) {
            // TW may or may not cover notch area
            GameServerEnum.Tw -> prefs.ignoreNotchCalculation
            else -> true
        }

    private fun getCutout(rotation: Int): Cutout {
        if (shouldIgnoreNotch() || cutoutValue == Cutout.NoCutouts) {
            return Cutout.NoCutouts
        }

        val (l, t, r, b) = cutoutValue

        // Consider current orientation of screen
        return when (rotation) {
            Surface.ROTATION_90 -> Cutout(t, r, b, l)
            Surface.ROTATION_180 -> Cutout(r, b, l, t)
            Surface.ROTATION_270 -> Cutout(b, l, t, r)
            else -> cutoutValue
        }
    }

    private fun getScreenSize(): Size {
        val metrics = display.metrics

        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    fun getCutoutAppliedRegion(): Region {
        val (w, h) = getScreenSize()

        return when (prefsCore.gameAreaMode.get()) {
            GameAreaMode.Default -> {
                val cutout = getCutout(display.rotation)
                val (l, t, r, b) = cutout

                // remove notch from dimensions
                Region(l, t, w - l - r, h - t - b)
            }
            GameAreaMode.Xperia -> {
                val fgoRatio = 0.9
                val barRatio = 0.026
                val navRatio = 1 - (fgoRatio + barRatio)

                val leftRatio = if (display.rotation == Surface.ROTATION_270) navRatio else barRatio

                Region((w * leftRatio).roundToInt(), 0, (w * fgoRatio).roundToInt(), h)
            }
            GameAreaMode.Duo -> Region(192, 0, 2400, h)
            GameAreaMode.Custom -> {
                val l = prefsCore.gameOffsetLeft.get()
                val t = prefsCore.gameOffsetTop.get()
                val r = prefsCore.gameOffsetRight.get()
                val b = prefsCore.gameOffsetBottom.get()

                // if the camera is on the right, use the right offset as x
                val x = if (display.rotation == Surface.ROTATION_270) r else l
                Region(x, t, w - l - r, h - t - b)
            }
        }
    }
}
