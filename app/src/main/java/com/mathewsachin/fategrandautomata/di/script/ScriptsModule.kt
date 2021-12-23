package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.accessibility.AccessibilityGestures
import com.mathewsachin.fategrandautomata.scripts.FgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.locations.IScriptAreaTransforms
import com.mathewsachin.fategrandautomata.scripts.locations.ScriptAreaTransforms
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.GestureService
import com.mathewsachin.libautomata.PlatformImpl
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(ScriptComponent::class)
abstract class ScriptsModule {
    companion object {
        @ScriptScope
        @Provides
        fun provideGameAreaManager(platformImpl: PlatformImpl, prefs: IPreferences): GameAreaManager =
            FgoGameAreaManager(
                gameSizeWithBorders = platformImpl.windowRegion.size,
                offset = { platformImpl.windowRegion.location },
                isNewUI = prefs.isNewUI
            )
    }

    @ScriptScope
    @Binds
    abstract fun api(api: FgoAutomataApi): IFgoAutomataApi

    @ScriptScope
    @Binds
    abstract fun gestures(gestures: AccessibilityGestures): GestureService

    @ScriptScope
    @Binds
    abstract fun scriptAreaTransforms(scriptAreaTransforms: ScriptAreaTransforms): IScriptAreaTransforms
}