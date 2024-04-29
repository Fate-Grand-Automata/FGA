package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class Teapots @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {

    var teapotsUsed = 0


    fun manageTeapotsAtParty() {
        val region = locations.teapotsPartyRegion
        var teapotsAvailable = false
        var teapotsOn = false
        useSameSnapIn {
            teapotsAvailable = checkTeapotAvailability(region)
            teapotsOn = images[Images.TeapotsOn] in region
        }
        if (!teapotsAvailable) return
        val limitTeapots = prefs.selectedServerConfigPref.limitTeapots

        if (limitTeapots > 0 && !teapotsOn) {
            region.click()
            0.5.seconds.wait()
        }

    }

    fun manageTeapotsAtState(runs: Int) {
        val region = locations.teapotsRepeatRegion
        var teapotsAvailable = false
        var teapotsOn = false
        useSameSnapIn {
            teapotsAvailable = checkTeapotAvailability(region)
            teapotsOn = images[Images.TeapotsOn] in region
        }
        if (!teapotsAvailable) return

        val limitTeapots = prefs.selectedServerConfigPref.limitTeapots

        when {
            // Teapots is turn off and we still have teapots to use
            !teapotsOn && limitTeapots > 0 -> {
                region.click()
            }
            // Teapots is turn on and we still have teapots to use
            // Need to also limit with the number of runs
            // to ensure that the teapots was ueed
            teapotsOn && limitTeapots > 0 && runs > 0 -> {
                teapotsUsed++
                prefs.selectedServerConfigPref.limitTeapots--
            }
            // Teapots is turn on and we don't have teapots to use
            teapotsOn && limitTeapots <= 0 && runs > 0 -> {
                region.click()
            }
        }
    }

    private fun checkTeapotAvailability(region: Region): Boolean {
        val shouldUseTeapots = prefs.selectedServerConfigPref.shouldUseTeapots

        if (!shouldUseTeapots) return false

        val isTeapotsPresent = checkTeapotsAtRegion(region)

        if (!isTeapotsPresent) return false

        0.25.seconds.wait()

        return true
    }


    private fun checkTeapotsAtRegion(region: Region) = mapOf(
        images[Images.TeapotsOn] to region,
        images[Images.TeapotsOff] to region
    ).exists()

}