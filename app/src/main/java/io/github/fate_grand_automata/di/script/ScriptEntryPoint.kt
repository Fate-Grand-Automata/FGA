package io.github.fate_grand_automata.di.script

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.entrypoints.AutoCEBomb
import io.github.fate_grand_automata.scripts.entrypoints.AutoDetect
import io.github.fate_grand_automata.scripts.entrypoints.AutoFriendGacha
import io.github.fate_grand_automata.scripts.entrypoints.AutoGiftBox
import io.github.fate_grand_automata.scripts.entrypoints.AutoLottery
import io.github.fate_grand_automata.scripts.entrypoints.AutoServantLevel
import io.github.fate_grand_automata.scripts.entrypoints.SupportImageMaker

@EntryPoint
@InstallIn(ScriptComponent::class)
interface ScriptEntryPoint {
    fun battle(): AutoBattle
    fun fp(): AutoFriendGacha
    fun giftBox(): AutoGiftBox
    fun lottery(): AutoLottery
    fun supportImageMaker(): SupportImageMaker
    fun ceBomb(): AutoCEBomb

    fun servantLevel(): AutoServantLevel

    fun autoDetect(): AutoDetect
}
