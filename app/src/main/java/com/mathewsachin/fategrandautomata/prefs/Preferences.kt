package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.util.AutomataApplication
import com.mathewsachin.libautomata.IPlatformPrefs
import kotlin.time.milliseconds

object Preferences {
    private val context: Context = AutomataApplication.Instance
    private val prefs = SharedPreferenceDelegation(
        PreferenceManager.getDefaultSharedPreferences(context),
        context
    )

    init {
        applyDefaults()
    }

    private fun applyDefaults() {
        if (!prefs.prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            val prefFiles = arrayOf(
                R.xml.main_preferences,
                R.xml.app_preferences,
                R.xml.refill_preferences
            )

            for (prefFile in prefFiles) {
                PreferenceManager.setDefaultValues(context, prefFile, true)
            }
        }
    }

    val scriptMode by prefs.enum(R.string.pref_script_mode, ScriptModeEnum.Battle)

    var gameServer by prefs.enum(R.string.pref_gameserver, GameServerEnum.En)

    val skillConfirmation by prefs.bool(R.string.pref_skill_conf)

    var autoSkillList by prefs.stringSet(R.string.pref_autoskill_list)

    val autoSkillPreferences
        get() = autoSkillList.map {
            AutoSkillPreferences(
                it,
                AutomataApplication.Instance
            )
        }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    private var selectedAutoSkillConfigKey by prefs.string(R.string.pref_autoskill_selected)

    private var lastConfig: AutoSkillPreferences? = null

    var selectedAutoSkillConfig: AutoSkillPreferences
        get() {
            val config = lastConfig.let {
                val currentSelectedKey =
                    selectedAutoSkillConfigKey

                if (it != null && it.id == currentSelectedKey) {
                    it
                } else AutoSkillPreferences(
                    currentSelectedKey,
                    context
                )
            }

            lastConfig = config
            return config
        }
        set(value) {
            lastConfig = value
            selectedAutoSkillConfigKey = value.id
        }

    val castNoblePhantasm by prefs.enum(R.string.pref_battle_np, BattleNoblePhantasmEnum.None)

    val autoChooseTarget by prefs.bool(R.string.pref_auto_choose_target)

    val storySkip by prefs.bool(R.string.pref_story_skip)

    val withdrawEnabled by prefs.bool(R.string.pref_withdraw_enabled)

    val stopAfterBond10 by prefs.bool(R.string.pref_stop_bond10)

    val boostItemSelectionMode by prefs.stringAsInt(R.string.pref_boost_item, -1)

    val refill =
        RefillPreferences(prefs)

    val ignoreNotchCalculation by prefs.bool(R.string.pref_ignore_notch)

    val useRootForScreenshots by prefs.bool(R.string.pref_use_root_screenshot)

    val gudaFinal by prefs.bool(R.string.pref_guda_final)

    val recordScreen by prefs.bool(R.string.pref_record_screen)

    object Support {
        val mlbSimilarity by prefs.int(R.string.pref_mlb_similarity, 70).map { it / 100.0 }

        val supportSwipeMultiplier by prefs.int(R.string.pref_support_swipe_multiplier, 100)
            .map { it / 100.0 }

        val swipesPerUpdate by prefs.int(R.string.pref_support_swipes_per_update, 10)

        val maxUpdates by prefs.int(R.string.pref_support_max_updates, 3)
    }

    object PlatformPrefs : IPlatformPrefs {
        override val debugMode by prefs.bool(R.string.pref_debug_mode)

        override val minSimilarity by prefs.int(R.string.pref_min_similarity, 80).map { it / 100.0 }

        override val waitMultiplier by prefs.int(R.string.pref_wait_multiplier, 100)
            .map { it / 100.0 }
    }

    object Gestures {
        val clickWaitTime by prefs.int(R.string.pref_click_wait_time, 300).map { it.milliseconds }

        val clickDuration by prefs.int(R.string.pref_click_duration, 50).map { it.milliseconds }

        val clickDelay by prefs.int(R.string.pref_click_delay, 10).map { it.milliseconds }

        val swipeWaitTime by prefs.int(R.string.pref_swipe_wait_time, 700).map { it.milliseconds }

        val swipeDuration by prefs.int(R.string.pref_swipe_duration, 300).map { it.milliseconds }
    }
}