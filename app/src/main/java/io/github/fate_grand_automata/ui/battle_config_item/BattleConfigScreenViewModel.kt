package io.github.fate_grand_automata.ui.battle_config_item

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.OutputStream
import javax.inject.Inject
import kotlin.uuid.Uuid

@HiltViewModel
class BattleConfigScreenViewModel @Inject constructor(
    val prefs: IPreferences,
    val battleConfig: IBattleConfig,
    val battleConfigCore: BattleConfigCore
) : ViewModel() {

    val cardPriority =
        battleConfigCore.cardPriority
            .asFlow()

    val skillCommand =
        battleConfigCore.skillCommand
            .asFlow()
            .map {
                try {
                    SkillMakerModel(it)
                } catch (e: Exception) {
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
                Timber.e(e, "Failed to export")

                val msg = context.getString(R.string.battle_config_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createCopyAndReturnId(context: Context): String {
        val guid = Uuid.random().toString()
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