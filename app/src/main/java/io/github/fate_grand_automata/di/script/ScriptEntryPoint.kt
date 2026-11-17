package io.github.fate_grand_automata.di.script

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import io.github.fate_grand_automata.scripts.entrypoints.*
import io.github.fate_grand_automata.scripts.models.SkillUpgradeState
import io.github.lib_automata.OcrService

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

    fun skill(): AutoSkillUpgrade

    fun autoDetect(): AutoDetect

    /** Shared by auto-detection, the skill launcher and [AutoSkillUpgrade] within one run. */
    fun skillUpgradeState(): SkillUpgradeState

    /** Exposed so [ScriptComponent]'s OCR engine can be released when the run ends. */
    fun ocrService(): OcrService
}