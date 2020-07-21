package com.mathewsachin.fategrandautomata

import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.extensions.TransformationExtensions
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class TransformationTest {
    fun getGameAreaManager(windowRegion: Region): GameAreaManager {
        val mockPlatformImpl = mockk<IPlatformImpl>()
        every { mockPlatformImpl.windowRegion } returns windowRegion

        return GameAreaManager(mockPlatformImpl)
    }

    @Test
    fun no_scaling_when_window_region_is_same_as_script_dimensions() {
        val game = Game(mockk())

        val gameAreaManager = getGameAreaManager(
            Region(0, 0, game.scriptSize.Width, game.scriptSize.Height)
        )

        TransformationExtensions(gameAreaManager).run {
            val original = Region(10, 20, 120, 230)
            val scaled = original.transform()

            Assert.assertEquals(original, scaled)
        }
    }
}
