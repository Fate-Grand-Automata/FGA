package io.github.fate_grand_automata.scripts.models.battle

import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum

class ChainPriorityPerWave private constructor(
    private val chainsPerWave: List<List<ChainTypeEnum>>
) : List<List<ChainTypeEnum>> by chainsPerWave {
    fun atWave(wave: Int) =
        chainsPerWave[wave.coerceIn(chainsPerWave.indices)]

    override fun toString() =
        chainsPerWave
            .joinToString(STAGE_SEPARATOR) {
                it.joinToString(SEPARATOR) { c -> c.name }
            }

    companion object {
        val default = from(listOf(ChainTypeEnum.defaultOrder))
        private const val STAGE_SEPARATOR = "\n"
        private const val SEPARATOR = ","

        fun from(chainsPerWave: List<List<ChainTypeEnum>>) =
            ChainPriorityPerWave(chainsPerWave)

        fun of(priority: String): ChainPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                ChainPriorityPerWave(
                    priority
                        .split(STAGE_SEPARATOR)
                        .map { priorityOnAWave ->
                            priorityOnAWave
                                .split(SEPARATOR)
                                .map {
                                    ChainTypeEnum.valueOf(it)
                                }
                                .toSet()
                                .plusElement(ChainTypeEnum.None)
                                .toList()
                        }
                )
            }
    }
}