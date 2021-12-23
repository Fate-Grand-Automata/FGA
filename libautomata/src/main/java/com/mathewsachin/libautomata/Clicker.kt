package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import javax.inject.Inject

interface Clicker {
    operator fun invoke(location: Location, times: Int = 1)
}

class RealClicker @Inject constructor(
    private val gestureService: GestureService,
    private val exitManager: ExitManager,
    transformations: ITransformationExtensions
): Clicker, ITransformationExtensions by transformations {
    override fun invoke(location: Location, times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(location.transform(), times)
    }
}