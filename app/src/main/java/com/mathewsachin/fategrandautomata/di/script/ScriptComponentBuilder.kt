package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.libautomata.ScreenshotService
import dagger.BindsInstance
import dagger.hilt.DefineComponent

@DefineComponent.Builder
interface ScriptComponentBuilder {
    fun screenshotService(@BindsInstance screenshotService: ScreenshotService): ScriptComponentBuilder
    fun build(): ScriptComponent
}