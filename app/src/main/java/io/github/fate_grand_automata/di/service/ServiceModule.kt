package io.github.fate_grand_automata.di.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.scripts.IScriptMessages
import io.github.fate_grand_automata.util.AndroidImpl
import io.github.fate_grand_automata.util.ScriptMessages
import io.github.lib_automata.PlatformImpl

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