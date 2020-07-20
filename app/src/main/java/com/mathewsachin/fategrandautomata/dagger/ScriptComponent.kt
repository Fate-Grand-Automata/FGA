package com.mathewsachin.fategrandautomata.dagger

import com.mathewsachin.fategrandautomata.util.ScriptLaunchParams
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Subcomponent

@ScriptScope
@Subcomponent(modules = [ScriptModule::class, ScreenshotModule::class])
interface ScriptComponent {
    @Subcomponent.Builder
    interface Builder {
        fun screenshotModule(module: ScreenshotModule): Builder

        fun build(): ScriptComponent
    }

    @ScriptScope
    fun getScriptLaunchParams(): ScriptLaunchParams
}