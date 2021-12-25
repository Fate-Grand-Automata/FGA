package com.mathewsachin.fategrandautomata.di.service

import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.util.AndroidImpl
import com.mathewsachin.fategrandautomata.util.ScriptMessages
import com.mathewsachin.libautomata.PlatformImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
interface ServiceModule {
    @ServiceScoped
    @Binds
    fun bindPlatformImpl(impl: AndroidImpl): PlatformImpl

    @ServiceScoped
    @Binds
    fun bindScriptMessages(scriptMessages: ScriptMessages): IScriptMessages
}