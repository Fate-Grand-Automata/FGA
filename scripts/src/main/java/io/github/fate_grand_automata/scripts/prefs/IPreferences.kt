package io.github.fate_grand_automata.scripts.prefs

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.ScriptModeEnum
import io.github.lib_automata.PlatformPrefs
import kotlin.time.Duration

interface IPreferences {
    var scriptMode: ScriptModeEnum
    var gameServer: GameServer
    val skillConfirmation: Boolean
    val battleConfigs: List<IBattleConfig>
    var selectedBattleConfig: IBattleConfig
    val storySkip: Boolean
    val withdrawEnabled: Boolean
    val stopOnCEGet: Boolean
    val stopOnFirstClearRewards: Boolean
    val boostItemSelectionMode: Int
    val refill: IRefillPreferences
    var waitAPRegen: Boolean
    val useRootForScreenshots: Boolean
    val recordScreen: Boolean
    val skillDelay: Duration
    val screenshotDrops: Boolean
    val screenshotDropsUnmodified: Boolean
    var maxGoldEmberSetSize: Int
    var stopAfterThisRun: Boolean
    val skipServantFaceCardCheck: Boolean

    var shouldLimitFP: Boolean
    var limitFP: Int
    var receiveEmbersWhenGiftBoxFull: Boolean

    val stageCounterSimilarity: Double
    val stageCounterNew: Boolean
    val waitBeforeTurn: Duration
    val waitBeforeCards: Duration

    val support: ISupportPreferencesCommon
    val platformPrefs: PlatformPrefs
    val gestures: IGesturesPreferences

    var ceBombTargetRarity: Int

    fun forBattleConfig(id: String): IBattleConfig
    fun addBattleConfig(id: String): IBattleConfig
    fun removeBattleConfig(id: String)
    fun isOnboardingRequired(): Boolean
    fun completedOnboarding()
}

val IPreferences.wantsMediaProjectionToken get() = !useRootForScreenshots