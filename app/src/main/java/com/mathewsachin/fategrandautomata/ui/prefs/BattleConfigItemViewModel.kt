package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine

class BattleConfigItemViewModel @ViewModelInject constructor(
    val preferences: IPreferences,
    val prefsCore: PrefsCore,
    @ApplicationContext context: Context,
    @Assisted savedState: SavedStateHandle
) : ViewModel() {
    val battleConfigKey: String = savedState[BattleConfigItemSettingsFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    private val prefs = prefsCore.forBattleConfig(battleConfigKey)

    val cardPriority = prefs
        .cardPriority
        .asFlow()
        .asLiveData()

    val skillCommand = prefs
        .skillCommand
        .asFlow()
        .asLiveData()

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
            .asLiveData()

    val supportSelectionMode = prefs
        .support
        .selectionMode
        .asFlow()
        .asLiveData()
}