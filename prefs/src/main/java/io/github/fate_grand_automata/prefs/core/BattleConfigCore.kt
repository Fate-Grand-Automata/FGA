package io.github.fate_grand_automata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.fredporciuncula.flow.preferences.Serializer
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.fate_grand_automata.prefs.import
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.ShuffleCardsEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantSpamConfig

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

    val cardPriority = maker.serialized(
        "card_priority",
        serializer = object : Serializer<CardPriorityPerWave> {
            override fun deserialize(serialized: String) =
                CardPriorityPerWave.of(serialized)

            override fun serialize(value: CardPriorityPerWave) =
                value.toString()
        },
        default = CardPriorityPerWave.default
    )

    val rearrangeCards = maker.serialized(
        "auto_skill_rearrange_cards",
        serializer = object : Serializer<List<Boolean>> {
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
        serializer = object : Serializer<List<BraveChainEnum>> {
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

    val useServantPriority = maker.bool("use_servant_priority")
    val servantPriority = maker.serialized(
        "servant_priority",
        serializer = object : Serializer<ServantPriorityPerWave> {
            override fun deserialize(serialized: String): ServantPriorityPerWave =
                try {
                    ServantPriorityPerWave.of(serialized)
                } catch (e: Exception) {
                    ServantPriorityPerWave.default
                }

            override fun serialize(value: ServantPriorityPerWave) =
                value.toString()
        },
        default = ServantPriorityPerWave.default
    )

    private val gson = Gson()
    private val defaultSpamConfig = (1..6).map { ServantSpamConfig() }

    val spam = maker.serialized(
        "spam_x",
        serializer = object : Serializer<List<ServantSpamConfig>> {
            override fun deserialize(serialized: String) =
                try {
                    gson
                        .fromJson(serialized, Array<ServantSpamConfig>::class.java)
                        ?.toList() ?: defaultSpamConfig
                } catch (e: JsonSyntaxException) {
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

    sealed class Server {
        class Set(val server: GameServer) : Server()
        object NotSet : Server()

        fun asGameServer() = when (this) {
            NotSet -> null
            is Set -> server
        }
    }

    val server = maker.serialized(
        "battle_config_server",
        serializer = object : Serializer<Server> {
            override fun deserialize(serialized: String) =
                try {
                    GameServer.deserialize(serialized)?.let { Server.Set(it) } ?: Server.NotSet
                } catch (e: Exception) {
                    Server.NotSet
                }

            override fun serialize(value: Server) =
                when (value) {
                    Server.NotSet -> ""
                    is Server.Set -> value.server.serialize()
                }
        },
        default = Server.NotSet
    )

    val support = SupportPrefsCore(maker)

    val autoChooseTarget = maker.bool("auto_choose_target")

    val addRaidTurnDelay = maker.bool("add_raid_delay")

    val raidTurnDelaySeconds = maker.stringAsInt("raid_delay_seconds", 2)
}