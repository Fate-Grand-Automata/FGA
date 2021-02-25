package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class BattleConfigItemViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    @ApplicationContext context: Context,
    savedState: SavedStateHandle
) : ViewModel() {
    val battleConfigKey: String = savedState[BattleConfigItemSettingsFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    private val prefs = prefsCore.forBattleConfig(battleConfigKey)

    private val skillLevels =
        combine(
            prefs.support.skill1Max.asFlow(),
            prefs.support.skill2Max.asFlow(),
            prefs.support.skill3Max.asFlow()
        ) { s1, s2, s3 -> listOf(s1, s2, s3) }

    val preferredMessage =
        combine(
            prefs.support.preferredServants.asFlow(),
            prefs.support.preferredCEs.asFlow(),
            prefs.support.mlb.asFlow(),
            skillLevels
        ) { servants, ces, mlb, levels ->
            buildString {
                if (servants.isEmpty() && ces.isEmpty()) {
                    appendLine(context.getString(R.string.battle_config_support_any))
                }

                if (servants.isNotEmpty()) {
                    appendLine(servants.joinToString())

                    if (levels.any { it }) {
                        appendLine(levels.joinToString("/", prefix = "[", postfix = "]") {
                            if (it) "10" else "x"
                        })
                    }
                }

                appendLine()

                if (ces.isNotEmpty()) {
                    appendLine(ces.joinToString().let {
                        if (mlb)
                            "$it (☆)"
                        else it
                    })
                }
            }.trim()
        }
}