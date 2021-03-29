package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BattleConfigItemViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val prefs: BattleConfigCore
) : ViewModel() {

    val cardPriority =
        prefs.cardPriority
            .asFlow()
            .map {
                CardPriorityPerWave.of(it)
            }

    val skillCommand =
        prefs.skillCommand
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
            prefs.support.skill1Max.asFlow(),
            prefs.support.skill2Max.asFlow(),
            prefs.support.skill3Max.asFlow()
        ) { s1, s2, s3 -> listOf(s1, s2, s3) }
            .map { skills ->
                skills.joinToString("/") {
                    if (it) "10" else "x"
                }
            }

    fun export(stream: OutputStream) {
        val values = prefs.export()
        val gson = Gson()
        val json = gson.toJson(values)

        stream.writer().use { it.write(json) }
    }
}