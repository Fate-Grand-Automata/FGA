package io.github.fate_grand_automata.scripts.models.battle

import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum

/** A chain type in the Chain Priority order. A disabled one keeps its place but is never tried. */
data class ChainPriorityItem(
    val type: ChainTypeEnum,
    val isEnabled: Boolean = true
)

/**
 * The Chain Priority order of each wave. Every wave holds each [ChainTypeEnum] exactly once.
 *
 * Saved as one line per wave, with the chain names in order and `!` in front of disabled ones,
 * e.g. `Mighty,!Buster,Arts,Quick,!Avoid`.
 */
class ChainPriorityPerWave private constructor(
    private val chainsPerWave: List<List<ChainPriorityItem>>
) : List<List<ChainPriorityItem>> by chainsPerWave {
    fun atWave(wave: Int) =
        chainsPerWave[wave.coerceIn(chainsPerWave.indices)]

    /** The chains to try in [wave], in order. */
    fun enabledAtWave(wave: Int) =
        atWave(wave)
            .filter { it.isEnabled }
            .map { it.type }

    override fun toString() =
        chainsPerWave
            .joinToString(STAGE_SEPARATOR) {
                it.joinToString(SEPARATOR) { item ->
                    if (item.isEnabled) item.type.name else DISABLED_PREFIX + item.type.name
                }
            }

    companion object {
        val default = from(
            listOf(
                ChainTypeEnum.entries.map { ChainPriorityItem(it, isEnabled = it in ChainTypeEnum.defaultOrder) }
            )
        )
        private const val STAGE_SEPARATOR = "\n"
        private const val SEPARATOR = ","
        private const val DISABLED_PREFIX = "!"

        fun from(chainsPerWave: List<List<ChainPriorityItem>>) =
            ChainPriorityPerWave(chainsPerWave.map { it.complete() })

        fun of(priority: String): ChainPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                from(
                    priority
                        .split(STAGE_SEPARATOR)
                        .map { parseWave(it) }
                )
            }

        private fun parseWave(wave: String): List<ChainPriorityItem> =
            wave
                .split(SEPARATOR)
                .mapNotNull { name ->
                    // Skip names that are not chain types (e.g. a corrupt or edited config)
                    // instead of crashing while the config loads
                    val type = ChainTypeEnum.entries
                        .firstOrNull { it.name == name.removePrefix(DISABLED_PREFIX) }
                        ?: return@mapNotNull null

                    ChainPriorityItem(type, isEnabled = !name.startsWith(DISABLED_PREFIX))
                }

        /** Keeps the first occurrence of each chain type and adds missing ones at the end, disabled. */
        private fun List<ChainPriorityItem>.complete(): List<ChainPriorityItem> {
            val distinct = distinctBy { it.type }
            val missing = ChainTypeEnum.entries
                .filter { type -> distinct.none { it.type == type } }
                .map { ChainPriorityItem(it, isEnabled = false) }

            return distinct + missing
        }
    }
}
