package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import com.mathewsachin.fategrandautomata.scripts.prefs.IGesturesPreferences
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.extensions.IDurationExtensions
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.math.*

private val logger = KotlinLogging.logger {}

/**
 * Class to perform gestures using Android's [AccessibilityService].
 */
class AccessibilityGestures @Inject constructor(
    service: Service,
    val gesturePrefs: IGesturesPreferences,
    durationExtensions: IDurationExtensions
) : IGestureService, IDurationExtensions by durationExtensions {
    val service = service as AccessibilityService

    fun Path.moveTo(location: Location) = apply {
        moveTo(location.X.toFloat(), location.Y.toFloat())
    }

    fun Path.lineTo(location: Location) = apply {
        lineTo(location.X.toFloat(), location.Y.toFloat())
    }

    /**
     * On Android 7, swipe is like a flick.
     * If the swipe distance is too long, FGO won't detect it correctly and have occasional weird behaviour like sudden jumps
     */
    fun swipe7(start: Location, end: Location) {
        val swipePath = Path()
            .moveTo(start)
            .lineTo(end)

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            gesturePrefs.swipeDuration.toLongMilliseconds()
        )
        performGesture(swipeStroke)

        gesturePrefs.swipeWaitTime.wait()
    }

    /**
     * Android 8+ swipe is precise due to use of continued gestures.
     *
     * Instead of swiping the whole distance as a single gesture,
     * it is split into multiple small swipes, which is similar to how events are sent if a real human is doing it.
     * There is a finger down delay, followed by multiple small swipe events, followed by a finger lift delay.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun swipe8(start: Location, end: Location) {
        val xDiff = (end.X - start.X).toFloat()
        val yDiff = (end.Y - start.Y).toFloat()
        val direction = atan2(xDiff, yDiff)
        var distanceLeft = sqrt(xDiff.pow(2) + yDiff.pow(2))

        val thresholdDistance = 5f
        val swipeDuration = 1L

        var from = start

        val mouseDownPath = Path().moveTo(start)

        var lastStroke = GestureDescription.StrokeDescription(
            mouseDownPath,
            0,
            200L,
            true
        )

        performGesture(lastStroke)

        while (distanceLeft > 0) {
            val distanceToScroll = minOf(thresholdDistance, distanceLeft)

            val x = (from.X + distanceToScroll * sin(direction)).roundToInt()
            val y = (from.Y + distanceToScroll * cos(direction)).roundToInt()
            val to = Location(x, y)

            val swipePath = Path()
                .moveTo(from)
                .lineTo(to)

            lastStroke = lastStroke.continueStroke(
                swipePath,
                1,
                swipeDuration,
                true
            )

            performGesture(lastStroke)

            from = to

            distanceLeft -= distanceToScroll
        }

        val mouseUpPath = Path().moveTo(from)

        lastStroke = lastStroke.continueStroke(
            mouseUpPath,
            1,
            400L,
            false
        )

        performGesture(lastStroke)
    }

    override fun swipe(Start: Location, End: Location) {
        logger.debug { "swipe $Start, $End" }

        val swipeFunction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ::swipe8
        } else ::swipe7

        swipeFunction(Start, End)
    }

    override fun click(Location: Location, Times: Int) {
        val swipePath = Path().moveTo(Location)

        val stroke = GestureDescription.StrokeDescription(
            swipePath,
            gesturePrefs.clickDelay.toLongMilliseconds(),
            gesturePrefs.clickDuration.toLongMilliseconds()
        )

        logger.debug { "click $Location x$Times" }

        repeat(Times) {
            performGesture(stroke)
        }

        gesturePrefs.clickWaitTime.wait()
    }

    private fun performGesture(StrokeDesc: GestureDescription.StrokeDescription) {
        val gestureDesc = GestureDescription.Builder()
            .addStroke(StrokeDesc)
            .build()

        val callback = GestureCompletedCallback()

        service.dispatchGesture(gestureDesc, callback, null)

        callback.waitTillFinish()
    }

    override fun close() {}
}