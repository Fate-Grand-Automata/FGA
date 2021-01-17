package com.mathewsachin.fategrandautomata.prefs.core

import com.mathewsachin.fategrandautomata.prefs.R
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsCore @Inject constructor(
    val maker: PrefMaker
) {
    val scriptMode = maker.enum(
        R.string.pref_script_mode,
        ScriptModeEnum.Battle
    )

    val gameServerRaw = maker.string(
        R.string.pref_game_server,
        maker.context.getString(R.string.pref_game_server_auto_detect)
    )

    val skillConfirmation = maker.bool(R.string.pref_skill_conf)

    val battleConfigList = maker.stringSet(R.string.pref_battle_config_list)

    val selectedAutoSkillConfig = maker.string(R.string.pref_battle_config_selected)

    val storySkip = maker.bool(R.string.pref_story_skip)

    val withdrawEnabled = maker.bool(R.string.pref_withdraw_enabled)

    val stopOnCEDrop = maker.bool(R.string.pref_stop_on_ce_drop)

    val stopOnCEGet = maker.bool(R.string.pref_stop_on_ce_get)

    val boostItemSelectionMode = maker.stringAsInt(R.string.pref_boost_item, -1)

    val refill = RefillPrefsCore(maker)

    val waitAPRegen = maker.bool(R.string.pref_wait_ap_regen)

    val waitAPRegenMinutes = maker.int(R.string.pref_wait_ap_regen_minutes)

    val ignoreNotchCalculation = maker.bool(R.string.pref_ignore_notch)

    val useRootForScreenshots = maker.bool(R.string.pref_use_root_screenshot)

    val gudaFinal = maker.bool(R.string.pref_guda_final)

    val recordScreen = maker.bool(R.string.pref_record_screen)

    val screenshotDrops = maker.bool(R.string.pref_screenshot_drops)

    val supportSwipesPerUpdate = maker.int("support_swipes_per_update_x", 10)
    val supportMaxUpdates = maker.int("support_max_updates_x", 5)

    val debugMode = maker.bool(R.string.pref_debug_mode)

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

    val autoStartService = maker.bool(R.string.pref_auto_start_service)

    val showTextBoxForSkillCmd = maker.bool(R.string.pref_battle_config_cmd_text)

    val maxGoldEmberSetSize = maker.int(R.string.pref_max_gold_ember_set_size, 1)

    var dirRoot = maker.string(R.string.pref_dir)

    private val battleConfigMap = mutableMapOf<String, BattleConfigCore>()

    fun forBattleConfig(id: String): BattleConfigCore =
        battleConfigMap.getOrPut(id) {
            BattleConfigCore(
                id,
                maker.context
            )
        }

    fun removeBattleConfig(id: String) = battleConfigMap.remove(id)
}