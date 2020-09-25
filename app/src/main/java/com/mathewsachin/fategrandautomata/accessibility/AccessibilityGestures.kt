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

    fun swipe7(Start: Location, End: Location) {
        val swipePath = Path().apply {
            moveTo(Start.X.toFloat(), Start.Y.toFloat())
            lineTo(End.X.toFloat(), End.Y.toFloat())
        }

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            gesturePrefs.swipeDuration.toLongMilliseconds()
        )
        performGesture(swipeStroke)

        gesturePrefs.swipeWaitTime.wait()
    }

    /*
    void ManualTouch(Location Start, Location End)
{
    var direction = Math.Atan2(Start.X - End.X, Start.Y - End.Y);

    var distanceLeft = Math.Sqrt(Math.Pow(Start.X - End.X, 2) + Math.Pow(Start.Y - End.Y, 2));

    const int thresholdDistance = 5;
    const int betweenScrollWait = 5;

    while (distanceLeft > 0)
    {
        var distanceToScroll = Math.Max(thresholdDistance, distanceLeft);

        var x = (Start.X + distanceToScroll * Math.Cos(direction)).Round();
        var y = (Start.Y + distanceToScroll * Math.Sin(direction)).Round();
        End = new Location(x, y);

        Scroll(Start, End);
        AutomataApi.Wait(betweenScrollWait);

        Start = End;

        distanceLeft -= distanceToScroll;
    }
}
    */

    @RequiresApi(Build.VERSION_CODES.O)
    fun swipe8(Start: Location, End: Location) {
        val xDiff = (End.X - Start.X).toFloat()
        val yDiff = (End.Y - Start.Y).toFloat()
        val direction = atan2(xDiff, yDiff)
        var distanceLeft = sqrt(xDiff.pow(2) + yDiff.pow(2))

        val thresholdDistance = 5f
        val tapDuration = 100L
        val swipeDuration = 1L

        var from = Start

        val mouseDownPath = Path().apply {
            moveTo(Start.X.toFloat(), Start.Y.toFloat())
        }

        var lastStroke = GestureDescription.StrokeDescription(
            mouseDownPath,
            0,
            tapDuration,
            true
        )

        performGesture(lastStroke)

        while (distanceLeft > 0) {
            val distanceToScroll = minOf(thresholdDistance, distanceLeft)

            val x = (from.X + distanceToScroll * sin(direction)).roundToInt()
            val y = (from.Y + distanceToScroll * cos(direction)).roundToInt()
            val to = Location(x, y)

            val swipePath = Path().apply {
                moveTo(from.X.toFloat(), from.Y.toFloat())
                lineTo(to.X.toFloat(), to.Y.toFloat())
            }

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

        val mouseUpPath = Path().apply {
            moveTo(from.X.toFloat(), from.Y.toFloat())
        }

        lastStroke = lastStroke.continueStroke(
            mouseUpPath,
            1,
            tapDuration,
            false
        )

        performGesture(lastStroke)

        gesturePrefs.swipeWaitTime.wait()
    }

    override fun swipe(Start: Location, End: Location) {
        logger.debug { "swipe $Start, $End" }

        val swipeFunction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ::swipe8
        } else ::swipe7

        swipeFunction(Start, End)
    }

    override fun click(Location: Location, Times: Int) {
        val swipePath = Path()
        swipePath.moveTo(Location.X.toFloat(), Location.Y.toFloat())

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