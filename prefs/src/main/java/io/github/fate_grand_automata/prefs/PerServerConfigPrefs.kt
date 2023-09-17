package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs

internal class PerServerConfigPrefs(
    override val id: String,
    prefsCore: PrefsCore
) : IPerServerConfigPrefs {

    val prefs = prefsCore.forPerServerConfigPrefs(id)
    override var selectedAutoSkillKey by prefs.selectedAutoSkillConfig

    override var serverRaw by prefs.serverRaw

    override var rainbowApple by prefs.rainbowAppleCount

    override var goldApple by prefs.goldAppleCount
    override var silverApple by prefs.silverAppleCount
    override var blueApple by prefs.blueAppleCount
    override var copperApple by prefs.copperAppleCount


    override var waitForAPRegen by prefs.waitAPRegen

    override var selectedApple by prefs.selectedApple
    override var currentAppleCount: Int
        get() {
            return when(selectedApple){
                RefillResourceEnum.Copper -> copperApple
                RefillResourceEnum.Bronze -> blueApple
                RefillResourceEnum.Silver -> silverApple
                RefillResourceEnum.Gold -> goldApple
                RefillResourceEnum.SQ -> rainbowApple
            }
        }
        set(value) {
            when(selectedApple){
                RefillResourceEnum.Copper -> copperApple = value
                RefillResourceEnum.Bronze -> blueApple = value
                RefillResourceEnum.Silver -> silverApple = value
                RefillResourceEnum.Gold -> goldApple = value
                RefillResourceEnum.SQ -> rainbowApple = value
            }
        }

    override val resources: List<RefillResourceEnum> by prefs.refill.resources.map { set ->
        set.sortedBy { it.ordinal }
    }

    override fun updateResources(resources: Set<RefillResourceEnum>) = prefs.refill.resources.set(resources)

    override var shouldLimitRuns: Boolean by prefs.refill.shouldLimitRuns
    override var limitRuns: Int by prefs.refill.limitRuns
    override var shouldLimitMats: Boolean by prefs.refill.shouldLimitMats
    override var limitMats: Int by prefs.refill.limitMats
    override var shouldLimitCEs: Boolean by prefs.refill.shouldLimitCEs
    override var limitCEs: Int by prefs.refill.limitCEs
}