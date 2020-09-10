package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.libautomata.IScreenshotService
import dagger.BindsInstance
import dagger.hilt.DefineComponent

@DefineComponent.Builder
interface ScriptComponentBuilder {
    fun screenshotService(@BindsInstance screenshotService: IScreenshotService): ScriptComponentBuilder
    fun build(): ScriptComponent
}