package io.github.fate_grand_automata.scripts.locations

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.isWide
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportScreenLocationsTest {
    private class FakeScriptAreaTransforms(
        override val scriptArea: Region
    ) : IScriptAreaTransforms {
        override val isWide = scriptArea.size.isWide()
        override val gameServer = GameServer.Kr
        override val canLongSwipe = true

        override fun Location.xFromCenter() =
            this + Location(scriptArea.center.x, 0)

        override fun Region.xFromCenter() =
            this + Location(scriptArea.center.x, 0)

        override fun Location.xFromRight() =
            this + Location(scriptArea.right, 0)

        override fun Region.xFromRight() =
            this + Location(scriptArea.right, 0)

        override fun Location.yFromBottom() =
            this + Location(0, scriptArea.bottom)

        override fun Region.yFromBottom() =
            this + Location(0, scriptArea.bottom)
    }

    private fun locations(scriptWidth: Int) =
        SupportScreenLocations(
            FakeScriptAreaTransforms(
                Region(0, 0, scriptWidth, 1440)
            )
        )

    @Test
    fun `skill display toggle follows the support header across aspect ratios`() {
        val expectedXByScriptWidth = mapOf(
            2560 to 1700, // 16:9 game area
            2880 to 1700, // exactly 18:9 is not treated as wide
            3088 to 1871, // Galaxy S23 Ultra
            3120 to 1871, // 19.5:9
            3200 to 1871, // 20:9
            3360 to 1871  // 21:9 / cropped ultrawide game area
        )

        expectedXByScriptWidth.forEach { (scriptWidth, expectedX) ->
            val support = locations(scriptWidth)

            assertThat(support.skillDisplayToggle.x).isEqualTo(expectedX)
            assertThat(support.skillDisplayToggle.y).isEqualTo(250)
        }
    }

    @Test
    fun `skill display toggle never overlaps refresh and stays on screen`() {
        listOf(2560, 2880, 3088, 3120, 3200, 3360).forEach { scriptWidth ->
            val support = locations(scriptWidth)
            val toggle = support.skillDisplayToggle
            val refresh = support.refreshRegion

            val overlapsRefresh =
                toggle.x in refresh.x until refresh.right &&
                    toggle.y in refresh.y until refresh.bottom

            assertFalse(overlapsRefresh, "toggle overlaps refresh at script width $scriptWidth")
            assertTrue(toggle.x in 0 until scriptWidth, "toggle is outside screen width $scriptWidth")
            assertTrue(toggle.y in 0 until 1440, "toggle is outside screen height")
        }
    }

    @Test
    fun `append skill regions use normalized spacing and stop before class score`() {
        val expectedOffsets = listOf(1610, 1685, 1760, 1835, 1910)

        listOf(2560, 2880, 3088, 3120, 3200, 3360).forEach { scriptWidth ->
            val support = locations(scriptWidth)
            val bounds = support.defaultBounds.copy(y = 400)
            val appendRegions = support.appendSkillRegions(bounds)
            val classScore = support.classScoreRegion(bounds)

            assertThat(appendRegions.size).isEqualTo(5)

            appendRegions.forEachIndexed { index, region ->
                assertThat(region.x - bounds.x).isEqualTo(expectedOffsets[index])
                assertThat(region.y - bounds.y).isEqualTo(325)
                assertThat(region.width).isEqualTo(60)
                assertThat(region.height).isEqualTo(50)
            }

            assertThat(appendRegions.last().right).isEqualTo(classScore.x)
        }
    }
}
