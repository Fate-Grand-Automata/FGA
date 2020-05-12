package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.mathewsachin.fategrandautomata.core.AutomataApi
import com.mathewsachin.fategrandautomata.core.IGestureService
import com.mathewsachin.fategrandautomata.core.Location
import com.mathewsachin.fategrandautomata.util.*

class AccessibilityGestures(private var AccessibilityService: AccessibilityService?) : IGestureService {
    override fun swipe(Start: Location, End: Location) {
        val swipePath = Path()
        swipePath.moveTo(Start.X.toFloat(), Start.Y.toFloat())
        swipePath.lineTo(End.X.toFloat(), End.Y.toFloat())

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            swipeDurationMs
        )
        performGesture(swipeStroke)

        AutomataApi.wait(swipeWaitTimeSec)
    }

    override fun click(Location: Location) {
        continueClick(Location, 1)
    }

    override fun continueClick(Location: Location, Times: Int) {
        for (i in 1..Times)
        {
            val swipePath = Path()
            swipePath.moveTo(Location.X.toFloat(), Location.Y.toFloat())

            val stroke = GestureDescription.StrokeDescription(
                swipePath,
                clickDelayMs,
                clickDurationMs
            )
            performGesture(stroke)
        }

        AutomataApi.wait(clickWaitTimeSec)
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