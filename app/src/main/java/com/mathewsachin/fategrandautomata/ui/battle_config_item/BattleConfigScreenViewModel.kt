package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import timber.log.error
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BattleConfigScreenViewModel @Inject constructor(
    val prefs: IPreferences,
    val battleConfig: IBattleConfig,
    val prefsCore: PrefsCore,
    val battleConfigCore: BattleConfigCore
) : ViewModel() {

    val cardPriority =
        battleConfigCore.cardPriority
            .asFlow()
            .map {
                CardPriorityPerWave.of(it)
            }

    val skillCommand =
        battleConfigCore.skillCommand
            .asFlow()
            .map {
                try {
                    SkillMakerModel(it)
                }
                catch (e: Exception) {
                    SkillMakerModel("")
                }
                    .skillCommand
                    .drop(1)
            }

    val maxSkillText =
        combine(
            battleConfigCore.support.skill1Max.asFlow(),
            battleConfigCore.support.skill2Max.asFlow(),
            battleConfigCore.support.skill3Max.asFlow()
        ) { s1, s2, s3 -> listOf(s1, s2, s3) }
            .map { skills ->
                skills.joinToString("/") {
                    if (it) "10" else "x"
                }
            }

    private fun export(stream: OutputStream) {
        val values = battleConfig.export()
        val gson = Gson()
        val json = gson.toJson(values)

        stream.writer().use { it.write(json) }
    }

    fun export(context: Context, uri: Uri?) {
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    export(outStream)
                }
            } catch (e: Exception) {
                Timber.error(e) { "Failed to export" }

                val msg = context.getString(R.string.battle_config_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createCopyAndReturnId(context: Context): String {
        val guid = UUID.randomUUID().toString()
        prefs.addBattleConfig(guid)
        val newConfig = prefs.forBattleConfig(guid)

        val map = battleConfig.export()
        newConfig.import(map)
        newConfig.name = context.getString(R.string.battle_config_item_copy_name, newConfig.name)

        return guid
    }

    fun delete() {
        prefs.removeBattleConfig(battleConfig.id)
    }
}