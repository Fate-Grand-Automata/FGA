package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker

class ServantPriorityPerWave private constructor(
    private val scoresPerWave: List<List<ServantTracker.TeamSlot>>
) : List<List<ServantTracker.TeamSlot>> by scoresPerWave {
    fun atWave(wave: Int) =
        scoresPerWave[wave.coerceIn(scoresPerWave.indices)]

    override fun toString() =
        scoresPerWave
            .joinToString(stageSeparator) { it.joinToString(separator) }

    companion object {
        val default = from(listOf(ServantTracker.TeamSlot.list))
        private const val stageSeparator = "\n"
        private const val separator = ","

        fun from(scoresPerWave: List<List<ServantTracker.TeamSlot>>) =
            ServantPriorityPerWave(scoresPerWave)

        fun of(priority: String): ServantPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                ServantPriorityPerWave(
                    priority
                        .split(stageSeparator)
                        .map { priorityOnAWave ->
                            priorityOnAWave
                                .split(separator)
                                .map {
                                    ServantTracker.TeamSlot.list[it.toInt() - 1]
                                }
                        }
                )
            }
    }
}