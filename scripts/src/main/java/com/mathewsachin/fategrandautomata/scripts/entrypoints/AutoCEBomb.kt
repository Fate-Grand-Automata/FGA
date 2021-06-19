package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Location
import javax.inject.Inject
import kotlin.time.Duration

class AutoCEBomb @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object NoLvl1TargetCEFound: ExitReason()
    }

    enum class Target {
        Gloom,
        Starvation
    }

    fun Target.pattern() = when (this) {
        Target.Gloom -> Images.CEGloomLv1
        Target.Starvation -> Images.CEStarvationLv1
    }.let { images[it] }

    class ExitException(val reason: ExitReason): Exception()

    fun level1CE(img: IPattern = prefs.ceBombTarget.pattern()) =
        game.levelOneCERegion.find(img)
            ?: throw ExitException(ExitReason.NoLvl1TargetCEFound)

    override fun script(): Nothing {
        game.ceEnhanceClick.click()

        while (true) {
            Duration.seconds(2).wait()

            val baseCERegion = level1CE().Region

            // This would help if we later make it able to recognize multiple CEs later.
            val img = baseCERegion.getPattern()

            img.use {
                baseCERegion.click()
                Duration.seconds(2).wait()

                Location(900, 500).click()
                Duration.seconds(2).wait()

                pickMatchingCE(img)
                pickCEs()

                repeat(2) {
                    Location(2300, 1300).click()
                    Duration.seconds(1).wait()
                }

                Location(1600, 1200).click()
                Duration.seconds(1).wait()

                Location(2000, 1000).click(70)
                game.ceEnhanceClick.click()
            }
        }
    }

    private fun pickMatchingCE(img: IPattern) {
        level1CE(img).Region.click()
        Duration.seconds(1).wait()
    }

    private fun pickCEs() {
        Location(2040, 1400).click()
        Duration.seconds(2).wait()

        for (y in 0..3) {
            for (x in 0..6) {
                Location(1900 - 270 * x, 1300 - 290 * y).click()
            }
        }
    }
}