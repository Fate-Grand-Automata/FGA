package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.scripts.entrypoints.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(ScriptComponent::class)
interface ScriptEntryPoint {
    fun battle(): AutoBattle

    fun friendGacha(): AutoFriendGacha

    fun lottery(): AutoLottery

    fun supportImageMaker(): SupportImageMaker

    fun giftBox(): AutoGiftBox
}