package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.nav

class SkillCmdPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {

    var autoSkillKey = ""

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(::autoSkillKey.name, autoSkillKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments = bundleOf(ARG_KEY to getString(R.string.pref_autoskill_cmd))

        if (savedInstanceState != null) {
            autoSkillKey = savedInstanceState.getString(::autoSkillKey.name, "")
        }

        super.onCreate(savedInstanceState)
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        val actionMsg = getString(R.string.battle_config_item_open_maker)
        builder.setNeutralButton(actionMsg) { _, _ ->
            val action = BattleConfigItemSettingsFragmentDirections
                .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(autoSkillKey)

            nav(action)
        }
    }
}