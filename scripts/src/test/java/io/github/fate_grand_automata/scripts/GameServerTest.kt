package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.GameServers
import kotlin.test.Test

class GameServerTest {
    @Test
    fun testSerializeDeserialize() {
        GameServers.values.forEach {
            assertThat(GameServers.deserialize(it.serialize())).isEqualTo(it)
        }
    }
}