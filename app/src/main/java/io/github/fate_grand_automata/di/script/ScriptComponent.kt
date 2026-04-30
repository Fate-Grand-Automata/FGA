package io.github.fate_grand_automata.di.script

import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ServiceComponent
import io.github.lib_automata.dagger.ScriptScope

@ScriptScope
@DefineComponent(parent = ServiceComponent::class)
interface ScriptComponent
