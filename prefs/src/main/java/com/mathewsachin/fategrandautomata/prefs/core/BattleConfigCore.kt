package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.prefs.import
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.ServantSpamConfig
import com.tfcporciuncula.flow.Serializer

class BattleConfigCore(
    val id: String,
    val context: Context
) {
    val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        id,
        Context.MODE_PRIVATE
    )

    private val maker = PrefMaker(sharedPrefs)

    fun import(map: Map<String, *>) =
        sharedPrefs.edit {
            import(map)
        }

    fun export(): Map<String, *> = sharedPrefs.all

    val name = maker.string("autoskill_name", "--")
    val skillCommand = maker.string("autoskill_cmd")
    val notes = maker.string("autoskill_notes")

    val cardPriority = maker.string("card_priority", defaultCardPriority)

    val rearrangeCards = maker.serialized(
        "auto_skill_rearrange_cards",
        serializer = object: Serializer<List<Boolean>> {
            private val separator = ","
            private val Yes = "T"
            private val No = "F"

            override fun deserialize(serialized: String) =
                serialized
                    .split(separator)
                    .map { m -> m == Yes }

            override fun serialize(value: List<Boolean>) =
                value
                    .joinToString(separator) { m -> if (m) Yes else No }
        },
        default = emptyList()
    )

    var braveChains = maker.serialized(
        "auto_skill_brave_chains",
        serializer = object: Serializer<List<BraveChainEnum>> {
            private val separator = ","

            override fun deserialize(serialized: String) =
                serialized
                    .split(separator)
                    .map { m ->
                        try {
                            enumValueOf(m)
                        } catch (e: Exception) {
                            BraveChainEnum.None
                        }
                    }

            override fun serialize(value: List<BraveChainEnum>) =
                value
                    .joinToString(separator) { m -> m.toString() }
        },
        default = emptyList()
    )

    val shuffleCards = maker.enum("shuffle_cards", ShuffleCardsEnum.None)
    val shuffleCardsWave = maker.int("shuffle_cards_wave", 3)

    private val gson = Gson()
    private val defaultSpamConfig = (1..6).map { ServantSpamConfig() }

    val spam = maker.serialized(
        "spam_x",
        serializer = object: Serializer<List<ServantSpamConfig>> {
            override fun deserialize(serialized: String) =
                try {
                    gson
                        .fromJson(serialized, Array<ServantSpamConfig>::class.java)
                        ?.toList() ?: defaultSpamConfig
                }
                catch (e: JsonSyntaxException) {
                    defaultSpamConfig
                }

            override fun serialize(value: List<ServantSpamConfig>) =
                gson.toJson(value)
        },
        defaultSpamConfig
    )

    val party = maker.stringAsInt("autoskill_party", -1)
    val materials = maker.stringSet("battle_config_mat").map(
        defaultValue = emptySet(),
        convert = {
            it
                .mapNotNull { mat ->
                    try {
                        enumValueOf<MaterialEnum>(mat)
                    } catch (e: Exception) {
                        null
                    }
                }
                .toSet()
        },
        reverse = {
            it
                .map { m -> m.name }
                .toSet()
        }
    )

    val support = SupportPrefsCore(maker)

    val autoChooseTarget = maker.bool("auto_choose_target")
}