package com.mathewsachin.fategrandautomata.dagger.service

import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.dagger.script.ScriptComponent
import com.mathewsachin.fategrandautomata.dagger.script.ScriptRunnerModule
import dagger.Subcomponent

@ServiceScope
@Subcomponent(modules = [ScriptRunnerServiceModule::class, ScriptRunnerModule::class])
interface ScriptRunnerServiceComponent {
    @Subcomponent.Builder
    interface Builder {
        fun scriptRunnerServiceModule(module: ScriptRunnerServiceModule): Builder

        fun build(): ScriptRunnerServiceComponent
    }

    fun inject(service: ScriptRunnerService)

    fun scriptComponent(): ScriptComponent.Builder
}