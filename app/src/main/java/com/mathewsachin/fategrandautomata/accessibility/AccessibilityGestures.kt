package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.graphics.Path
import com.mathewsachin.fategrandautomata.scripts.prefs.IGesturesPreferences
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.extensions.IDurationExtensions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.coroutines.resume

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

    override fun swipe(Start: Location, End: Location) = runBlocking {
        val swipePath = Path()
        swipePath.moveTo(Start.X.toFloat(), Start.Y.toFloat())
        swipePath.lineTo(End.X.toFloat(), End.Y.toFloat())

        logger.debug { "swipe $Start, $End" }

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            gesturePrefs.swipeDuration.toLongMilliseconds()
        )
        performGesture(swipeStroke)

        gesturePrefs.swipeWaitTime.wait()
    }

    override fun click(Location: Location, Times: Int) = runBlocking {
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

    private suspend fun performGesture(StrokeDesc: GestureDescription.StrokeDescription): Unit = suspendCancellableCoroutine {
        val gestureDesc = GestureDescription.Builder()
            .addStroke(StrokeDesc)
            .build()

        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                it.resume(Unit)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                it.resume(Unit)
            }
        }

        service.dispatchGesture(gestureDesc, callback, null)
    }

    override fun close() {}
}