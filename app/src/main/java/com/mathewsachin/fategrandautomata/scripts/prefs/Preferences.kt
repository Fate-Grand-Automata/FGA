package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.libautomata.IPlatformPrefs
import kotlin.time.milliseconds

object Preferences {
    init {
        applyDefaults()
    }

    val ScriptMode get() = getEnumPref(R.string.pref_script_mode, ScriptModeEnum.Battle)

    val GameServer get() = getEnumPref(R.string.pref_gameserver, GameServerEnum.En)

    val SkillConfirmation get() = getBoolPref(R.string.pref_skill_conf)

    val EnableAutoSkill get() = getBoolPref(R.string.pref_autoskill_enable)

    val SelectedAutoSkillConfig get() = getStringPref(R.string.pref_autoskill_selected)

    val SkillCommand: String get() {
        val prefs = getPrefsForSelectedAutoSkill()
            ?: return ""

        return getStringPref(R.string.pref_autoskill_cmd, "", prefs)
    }

    val BattleCardPriority get() = getStringPref(R.string.pref_card_priority, defaultCardPriority)

    val BattleNoblePhantasm get() = getEnumPref(R.string.pref_battle_np, BattleNoblePhantasmEnum.None)

    val BattleAutoChooseTarget get() = getBoolPref(R.string.pref_auto_choose_target)

    val StorySkip get() = getBoolPref(R.string.pref_story_skip)

    val WithdrawEnabled get() = getBoolPref(R.string.pref_withdraw_enabled)

    val StopAfterBond10 get() = getBoolPref(R.string.pref_stop_bond10)

    val BoostItemSelectionMode get() = getStringAsIntPref(R.string.pref_boost_item, -1)

    val Support = SupportPreferences()

    val Refill = RefillPreferences()

    val IgnoreNotchCalculation get() = getBoolPref(R.string.pref_ignore_notch)

    val UseRootForScreenshots get() = getBoolPref(R.string.pref_use_root_screenshot)

    val UseRootForGestures get() = getBoolPref(R.string.pref_use_root_gestures)

    val GudaFinal get() = getBoolPref(R.string.pref_guda_final)

    val RecordScreen get() = getBoolPref(R.string.pref_record_screen)

    val Party: Int get() {
        val notSet = -1

        val pref = getPrefsForSelectedAutoSkill()
            ?: return notSet

        return getStringAsIntPref(R.string.pref_autoskill_party, notSet, pref)
    }

    object PlatformPrefs: IPlatformPrefs {
        override val debugMode get() = getBoolPref(R.string.pref_debug_mode)

        override val minSimilarity get() = getIntPref(R.string.pref_min_similarity, 80) / 100.0

        override val waitMultiplier get() = getIntPref(R.string.pref_wait_multiplier, 100) / 100.0
    }

    object Gestures {
        val clickWaitTime get() = getIntPref(R.string.pref_click_wait_time, 300).milliseconds

        val clickDuration get() = getIntPref(R.string.pref_click_duration, 50).milliseconds

        val clickDelay get() = getIntPref(R.string.pref_click_delay, 10).milliseconds

        val swipeWaitTime get() = getIntPref(R.string.pref_swipe_wait_time, 700).milliseconds

        val swipeDuration get() = getIntPref(R.string.pref_swipe_duration, 300).milliseconds
    }
}