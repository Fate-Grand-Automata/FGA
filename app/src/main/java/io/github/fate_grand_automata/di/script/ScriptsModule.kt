package io.github.fate_grand_automata.di.script

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import io.github.fate_grand_automata.accessibility.AccessibilityGestures
import io.github.fate_grand_automata.accessibility.AccessibilityGlobalEvents
import io.github.fate_grand_automata.scripts.FgoAutomataApi
import io.github.fate_grand_automata.scripts.FgoGameAreaManager
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.locations.IScriptAreaTransforms
import io.github.fate_grand_automata.scripts.locations.ScriptAreaTransforms
import io.github.fate_grand_automata.scripts.modules.RealSupportScreen
import io.github.fate_grand_automata.scripts.modules.SupportScreen
import io.github.lib_automata.GameAreaManager
import io.github.lib_automata.GestureService
import io.github.lib_automata.GlobalEventService
import io.github.lib_automata.PlatformImpl
import io.github.lib_automata.dagger.ScriptScope

@Module
@InstallIn(ScriptComponent::class)
abstract class ScriptsModule {
    companion object {
        @ScriptScope
        @Provides
        fun provideGameAreaManager(platformImpl: PlatformImpl): GameAreaManager =
            FgoGameAreaManager(
                gameSizeWithBorders = platformImpl.windowRegion.size,
                offset = { platformImpl.windowRegion.location }
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
    abstract fun globalEvents(globalEvents: AccessibilityGlobalEvents): GlobalEventService

    @ScriptScope
    @Binds
    abstract fun scriptAreaTransforms(scriptAreaTransforms: ScriptAreaTransforms): IScriptAreaTransforms

    @ScriptScope
    @Binds
    abstract fun supportScreen(screen: RealSupportScreen): SupportScreen
}