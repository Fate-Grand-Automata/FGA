package com.mathewsachin.fategrandautomata.scripts.prefs

import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.util.AutomataApplication

class Preferences {
    companion object {
        init {
            applyDefaults()
        }

        val ScriptMode get() = getEnumPref(R.string.pref_script_mode, ScriptModeEnum.Battle)

        var GameServer get() = getEnumPref(R.string.pref_gameserver, GameServerEnum.En)
            set(value) {
                defaultPrefs.edit(commit = true) {
                    putString(AutomataApplication.Instance.getString(R.string.pref_gameserver), value.toString())
                }
            }

        val SkillConfirmation get() = getBoolPref(R.string.pref_skill_conf)

        val SelectedAutoSkillConfig get() = getStringPref(R.string.pref_autoskill_selected)

        val SkillCommand: String get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return ""

            return getStringPref(R.string.pref_autoskill_cmd, "", prefs)
        }

        val CardPriority: String get() {
            val prefs = getPrefsForSelectedAutoSkill()
                ?: return defaultCardPriority

            return getStringPref(R.string.pref_card_priority, defaultCardPriority, prefs)
        }

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

        val DebugMode get() = getBoolPref(R.string.pref_debug_mode)

        val GudaFinal get() = getBoolPref(R.string.pref_guda_final)

        val RecordScreen get() = getBoolPref(R.string.pref_record_screen)

        val Party: Int get() {
            val notSet = -1

            val pref = getPrefsForSelectedAutoSkill()
                ?: return notSet

            return getStringAsIntPref(R.string.pref_autoskill_party, notSet, pref)
        }
    }
}