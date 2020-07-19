package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.libautomata.IPlatformPrefs

interface IPreferences {
    val scriptMode: ScriptModeEnum
    var gameServer: GameServerEnum
    val skillConfirmation: Boolean
    var autoSkillList: Set<String>
    val autoSkillPreferences: List<IAutoSkillPreferences>
    var selectedAutoSkillConfig: IAutoSkillPreferences
    val castNoblePhantasm: BattleNoblePhantasmEnum
    val autoChooseTarget: Boolean
    val storySkip: Boolean
    val withdrawEnabled: Boolean
    val stopAfterBond10: Boolean
    val boostItemSelectionMode: Int
    val refill: IRefillPreferences
    val ignoreNotchCalculation: Boolean
    val useRootForScreenshots: Boolean
    val gudaFinal: Boolean
    val recordScreen: Boolean

    val support: ISupportPreferencesCommon
    val platformPrefs: IPlatformPrefs
    val gestures: IGesturesPreferences

    fun forAutoSkillConfig(id: String): IAutoSkillPreferences
}