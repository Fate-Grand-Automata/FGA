package com.mathewsachin.fategrandautomata.dagger

import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Module
import dagger.Provides

@Module
class ScreenshotModule(val screenshotService: IScreenshotService) {
    @ScriptScope
    @Provides
    fun provideScreenshotService() = screenshotService
}