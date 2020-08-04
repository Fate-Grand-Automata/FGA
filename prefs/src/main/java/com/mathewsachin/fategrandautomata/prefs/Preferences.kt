package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.helpers.SharedPreferenceDelegation
import com.mathewsachin.fategrandautomata.prefs.helpers.map
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.*
import com.mathewsachin.libautomata.IPlatformPrefs
import javax.inject.Inject
import kotlin.time.milliseconds

class PreferencesImpl @Inject constructor(
    private val context: Context,
    val storageDirs: StorageDirs
) : IPreferences {
    private val prefs =
        SharedPreferenceDelegation(
            PreferenceManager.getDefaultSharedPreferences(context),
            context
        )

    override val scriptMode by prefs.enum(R.string.pref_script_mode, ScriptModeEnum.Battle)

    override var gameServer by prefs.enum(R.string.pref_gameserver, GameServerEnum.En)

    override val skillConfirmation by prefs.bool(R.string.pref_skill_conf)

    override var autoSkillList by prefs.stringSet(R.string.pref_autoskill_list)
        private set

    override val autoSkillPreferences
        get() = autoSkillList.map { forAutoSkillConfig(it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    private var selectedAutoSkillConfigKey by prefs.string(R.string.pref_autoskill_selected)

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

    override val castNoblePhantasm by prefs.enum(
        R.string.pref_battle_np,
        BattleNoblePhantasmEnum.None
    )

    override val autoChooseTarget by prefs.bool(R.string.pref_auto_choose_target)

    override val storySkip by prefs.bool(R.string.pref_story_skip)

    override val withdrawEnabled by prefs.bool(R.string.pref_withdraw_enabled)

    override val friendPtsOnly by prefs.bool(R.string.pref_friend_pts, true)

    override val boostItemSelectionMode by prefs.stringAsInt(R.string.pref_boost_item, -1)

    override val refill: IRefillPreferences =
        RefillPreferences(prefs)

    override val ignoreNotchCalculation by prefs.bool(R.string.pref_ignore_notch)

    override val useRootForScreenshots by prefs.bool(R.string.pref_use_root_screenshot)

    override val gudaFinal by prefs.bool(R.string.pref_guda_final)

    override val recordScreen by prefs.bool(R.string.pref_record_screen)

    override val skillDelay by prefs.int(R.string.pref_skill_delay, 500).map { it.milliseconds }

    override fun forAutoSkillConfig(id: String): IAutoSkillPreferences =
        AutoSkillPreferences(
            id,
            context,
            storageDirs
        )

    override fun addAutoSkillConfig(id: String) {
        autoSkillList = autoSkillList
            .toMutableSet()
            .apply { add(id) }
    }

    override fun removeAutoSkillConfig(id: String) {
        context.deleteSharedPreferences(id)

        autoSkillList = autoSkillList
            .toMutableSet()
            .apply { remove(id) }

        if (selectedAutoSkillConfigKey == id) {
            selectedAutoSkillConfigKey = ""
        }
    }

    override val support = object :
        ISupportPreferencesCommon {
        override val mlbSimilarity by prefs.int(R.string.pref_mlb_similarity, 70).map { it / 100.0 }

        override val supportSwipeMultiplier by prefs.int(
            R.string.pref_support_swipe_multiplier,
            100
        )
            .map { it / 100.0 }

        override val swipesPerUpdate by prefs.int(R.string.pref_support_swipes_per_update, 10)

        override val maxUpdates by prefs.int(R.string.pref_support_max_updates, 3)
    }

    override val platformPrefs = object : IPlatformPrefs {
        override val debugMode by prefs.bool(R.string.pref_debug_mode)

        override val minSimilarity by prefs.int(R.string.pref_min_similarity, 80).map { it / 100.0 }

        override val waitMultiplier by prefs.int(R.string.pref_wait_multiplier, 100)
            .map { it / 100.0 }
    }

    override val gestures = object :
        IGesturesPreferences {
        override val clickWaitTime by prefs.int(R.string.pref_click_wait_time, 300)
            .map { it.milliseconds }

        override val clickDuration by prefs.int(R.string.pref_click_duration, 50)
            .map { it.milliseconds }

        override val clickDelay by prefs.int(R.string.pref_click_delay, 10).map { it.milliseconds }

        override val swipeWaitTime by prefs.int(R.string.pref_swipe_wait_time, 700)
            .map { it.milliseconds }

        override val swipeDuration by prefs.int(R.string.pref_swipe_duration, 300)
            .map { it.milliseconds }
    }
}