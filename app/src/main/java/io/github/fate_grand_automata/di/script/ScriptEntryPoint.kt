package io.github.fate_grand_automata.di.script

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import io.github.fate_grand_automata.scripts.entrypoints.*

@EntryPoint
@InstallIn(ScriptComponent::class)
interface ScriptEntryPoint {
    fun battle(): AutoBattle
    fun fp(): AutoFriendGacha
    fun giftBox(): AutoGiftBox
    fun lottery(): AutoLottery
    fun supportImageMaker(): SupportImageMaker
    fun ceBomb(): AutoCEBomb

    fun skillUpgrade(): AutoSkillUpgrade

    fun servantLevel(): AutoServantLevel

    fun autoDetect(): AutoDetect

    fun playButtonDetection(): AutoNotifyError

    fun append(): AutoAppend

    fun notifyError(): AutoNotifyError
}