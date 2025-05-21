package io.github.lib_automata

import javax.inject.Inject

interface LongPressAndSwipeOrMultipleClicks {
    operator fun invoke(clicks: List<List<Location>>, chunked: Int)
}

class RealLongPressAndSwipeOrMultipleClicks @Inject constructor(
    private val gestureService: GestureService,
    private val platformImpl: PlatformImpl,
    private val transform: Transformer
) : LongPressAndSwipeOrMultipleClicks {

    override fun invoke(clicks: List<List<Location>>, chunked: Int) {

        val transformClicks = clicks.map { row ->
            row.map { columnLocation ->
                transform.toScreen(columnLocation)
            }
        }

        gestureService.longPressAndDragOrMultipleClicks(
            clicks = transformClicks,
            chunked = chunked
        )
    }
}