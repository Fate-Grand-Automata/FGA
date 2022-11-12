package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.*
import com.mathewsachin.libautomata.PlatformPrefs
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PreferencesImpl @Inject constructor(
    val prefs: PrefsCore,
) : IPreferences {
    override var scriptMode by prefs.scriptMode

    override var gameServer = GameServerEnum.En

    override val skillConfirmation by prefs.skillConfirmation

    private var battleConfigList by prefs.battleConfigList

    override val battleConfigs
        get() = battleConfigList.map { forBattleConfig(it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    private var selectedAutoSkillConfigKey by prefs.selectedAutoSkillConfig

    private var lastConfig: IBattleConfig? = null

    override var selectedBattleConfig: IBattleConfig
        get() {
            val config = lastConfig.let {
                val currentSelectedKey =
                    selectedAutoSkillConfigKey

                if (it != null && it.id == currentSelectedKey) {
                    it
                } else forBattleConfig(currentSelectedKey)
            }

            lastConfig = config
            return config
        }
        set(value) {
            lastConfig = value
            selectedAutoSkillConfigKey = value.id
        }

    override val storySkip by prefs.storySkip

    override val withdrawEnabled by prefs.withdrawEnabled

    override val stopOnCEGet by prefs.stopOnCEGet

    override val stopOnFirstClearRewards by prefs.stopOnFirstClearRewards

    override val boostItemSelectionMode by prefs.boostItemSelectionMode

    override val refill: IRefillPreferences =
        RefillPreferences(prefs.refill)

    override var waitAPRegen by prefs.waitAPRegen

    override val ignoreNotchCalculation by prefs.ignoreNotchCalculation

    override val useRootForScreenshots by prefs.useRootForScreenshots

    override val skillDelay by prefs.skillDelay.map { it.milliseconds }

    override val screenshotDrops by prefs.screenshotDrops

    override val screenshotDropsUnmodified by prefs.screenshotDropsUnmodified

    override val stageCounterSimilarity by prefs.stageCounterSimilarity.map { it / 100.0 }

    override val stageCounterNew by prefs.stageCounterNew

    override val waitBeforeTurn by prefs.waitBeforeTurn.map { it.milliseconds }

    override val waitBeforeCards by prefs.waitBeforeCards.map { it.milliseconds }

    override var maxGoldEmberSetSize by prefs.maxGoldEmberSetSize

    override var ceBombTargetRarity by prefs.ceBombTargetRarity

    override var stopAfterThisRun by prefs.stopAfterThisRun

    override var skipServantFaceCardCheck by prefs.skipServantFaceCardCheck

    override var shouldLimitFP by prefs.shouldLimitFP
    override var limitFP by prefs.limitFP

    override var preventLotteryBoxReset by prefs.preventLotteryBoxReset

    override var receiveEmbersWhenGiftBoxFull by prefs.receiveEmbersWhenGiftBoxFull

    private val autoSkillMap = mutableMapOf<String, IBattleConfig>()

    override fun forBattleConfig(id: String): IBattleConfig =
        autoSkillMap.getOrPut(id) {
            BattleConfig(
                id,
                prefs
            )
        }

    override fun addBattleConfig(id: String): IBattleConfig {
        battleConfigList = battleConfigList
            .toMutableSet()
            .apply { add(id) }

        return forBattleConfig(id)
    }

    override fun removeBattleConfig(id: String) {
        prefs.context.deleteSharedPreferences(id)
        autoSkillMap.remove(id)
        prefs.removeBattleConfig(id)

        battleConfigList = battleConfigList
            .toMutableSet()
            .apply { remove(id) }

        if (selectedAutoSkillConfigKey == id) {
            selectedAutoSkillConfigKey = ""
        }
    }

    override val support = object :
        ISupportPreferencesCommon {
        override val mlbSimilarity by prefs.mlbSimilarity.map { it / 100.0 }

        override val swipesPerUpdate by prefs.supportSwipesPerUpdate

        override val maxUpdates by prefs.supportMaxUpdates
    }

    override val platformPrefs = object : PlatformPrefs {
        override val debugMode by prefs.debugMode

        override val minSimilarity by prefs.minSimilarity.map { it / 100.0 }

        override val waitMultiplier by prefs.waitMultiplier
            .map { it / 100.0 }

        override val swipeMultiplier by prefs.swipeMultiplier
            .map { it / 100.0 }
    }

    override val gestures = object :
        IGesturesPreferences {
        override val clickWaitTime by prefs.clickWaitTime
            .map { it.milliseconds }

        override val clickDuration by prefs.clickDuration
            .map { it.milliseconds }

        override val clickDelay by prefs.clickDelay.map { it.milliseconds }

        override val swipeWaitTime by prefs.swipeWaitTime
            .map { it.milliseconds }

        override val swipeDuration by prefs.swipeDuration
            .map { it.milliseconds }
    }
}