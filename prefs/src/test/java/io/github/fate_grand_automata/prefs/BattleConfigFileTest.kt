package io.github.fate_grand_automata.prefs

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BattleConfigFileTest {
    private fun roundTrip(values: Map<String, *>) =
        BattleConfigFile.decode(BattleConfigFile.encode(values))

    @Test
    fun `every value type a battle config stores survives a round trip`() {
        val decoded = roundTrip(
            mapOf(
                "autoskill_name" to "Farming",
                "shuffle_cards_wave" to 3,
                "use_servant_priority" to true,
                "battle_config_mat" to setOf("Proof", "Bone")
            )
        )

        assertThat(decoded["autoskill_name"]).isEqualTo("Farming")
        assertThat(decoded["shuffle_cards_wave"]).isEqualTo(3)
        assertThat(decoded["use_servant_priority"]).isEqualTo(true)
        assertThat(decoded["battle_config_mat"] as List<*>).containsOnly("Proof", "Bone")
    }

    /*
     * An int decoded as a double is not an Int, and the import side drops what it cannot type,
     * so a widened number would silently reset the preference to its default.
     */
    @Test
    fun `a whole number comes back as an Int`() {
        assertThat(roundTrip(mapOf("shuffle_cards_wave" to 3))["shuffle_cards_wave"])
            .isNotNull().isInstanceOf(Int::class)
    }

    /*
     * PrefMaker.stringAsInt stores numbers as strings, so JSON quoting is the only thing that
     * tells those apart from real int preferences.
     */
    @Test
    fun `a numeric string stays a String`() {
        assertThat(roundTrip(mapOf("autoskill_party" to "-1"))["autoskill_party"])
            .isNotNull().isInstanceOf(String::class)
    }

    @Test
    fun `a string that reads as a boolean stays a String`() {
        assertThat(roundTrip(mapOf("autoskill_notes" to "true"))["autoskill_notes"])
            .isEqualTo("true")
    }

    @Test
    fun `keys this version does not know are kept`() {
        assertThat(roundTrip(mapOf("some_future_pref" to "value"))["some_future_pref"])
            .isEqualTo("value")
    }

    @Test
    fun `a file that is not a JSON object is rejected`() {
        assertFailsWith<SerializationException> {
            BattleConfigFile.decode("not a battle config")
        }
    }
}
