package io.github.fate_grand_automata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.fate_grand_automata.scripts.prefs.IGesturesPreferences
import io.github.lib_automata.GestureService
import io.github.lib_automata.Location
import io.github.lib_automata.Waiter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * Class to perform gestures using Android's [AccessibilityService].
 */
class AccessibilityGestures @Inject constructor(
    private val gesturePrefs: IGesturesPreferences,
    private val wait: Waiter
) : GestureService {
    fun Path.moveTo(location: Location) = apply {
        moveTo(location.x.toFloat(), location.y.toFloat())
    }

    fun Path.lineTo(location: Location) = apply {
        lineTo(location.x.toFloat(), location.y.toFloat())
    }

    /**
     * On Android 7, swipe is like a flick.
     * If the swipe distance is too long, FGO won't detect it correctly and have occasional weird behaviour like sudden jumps
     */
    suspend fun swipe7(start: Location, end: Location) {
        val swipePath = Path()
            .moveTo(start)
            .lineTo(end)

        val swipeStroke = GestureDescription.StrokeDescription(
            swipePath,
            0,
            gesturePrefs.swipeDuration.inWholeMilliseconds
        )
        performGesture(swipeStroke)

        wait(gesturePrefs.swipeWaitTime)
    }

    /**
     * Android 8+ swipe is precise due to use of continued gestures.
     *
     * Instead of swiping the whole distance as a single gesture,
     * it is split into multiple small swipes, which is similar to how events are sent if a real human is doing it.
     * There is a finger down delay, followed by multiple small swipe events, followed by a finger lift delay.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun swipe8(start: Location, end: Location) {
        val xDiff = (end.x - start.x).toFloat()
        val yDiff = (end.y - start.y).toFloat()
        val direction = atan2(xDiff, yDiff)
        var distanceLeft = sqrt(xDiff.pow(2) + yDiff.pow(2))

        val swipeDelay = 1L
        val swipeDuration = 1L

        val timesToSwipe = gesturePrefs.swipeDuration.inWholeMilliseconds / (swipeDelay + swipeDuration)
        val thresholdDistance = distanceLeft / timesToSwipe

        var from = start
        val mouseDownPath = Path().moveTo(start)

        var lastStroke = GestureDescription.StrokeDescription(
            mouseDownPath,
            0,
            200L,
            true
        ).also {
            performGesture(it)
        }

        while (distanceLeft > 0) {
            val distanceToScroll = minOf(thresholdDistance, distanceLeft)

            val x = (from.x + distanceToScroll * sin(direction)).roundToInt()
            val y = (from.y + distanceToScroll * cos(direction)).roundToInt()
            val to = Location(x, y)

            val swipePath = Path()
                .moveTo(from)
                .lineTo(to)

            lastStroke = lastStroke.continueStroke(
                swipePath,
                swipeDelay,
                swipeDuration,
                true
            ).also {
                performGesture(it)
            }

            from = to
            distanceLeft -= distanceToScroll
        }

        val mouseUpPath = Path().moveTo(from)

        lastStroke.continueStroke(
            mouseUpPath,
            1,
            400L,
            false
        ).also {
            performGesture(it)
        }
    }

    override fun swipe(start: Location, end: Location) = runBlocking {
        Timber.d("swipe $start, $end")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            swipe8(start, end)
        } else swipe7(start, end)
    }

    override fun click(location: Location, times: Int) = runBlocking {
        val swipePath = Path().moveTo(location)

        val stroke = GestureDescription.StrokeDescription(
            swipePath,
            gesturePrefs.clickDelay.inWholeMilliseconds,
            gesturePrefs.clickDuration.inWholeMilliseconds
        )

        Timber.d("click $location x$times")

        repeat(times) {
            performGesture(stroke)
        }

        wait(gesturePrefs.clickWaitTime)
    }

    private suspend fun performGesture(
        strokeDesc: GestureDescription.StrokeDescription
    ): Boolean = suspendCancellableCoroutine {
        val gestureDesc = GestureDescription.Builder()
            .addStroke(strokeDesc)
            .build()

        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                it.resume(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                it.resume(false)
            }
        }

        TapperService.instance?.dispatchGesture(gestureDesc, callback, null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun longPressAndDrag8(
        clicks: List<Location>,
        chunked: Int = 1
    ) {
        val start = clicks.first()
        val end = clicks.last()

        /**
         * Creating fastest path possible
         */
        val clicksArrays = if (clicks.size > chunked) {
            val chunkedClick = clicks.chunked(chunked)
            val firstChunked = chunkedClick.first()
            val lastChunked = chunkedClick.last()
            listOfNotNull(
                firstChunked.first(),
                firstChunked.last(),
                if (lastChunked.size == chunked) null else {
                    Location(
                        x = firstChunked.last().x,
                        y = lastChunked.last().y
                    )
                },
                lastChunked.last(),
            )
        } else {
            listOf(clicks.first(), clicks.last())
        }

        /**
         * Turns out that you need to have a delay to make the
         * strokes sequential. Otherwise, they will be executed
         * at the same time or depending on when start time is.
         */
        var gestureDelay = 0L
        val longPressDuration = gesturePrefs.longPressDuration.inWholeMilliseconds
        val dragDuration = gesturePrefs.dragDuration.inWholeMilliseconds
        val dragReleaseDuration = 50L

        val mouseDownPath = Path().moveTo(start)

        /**
         * Long Pressed
         */
        var lastStroke = GestureDescription.StrokeDescription(
            mouseDownPath,
            gestureDelay,
            longPressDuration,
            true
        ).also {
            performGesture(it)
            gestureDelay += longPressDuration
        }
        Timber.d("Long Pressed")

        clicksArrays.windowed(2).forEach { (from, to) ->
            val swipePath = Path()
                .moveTo(from)
                .lineTo(to)
            lastStroke = lastStroke.continueStroke(
                swipePath,
                gestureDelay,
                dragDuration,
                true
            ).also {
                performGesture(it)
            }
            Timber.d("Drag From $from to $to  ")
            gestureDelay += dragDuration
        }

        val mouseUpPath = Path().moveTo(end)
        lastStroke.continueStroke(
            mouseUpPath,
            gestureDelay,
            dragReleaseDuration,
            false
        ).also {
            performGesture(it)
        }
        Timber.d("End the stroke")

    }

    override fun longPressAndDragOrMultipleClicks(
        clicks: List<Location>,
        chunked: Int
    ) = runBlocking {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            longPressAndDrag8(
                clicks = clicks,
                chunked = chunked
            )
        } else {
            clicks.forEach { singleClick ->
                click(singleClick)
            }
        }
    }

    override fun close() {}
}