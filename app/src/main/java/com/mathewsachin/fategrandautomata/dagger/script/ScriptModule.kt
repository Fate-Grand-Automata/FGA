package com.mathewsachin.fategrandautomata.dagger.script

import com.mathewsachin.fategrandautomata.accessibility.AccessibilityGestures
import com.mathewsachin.fategrandautomata.scripts.FGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.*
import dagger.Binds
import dagger.Module

@Module
interface ScriptModule {
    @ScriptScope
    @Binds
    fun bindFgAutomataApi(fgAutomataApi: FGAutomataApi): IFGAutomataApi

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