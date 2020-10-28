package com.mathewsachin.fategrandautomata

import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.extensions.TransformationExtensions
import org.junit.Assert
import org.junit.Test

class TransformationTest {
    fun getGameAreaManager(windowRegion: Region): GameAreaManager {
        return FgoGameAreaManager(
            FakePlatformImpl(windowRegion),
            Game.scriptSize,
            Game.imageSize
        )
    }

    @Test
    fun no_scaling_when_window_region_is_same_as_script_dimensions() {
        val gameAreaManager = getGameAreaManager(
            Game.scriptRegion
        )

        TransformationExtensions(gameAreaManager).run {
            val original = Region(10, 20, 120, 230)
            val scaled = original.transform()

            Assert.assertEquals(original, scaled)
        }
    }
}
