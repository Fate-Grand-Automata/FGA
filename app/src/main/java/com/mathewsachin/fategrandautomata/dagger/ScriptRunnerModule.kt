package com.mathewsachin.fategrandautomata.dagger

import com.mathewsachin.fategrandautomata.accessibility.AccessibilityGestures
import com.mathewsachin.fategrandautomata.util.AndroidImpl
import com.mathewsachin.libautomata.IGestureService
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.extensions.DurationExtensions
import com.mathewsachin.libautomata.extensions.IDurationExtensions
import dagger.Binds
import dagger.Module

@Module
abstract class ScriptRunnerModule {
    @ServiceScope
    @Binds
    abstract fun bindPlatformImpl(impl: AndroidImpl): IPlatformImpl

    @ServiceScope
    @Binds
    abstract fun bindGestures(gestures: AccessibilityGestures): IGestureService

    @ServiceScope
    @Binds
    abstract fun bindDurationExtensions(durationExtensions: DurationExtensions): IDurationExtensions
}