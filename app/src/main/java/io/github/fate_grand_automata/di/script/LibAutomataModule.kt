package io.github.fate_grand_automata.di.script

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import io.github.lib_automata.*
import io.github.lib_automata.dagger.ScriptScope

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
    abstract fun swiper(swiper: RealSwiper): Swiper

    @ScriptScope
    @Binds
    abstract fun waiter(waiter: RealWaiter): Waiter

    @ScriptScope
    @Binds
    abstract fun highlighter(highlighter: RealHighlighter): Highlighter

    @ScriptScope
    @Binds
    abstract fun clicker(clicker: RealClicker): Clicker

    @ScriptScope
    @Binds
    abstract fun scale(scale: RealScale): Scale

    @ScriptScope
    @Binds
    abstract fun transformer(transformer: RealTransformer): Transformer

    @ScriptScope
    @Binds
    abstract fun imageMatcher(imageMatcher: RealImageMatcher): ImageMatcher

    @ScriptScope
    @Binds
    abstract fun api(api: StandardAutomataApi): AutomataApi

    @ScriptScope
    @Binds
    abstract fun longPressAndSwipeOrMultipleClicks(
        longPressAndSwipeOrMultipleClicks: RealLongPressAndSwipeOrMultipleClicks
    ): LongPressAndSwipeOrMultipleClicks
}