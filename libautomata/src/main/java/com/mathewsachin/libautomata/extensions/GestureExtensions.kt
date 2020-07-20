package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class GestureExtensions @Inject constructor(
    val gestureService: IGestureService,
    val exitManager: ExitManager,
    transformationExtensions: ITransformationExtensions
) : IGestureExtensions, ITransformationExtensions by transformationExtensions {
    override fun Location.click(Times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(this.transform(), Times)
    }

    override fun Region.click(Times: Int) = center.click(Times)

    override fun swipe(Start: Location, End: Location) {
        gestureService.swipe(Start.transform(), End.transform())
    }
}