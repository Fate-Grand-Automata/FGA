package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsCore @Inject constructor(
    maker: PrefMaker,
    @ApplicationContext val context: Context
) {
    companion object {
        const val GameServerAutoDetect = "auto_detect"
    }

    val scriptMode = maker.enum("script_mode", ScriptModeEnum.Battle)

    val gameServerRaw = maker.string(
        "game_server",
        GameServerAutoDetect
    )

    val skillConfirmation = maker.bool("skill_conf")

    val battleConfigList = maker.stringSet("autoskill_list")
    val selectedAutoSkillConfig = maker.string("autoskill_selected")

    val storySkip = maker.bool("story_skip")
    val withdrawEnabled = maker.bool("withdraw_enabled")

    val stopOnCEDrop = maker.bool("stop_on_ce_drop")
    val stopOnCEGet = maker.bool("stop_on_ce_get")

    val boostItemSelectionMode = maker.stringAsInt("selected_boost_item", -1)

    val refill = RefillPrefsCore(maker)

    val waitAPRegen = maker.bool("wait_for_ap_regeneration")
    val waitAPRegenMinutes = maker.int("wait_for_ap_regeneration_minutes", 1)

    val gudaFinal = maker.bool("guda_final")

    val ignoreNotchCalculation = maker.bool("ignore_notch")
    val useRootForScreenshots = maker.bool("use_root_screenshot")
    val recordScreen = maker.bool("record_screen")
    val screenshotDrops = maker.bool("screenshot_drops")
    val debugMode = maker.bool("debug_mode")
    val autoStartService = maker.bool("auto_start_service")

    val supportSwipesPerUpdate = maker.int("support_swipes_per_update_x", 10)
    val supportMaxUpdates = maker.int("support_max_updates_x", 5)

    val minSimilarity = maker.int("min_similarity", 80)
    val mlbSimilarity = maker.int("mlb_similarity", 70)
    val stageCounterSimilarity = maker.int("stage_counter_similarity", 85)

    val skillDelay = maker.int("skill_delay", 500)
    val waitMultiplier = maker.int("wait_multiplier", 100)
    val waitBeforeTurn = maker.int("wait_before_turn", 500)
    val waitBeforeCards = maker.int("wait_before_cards", 2000)

    val clickWaitTime = maker.int("click_wait_time", 300)
    val clickDuration = maker.int("click_duration", 50)
    val clickDelay = maker.int("click_delay", 10)

    val swipeWaitTime = maker.int("swipe_wait_time", 700)
    val swipeDuration = maker.int("swipe_duration", 300)
    val swipeMultiplier = maker.int("swipe_multiplier", 100)

    val showTextBoxForSkillCmd = maker.bool("auto_skill_cmd_text")

    val maxGoldEmberSetSize = maker.int("max_gold_ember_set_size", 1)

    var dirRoot = maker.string("dir_root")

    private val battleConfigMap = mutableMapOf<String, BattleConfigCore>()

    fun forBattleConfig(id: String): BattleConfigCore =
        battleConfigMap.getOrPut(id) {
            BattleConfigCore(
                id,
                context
            )
        }

    fun removeBattleConfig(id: String) = battleConfigMap.remove(id)
}