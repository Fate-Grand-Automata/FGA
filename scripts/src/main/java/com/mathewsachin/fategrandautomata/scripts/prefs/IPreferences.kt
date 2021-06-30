package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.libautomata.IPlatformPrefs
import kotlin.time.Duration

interface IPreferences {
    var scriptMode: ScriptModeEnum
    var gameServer: GameServerEnum
    val skillConfirmation: Boolean
    val battleConfigs: List<IBattleConfig>
    var selectedBattleConfig: IBattleConfig
    val storySkip: Boolean
    val withdrawEnabled: Boolean
    val stopOnCEDrop: Boolean
    val stopOnCEGet: Boolean
    val boostItemSelectionMode: Int
    val refill: IRefillPreferences
    val waitAPRegen: Boolean
    val waitAPRegenMinutes: Int
    val ignoreNotchCalculation: Boolean
    val useRootForScreenshots: Boolean
    val gudaFinal: Boolean
    val recordScreen: Boolean
    val skillDelay: Duration
    val screenshotDrops: Boolean
    var maxGoldEmberSetSize: Int

    val stageCounterSimilarity: Double
    val waitBeforeTurn: Duration
    val waitBeforeCards: Duration

    val support: ISupportPreferencesCommon
    val platformPrefs: IPlatformPrefs
    val gestures: IGesturesPreferences

    fun forBattleConfig(id: String): IBattleConfig
    fun addBattleConfig(id: String): IBattleConfig
    fun removeBattleConfig(id: String)
}