package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.wait

/**
 * Class to perform gestures using Android's [AccessibilityService].
 */
class AccessibilityGestures(private var AccessibilityService: AccessibilityService?) :
    IGestureService {
    override fun swipe(Start: Location, End: Location) {
        val swipePath = Path()
        swipePath.moveTo(Start.X.toFloat(), Start.Y.toFloat())
        swipePath.lineTo(End.X.toFloat(), End.Y.toFloat())

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            Preferences.Gestures.swipeDuration.toLongMilliseconds()
        )
        performGesture(swipeStroke)

        Preferences.Gestures.swipeWaitTime.wait()
    }

    override fun click(Location: Location, Times: Int) {
        val swipePath = Path()
        swipePath.moveTo(Location.X.toFloat(), Location.Y.toFloat())

        val stroke = GestureDescription.StrokeDescription(
            swipePath,
            Preferences.Gestures.clickDelay.toLongMilliseconds(),
            Preferences.Gestures.clickDuration.toLongMilliseconds()
        )

        repeat(Times) {
            performGesture(stroke)
        }

        Preferences.Gestures.clickWaitTime.wait()
    }

    private fun performGesture(StrokeDesc: GestureDescription.StrokeDescription) {
        val acc = AccessibilityService ?: return

        val gestureDesc = GestureDescription.Builder()
            .addStroke(StrokeDesc)
            .build()

        val callback = GestureCompletedCallback()

        acc.dispatchGesture(gestureDesc, callback, null)

        callback.waitTillFinish()
    }

    override fun close() {
        AccessibilityService = null
    }
}