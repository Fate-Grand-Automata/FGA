package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.prefs.*
import com.mathewsachin.libautomata.IPlatformPrefs
import javax.inject.Inject
import kotlin.time.milliseconds

class PreferencesImpl @Inject constructor(
    val prefs: PrefsCore,
    val storageDirs: StorageDirs
) : IPreferences {
    override val scriptMode by prefs.scriptMode

    override var gameServer by prefs.gameServer

    override val skillConfirmation by prefs.skillConfirmation

    override var autoSkillList by prefs.autoSkillList
        private set

    override val autoSkillPreferences
        get() = autoSkillList.map { forAutoSkillConfig(it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    private var selectedAutoSkillConfigKey by prefs.selectedAutoSkillConfig

    private var lastConfig: IAutoSkillPreferences? = null

    override var selectedAutoSkillConfig: IAutoSkillPreferences
        get() {
            val config = lastConfig.let {
                val currentSelectedKey =
                    selectedAutoSkillConfigKey

                if (it != null && it.id == currentSelectedKey) {
                    it
                } else forAutoSkillConfig(currentSelectedKey)
            }

            lastConfig = config
            return config
        }
        set(value) {
            lastConfig = value
            selectedAutoSkillConfigKey = value.id
        }

    override val castNoblePhantasm by prefs.castNoblePhantasm

    override val autoChooseTarget by prefs.autoChooseTarget

    override val storySkip by prefs.storySkip

    override val withdrawEnabled by prefs.withdrawEnabled

    override val stopOnCEDrop by prefs.stopOnCEDrop

    override val stopOnCEGet by prefs.stopOnCEGet

    override val friendPtsOnly by prefs.friendPtsOnly

    override val boostItemSelectionMode by prefs.boostItemSelectionMode

    override val refill: IRefillPreferences =
        RefillPreferences(prefs.refill)

    override val ignoreNotchCalculation by prefs.ignoreNotchCalculation

    override val useRootForScreenshots by prefs.useRootForScreenshots

    override val gudaFinal by prefs.gudaFinal

    override val recordScreen by prefs.recordScreen

    override val skillDelay by prefs.skillDelay.map { it.milliseconds }

    override val screenshotDrops by prefs.screenshotDrops

    override val canPauseScript by prefs.canPauseScript

    override val stageCounterSimilarity by prefs.stageCounterSimilarity.map { it / 100.0 }

    override val waitBeforeTurn by prefs.waitBeforeTurn.map { it.milliseconds }

    private val autoSkillMap = mutableMapOf<String, IAutoSkillPreferences>()

    override fun forAutoSkillConfig(id: String): IAutoSkillPreferences =
        autoSkillMap.getOrPut(id) {
            AutoSkillPreferences(
                id,
                prefs,
                storageDirs
            )
        }

    override fun addAutoSkillConfig(id: String) {
        autoSkillList = autoSkillList
            .toMutableSet()
            .apply { add(id) }
    }

    override fun removeAutoSkillConfig(id: String) {
        prefs.maker.context.deleteSharedPreferences(id)
        autoSkillMap.remove(id)
        prefs.removeAutoSkillConfig(id)

        autoSkillList = autoSkillList
            .toMutableSet()
            .apply { remove(id) }

        if (selectedAutoSkillConfigKey == id) {
            selectedAutoSkillConfigKey = ""
        }
    }

    override val support = object :
        ISupportPreferencesCommon {
        override val mlbSimilarity by prefs.mlbSimilarity.map { it / 100.0 }

        override val supportSwipeMultiplier by prefs.supportSwipeMultiplier
            .map { it / 100.0 }

        override val swipesPerUpdate by prefs.supportSwipesPerUpdate

        override val maxUpdates by prefs.supportMaxUpdates
    }

    override val platformPrefs = object : IPlatformPrefs {
        override val debugMode by prefs.debugMode

        override val minSimilarity by prefs.minSimilarity.map { it / 100.0 }

        override val waitMultiplier by prefs.waitMultiplier
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