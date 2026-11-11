package io.github.fate_grand_automata.atlas

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.github.fate_grand_automata.scripts.enums.GameServer
import kotlin.test.Test

class AtlasServantTest {
    @Test
    fun `support image names lose the characters a file name cannot hold`() {
        assertThat("Archetype: EARTH".toSupportImageName()).isEqualTo("Archetype- EARTH")
        assertThat("Nero/asc1".toSupportImageName()).isEqualTo("Nero-asc1")
        assertThat("Koyanskaya\\of the Light".toSupportImageName())
            .isEqualTo("Koyanskaya-of the Light")
        assertThat("Jeanne\u0000Alter".toSupportImageName()).isEqualTo("Jeanne-Alter")
    }

    @Test
    fun `support image names are trimmed and never empty`() {
        assertThat("  Merlin  ".toSupportImageName()).isEqualTo("Merlin")
        assertThat("".toSupportImageName()).isEqualTo("Servant")
        assertThat("...".toSupportImageName()).isEqualTo("Servant")
    }

    @Test
    fun `regions follow the game server and not the app language`() {
        assertThat(GameServer.En.Original.atlasRegion).isEqualTo("NA")
        assertThat(GameServer.En.BetterFGO.atlasRegion).isEqualTo("NA")
        assertThat(GameServer.Jp.Original.atlasRegion).isEqualTo("JP")
        assertThat(GameServer.Jp.BetterFGO.atlasRegion).isEqualTo("JP")
        assertThat(GameServer.Cn.atlasRegion).isEqualTo("CN")
        assertThat(GameServer.Tw.atlasRegion).isEqualTo("TW")
        assertThat(GameServer.Kr.atlasRegion).isEqualTo("KR")
    }

    @Test
    fun `only servants a support list can offer are selectable`() {
        assertThat(AtlasServant(id = 1, name = "Waver", type = "normal").isSelectable).isTrue()
        assertThat(AtlasServant(id = 2, name = "Mash", type = "heroine").isSelectable).isTrue()
        assertThat(AtlasServant(id = 3, name = "Goetia", type = "enemyCollectionDetail").isSelectable)
            .isFalse()
        assertThat(AtlasServant(id = 4, name = "Nobody").isSelectable).isFalse()
    }
}
