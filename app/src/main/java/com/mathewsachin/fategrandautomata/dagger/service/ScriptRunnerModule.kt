package com.mathewsachin.fategrandautomata.dagger.service

import com.mathewsachin.fategrandautomata.util.AndroidImpl
import com.mathewsachin.libautomata.IPlatformImpl
import dagger.Binds
import dagger.Module

@Module
interface ScriptRunnerModule {
    @ServiceScope
    @Binds
    fun bindPlatformImpl(impl: AndroidImpl): IPlatformImpl
}