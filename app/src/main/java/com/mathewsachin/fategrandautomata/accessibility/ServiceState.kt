package com.mathewsachin.fategrandautomata.accessibility

import com.mathewsachin.libautomata.IScreenshotService

sealed class ServiceState {
    object Stopped : ServiceState()
    class Started(val screenshotService: IScreenshotService) : ServiceState()
}