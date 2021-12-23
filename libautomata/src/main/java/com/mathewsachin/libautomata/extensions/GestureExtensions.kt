package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.GestureService
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class GestureExtensions @Inject constructor(
    private val gestureService: GestureService,
    private val exitManager: ExitManager,
    transformationExtensions: ITransformationExtensions
) : IGestureExtensions, ITransformationExtensions by transformationExtensions {
    override fun Location.click(times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(this.transform(), times)
    }

    override fun Region.click(times: Int) = center.click(times)
}