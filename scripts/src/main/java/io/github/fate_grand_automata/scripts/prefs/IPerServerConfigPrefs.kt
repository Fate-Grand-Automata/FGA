package io.github.fate_grand_automata.scripts.prefs

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum


interface IPerServerConfigPrefs {
    val server: GameServer
    var selectedAutoSkillKey: String

    var rainbowApple: Int
    var goldApple: Int
    var silverApple: Int
    var blueApple: Int
    var copperApple: Int

    var waitForAPRegen: Boolean
    var sendSupportFriendRequest: Boolean

    var selectedApple: RefillResourceEnum

    var currentAppleCount: Int

    val resources: List<RefillResourceEnum>
    fun updateResources(resources: Set<RefillResourceEnum>)
    var shouldLimitRuns: Boolean
    var limitRuns: Int

    var shouldLimitMats: Boolean
    var limitMats: Int

    var shouldLimitCEs: Boolean
    var limitCEs: Int
}