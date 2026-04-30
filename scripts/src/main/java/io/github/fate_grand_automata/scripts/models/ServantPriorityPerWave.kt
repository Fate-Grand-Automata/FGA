package io.github.fate_grand_automata.scripts.models

class ServantPriorityPerWave private constructor(
    private val scoresPerWave: List<List<TeamSlot>>,
) : List<List<TeamSlot>> by scoresPerWave {
    fun atWave(wave: Int) =
        scoresPerWave[wave.coerceIn(scoresPerWave.indices)]

    override fun toString() =
        scoresPerWave
            .joinToString(STAGE_SEPARATOR) {
                it.joinToString(SEPARATOR) { m ->
                    m.position.toString()
                }
            }

    companion object {
        val default = from(listOf(TeamSlot.list))
        private const val STAGE_SEPARATOR = "\n"
        private const val SEPARATOR = ","

        fun from(scoresPerWave: List<List<TeamSlot>>) =
            ServantPriorityPerWave(scoresPerWave)

        fun of(priority: String): ServantPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                ServantPriorityPerWave(
                    priority
                        .split(STAGE_SEPARATOR)
                        .map { priorityOnAWave ->
                            priorityOnAWave
                                .split(SEPARATOR)
                                .map {
                                    TeamSlot.list[it.toInt() - 1]
                                }
                        },
                )
            }
    }
}
