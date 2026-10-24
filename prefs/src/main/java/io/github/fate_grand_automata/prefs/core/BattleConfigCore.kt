package io.github.fate_grand_automata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.fredporciuncula.flow.preferences.Serializer
import io.github.fate_grand_automata.prefs.import
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.GameServers
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.ShuffleCardsEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantSpamConfig
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/*
 * A config exported from a newer version has to stay loadable here, and one exported from here
 * loadable there: unknown keys are skipped, and every field is written out even at its default.
 */
private val spamJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

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

    val braveChains = maker.serialized(
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

    // Keep the legacy storage keys so installed preview builds retain their settings.
    val useCardSequence = maker.bool("use_mighty_chain")

    val npCardTypes = maker.serialized(
        "mighty_chain_np_card_types",
        serializer = object : Serializer<List<CardTypeEnum>> {
            override fun deserialize(serialized: String) =
                serialized.split(",").map {
                    runCatching { enumValueOf<CardTypeEnum>(it) }
                        .getOrDefault(CardTypeEnum.Unknown)
                }.let { values -> List(3) { values.getOrElse(it) { CardTypeEnum.Unknown } } }

            override fun serialize(value: List<CardTypeEnum>) =
                List(3) { value.getOrElse(it) { CardTypeEnum.Unknown } }
                    .joinToString(",") { it.name }
        },
        default = List(3) { CardTypeEnum.Unknown }
    )

    val cardSequenceOrders = maker.serialized(
        "mighty_chain_card_orders",
        serializer = object : Serializer<List<List<CardTypeEnum>>> {
            private val defaultOrder = listOf(
                CardTypeEnum.Buster,
                CardTypeEnum.Arts,
                CardTypeEnum.Quick
            )

            override fun deserialize(serialized: String) =
                serialized.split(";").map { wave ->
                    wave.split(",")
                        .mapNotNull { runCatching { enumValueOf<CardTypeEnum>(it) }.getOrNull() }
                        .filter { it in defaultOrder }
                        .let { parsed -> List(3) { parsed.getOrElse(it) { defaultOrder[it] } } }
                }.let { waves -> List(3) { waves.getOrElse(it) { defaultOrder } } }

            override fun serialize(value: List<List<CardTypeEnum>>) =
                List(3) { wave ->
                    val parsed = value.getOrElse(wave) { defaultOrder }
                    List(3) { index ->
                        parsed.getOrElse(index) { defaultOrder[index] }
                            .takeIf { it in defaultOrder }
                            ?: defaultOrder[index]
                    }
                        .joinToString(",") { it.name }
                }.joinToString(";")
        },
        default = List(3) {
            listOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Quick)
        }
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

    private val defaultSpamConfig = (1..6).map { ServantSpamConfig() }

    val spam = maker.serialized(
        "spam_x",
        serializer = object : Serializer<List<ServantSpamConfig>> {
            override fun deserialize(serialized: String) =
                try {
                    spamJson.decodeFromString<List<ServantSpamConfig>>(serialized)
                } catch (e: SerializationException) {
                    defaultSpamConfig
                }

            override fun serialize(value: List<ServantSpamConfig>) =
                spamJson.encodeToString(value)
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
                    GameServers.deserialize(serialized)?.let { Server.Set(it) } ?: Server.NotSet
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

    val raidTurnDelaySeconds = maker.stringAsInt("raid_delay_seconds", 3)
}
