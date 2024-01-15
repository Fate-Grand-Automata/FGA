package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.scripts.prefs.ICraftEssencePreferences
import io.github.fate_grand_automata.scripts.prefs.IFriendGachaPreferences
import io.github.fate_grand_automata.scripts.prefs.IGesturesPreferences
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.scripts.prefs.IServantEnhancementPreferences
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferencesCommon
import io.github.lib_automata.PlatformPrefs
import io.github.lib_automata.Region
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PreferencesImpl @Inject constructor(
    val prefs: PrefsCore,
) : IPreferences {
    override var scriptMode by prefs.scriptMode

    override var gameServer = GameServer.default

    private var battleConfigList by prefs.battleConfigList

    override val battleConfigs
        get() = battleConfigList.map { forBattleConfig(it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    override var showGameServers: List<GameServer> by prefs.showGameServer

    private var lastPerServerConfigPref: IPerServerConfigPrefs? = null

    override var selectedServerConfigPref: IPerServerConfigPrefs
        get() {
            val serverPrefConfig = lastPerServerConfigPref
                ?.takeIf { it.server.simple == gameServer.simple }
                ?: getPerServerConfigPref(gameServer)
            lastPerServerConfigPref = serverPrefConfig
            return serverPrefConfig
        }
        set(value) {
            lastPerServerConfigPref = value

        }

    private var lastConfig: IBattleConfig? = null

    override var selectedBattleConfig: IBattleConfig
        get() {
            val config = lastConfig.let {
                val currentSelectedKey = selectedServerConfigPref.selectedAutoSkillKey

                if (it != null && it.id == currentSelectedKey) {
                    it
                } else forBattleConfig(currentSelectedKey)
            }

            lastConfig = config
            return config
        }
        set(value) {
            lastConfig = value
            selectedServerConfigPref.selectedAutoSkillKey = value.id
        }

    override val storySkip by prefs.storySkip

    override val withdrawEnabled by prefs.withdrawEnabled

    override val stopOnCEGet by prefs.stopOnCEGet

    override val stopOnFirstClearRewards by prefs.stopOnFirstClearRewards

    override val boostItemSelectionMode by prefs.boostItemSelectionMode

    override val useRootForScreenshots by prefs.useRootForScreenshots

    override val recordScreen by prefs.recordScreen

    override val skillDelay by prefs.skillDelay.map { it.milliseconds }

    override val screenshotDrops by prefs.screenshotDrops

    override val screenshotDropsUnmodified by prefs.screenshotDropsUnmodified

    override val screenshotBond by prefs.screenshotBond

    override var hidePlayButton by prefs.hidePlayButton

    override val stageCounterSimilarity by prefs.stageCounterSimilarity.map { it / 100.0 }

    override val stageCounterNew by prefs.stageCounterNew

    override val waitBeforeTurn by prefs.waitBeforeTurn.map { it.milliseconds }

    override val waitBeforeCards by prefs.waitBeforeCards.map { it.milliseconds }

    override var maxGoldEmberStackSize by prefs.maxGoldEmberSetSize

    override var maxGoldEmberTotalCount by prefs.maxGoldEmberTotalCount

    override var stopAfterThisRun by prefs.stopAfterThisRun

    override var skipServantFaceCardCheck by prefs.skipServantFaceCardCheck

    override var treatSupportLikeOwnServant by prefs.treatSupportLikeOwnServant

    override var receiveEmbersWhenGiftBoxFull by prefs.receiveEmbersWhenGiftBoxFull

    override val craftEssence: ICraftEssencePreferences =
        CraftEssencePrefs(prefs.craftEssence)

    override val friendGacha: IFriendGachaPreferences =
        FriendGachaPrefs(prefs.friendGacha)

    override val playButtonRegion: Region by prefs.playButtonRegion

    private val autoSkillMap = mutableMapOf<String, IBattleConfig>()

    override val servant: IServantEnhancementPreferences =
        ServantEnhancementPrefs(prefs.servantEnhancement)

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


        serverPrefsMap.values.forEach {
            if (it.selectedAutoSkillKey == id) {
                it.selectedAutoSkillKey = ""
            }
        }
    }

    private val serverPrefsMap = mutableMapOf<String, IPerServerConfigPrefs>()

    override fun getPerServerConfigPref(server: GameServer): IPerServerConfigPrefs =
        serverPrefsMap.getOrPut(server.simple) {
            PerServerConfigPrefs(
                GameServer.deserialize(server.simple)!!,
                prefs
            )
        }

    override fun addPerServerConfigPref(server: GameServer): IPerServerConfigPrefs {
        return getPerServerConfigPref(server)
    }

    override fun isOnboardingRequired(): Boolean =
        prefs.onboardingCompletedVersion.get() < PrefsCore.CURRENT_ONBOARDING_VERSION

    override fun completedOnboarding() =
        prefs.onboardingCompletedVersion.set(PrefsCore.CURRENT_ONBOARDING_VERSION)

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

        override val longPressDuration by prefs.longPressDuration
            .map { it.milliseconds }
        override val dragDuration by prefs.dragDuration
            .map { it.milliseconds }
    }
}