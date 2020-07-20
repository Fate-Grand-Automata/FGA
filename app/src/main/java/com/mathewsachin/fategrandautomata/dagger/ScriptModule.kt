package com.mathewsachin.fategrandautomata.dagger

import com.mathewsachin.fategrandautomata.scripts.FGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.*
import dagger.Binds
import dagger.Module

@Module
abstract class ScriptModule {
    @ScriptScope
    @Binds
    abstract fun bindFgAutomataApi(fgAutomataApi: FGAutomataApi): IFGAutomataApi

    @ScriptScope
    @Binds
    abstract fun bindAutomataApi(automataApi: AutomataApi): IAutomataExtensions

    @ScriptScope
    @Binds
    abstract fun bindGestureExtensions(gestureExtensions: GestureExtensions): IGestureExtensions

    @ScriptScope
    @Binds
    abstract fun bindHighlightExtensions(highlightExtensions: HighlightExtensions): IHighlightExtensions

    @ScriptScope
    @Binds
    abstract fun bindImageMatchingExtensions(imageMatchingExtensions: ImageMatchingExtensions): IImageMatchingExtensions

    @ScriptScope
    @Binds
    abstract fun bindTransformationExtensions(transformationExtensions: TransformationExtensions): ITransformationExtensions
}