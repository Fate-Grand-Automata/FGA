package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoOther
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(ScriptComponent::class)
interface ScriptEntryPoint {
    fun battle(): AutoBattle

    fun other(): AutoOther
}