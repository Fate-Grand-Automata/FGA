package io.github.fate_grand_automata.atlas

import io.github.fate_grand_automata.scripts.enums.GameServer
import kotlinx.serialization.Serializable

/** The region whose servant names are the English ones the shipped support images use. */
internal const val ATLAS_NA_REGION = "NA"

/** Where the game data originates; servants reach the other regions from here. */
internal const val ATLAS_JP_REGION = "JP"

/**
 * The region codes of the Atlas Academy API (https://apps.atlasacademy.io/db).
 *
 * They follow the game region and not the app language: a user playing on JP with the app in
 * English still sees JP servant names in the support list, so that's what has to be searched.
 */
val GameServer.atlasRegion: String
    get() = when (this) {
        is GameServer.En -> ATLAS_NA_REGION
        is GameServer.Jp -> ATLAS_JP_REGION
        GameServer.Cn -> "CN"
        GameServer.Tw -> "TW"
        GameServer.Kr -> "KR"
    }

/** One entry of the `/basic/<region>/servant/search` endpoint. */
@Serializable
data class AtlasServant(
    val id: Int,
    val name: String,
    val className: String = "",
    val rarity: Int = 0,
    val type: String = "",
    /** The base face art, from which [finalAscensionFace] derives the final ascension's. */
    val face: String = ""
) {
    /**
     * Whether the support list can offer this servant at all.
     *
     * The search endpoint also returns enemies and NPCs (Solomon, the beast bosses), which carry
     * `enemyCollectionDetail` and are never selectable as a support. `flag` can't be used here:
     * regular servants are flagged with things like `ignoreCombineLimitSpecial` too.
     */
    val isSelectable: Boolean
        get() = type in SELECTABLE_TYPES

    companion object {
        private val SELECTABLE_TYPES = setOf("normal", "heroine")
    }
}

/**
 * Makes a servant name usable as a support image name.
 *
 * The name becomes a folder in the user's storage, which is often a FAT-formatted SD card that
 * rejects several of these characters outright. `/` additionally separates the folder from the
 * file name in [io.github.fate_grand_automata.IStorageProvider.writeSupportImage].
 */
internal fun String.toSupportImageName(): String =
    map { if (it in ILLEGAL_NAME_CHARS || it.isISOControl()) '-' else it }
        .joinToString("")
        .trim(' ', '.')
        .take(MAX_NAME_LENGTH)
        .ifEmpty { "Servant" }

private val ILLEGAL_NAME_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

/** Well above any in-game name, but storage providers dislike unbounded file names. */
private const val MAX_NAME_LENGTH = 100
