package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(ScriptComponent::class)
abstract class LibAutomataModule {
    companion object {
        @ScriptScope
        @Provides
        fun exitManager() = ExitManager()
    }

    @ScriptScope
    @Binds
    abstract fun automataApi(automataApi: AutomataApi): IAutomataExtensions

    @ScriptScope
    @Binds
    abstract fun gestureExtensions(gestureExtensions: GestureExtensions): IGestureExtensions

    @ScriptScope
    @Binds
    abstract fun highlightExtensions(highlightExtensions: HighlightExtensions): IHighlightExtensions

    @ScriptScope
    @Binds
    abstract fun imageMatchingExtensions(imageMatchingExtensions: ImageMatchingExtensions): IImageMatchingExtensions

    @ScriptScope
    @Binds
    abstract fun transformationExtensions(transformationExtensions: TransformationExtensions): ITransformationExtensions

    @ScriptScope
    @Binds
    abstract fun durationExtensions(durationExtensions: DurationExtensions): IDurationExtensions
}