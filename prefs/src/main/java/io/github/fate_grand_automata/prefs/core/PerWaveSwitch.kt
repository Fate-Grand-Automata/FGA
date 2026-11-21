package io.github.fate_grand_automata.prefs.core

/**
 * A battle config switch with one value per wave.
 *
 * Configs without per-wave values use [allWaves] for every wave. Saving writes only [perWave],
 * which takes precedence from then on.
 */
class PerWaveSwitch(
    private val perWave: Pref<List<Boolean>>,
    private val allWaves: Pref<Boolean>
) {
    fun get(): List<Boolean> =
        perWave.get().ifEmpty { listOf(allWaves.get()) }

    fun set(value: List<Boolean>) = perWave.set(value)
}
