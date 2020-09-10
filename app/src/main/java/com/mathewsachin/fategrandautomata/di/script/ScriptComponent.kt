package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ServiceComponent

@ScriptScope
@DefineComponent(parent = ServiceComponent::class)
interface ScriptComponent