package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.accessibility.AccessibilityGestures
import com.mathewsachin.fategrandautomata.scripts.FgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn

@Module
@InstallIn(ScriptComponent::class)
interface ScriptBindsModule {
    @ScriptScope
    @Binds
    fun bindFgAutomataApi(fgoAutomataApi: FgoAutomataApi): IFgoAutomataApi

    @ScriptScope
    @Binds
    fun bindAutomataApi(automataApi: AutomataApi): IAutomataExtensions

    @ScriptScope
    @Binds
    fun bindGestureExtensions(gestureExtensions: GestureExtensions): IGestureExtensions

    @ScriptScope
    @Binds
    fun bindHighlightExtensions(highlightExtensions: HighlightExtensions): IHighlightExtensions

    @ScriptScope
    @Binds
    fun bindImageMatchingExtensions(imageMatchingExtensions: ImageMatchingExtensions): IImageMatchingExtensions

    @ScriptScope
    @Binds
    fun bindTransformationExtensions(transformationExtensions: TransformationExtensions): ITransformationExtensions

    @ScriptScope
    @Binds
    fun bindGestures(gestures: AccessibilityGestures): IGestureService

    @ScriptScope
    @Binds
    fun bindDurationExtensions(durationExtensions: DurationExtensions): IDurationExtensions
}