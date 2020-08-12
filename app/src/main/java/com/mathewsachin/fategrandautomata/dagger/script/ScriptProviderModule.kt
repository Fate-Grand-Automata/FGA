package com.mathewsachin.fategrandautomata.dagger.script

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Module
import dagger.Provides

@Module
class ScriptProviderModule {
    @ScriptScope
    @Provides
    fun provideExitManager() = ExitManager()
}