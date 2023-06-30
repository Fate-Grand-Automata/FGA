package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.GameServer
import kotlin.test.Test

class GameServerTest {
    @Test
    fun testSerializeDeserialize() {
        GameServer.values.forEach {
            assertThat(GameServer.deserialize(it.serialize())).isEqualTo(it)
        }
    }
}