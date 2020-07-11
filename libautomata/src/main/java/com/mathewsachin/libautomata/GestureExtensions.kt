package com.mathewsachin.libautomata

interface IGestureExtensions {
    /**
     * Clicks on the [Location].
     *
     * @param Times the amount of times to click
     */
    fun Location.click(Times: Int = 1)

    /**
     * Clicks on the center of this Region.
     *
     * @param Times the amount of times to click
     */
    fun Region.click(Times: Int = 1)

    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param Start the [Location] where the swipe should start
     * @param End the [Location] where the swipe should end
     */
    fun swipe(Start: Location, End: Location)
}

class GestureExtensions(
    val exitManager: ExitManager,
    val gestureService: IGestureService,
    transformationExtensions: ITransformationExtensions
): IGestureExtensions, ITransformationExtensions by transformationExtensions {
    /**
     * Clicks on the [Location].
     *
     * @param Times the amount of times to click
     */
    override fun Location.click(Times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(this.transform(), Times)
    }

    /**
     * Clicks on the center of this Region.
     *
     * @param Times the amount of times to click
     */
    override fun Region.click(Times: Int) = center.click(Times)

    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param Start the [Location] where the swipe should start
     * @param End the [Location] where the swipe should end
     */
    override fun swipe(Start: Location, End: Location) {
        gestureService.swipe(Start.transform(), End.transform())
    }
}