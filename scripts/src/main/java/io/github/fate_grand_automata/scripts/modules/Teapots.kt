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

    private var isTeapotsOn = false


    fun manageTeapotsAtParty() {
        val region = locations.teapotsPartyRegion
        var teapotsAvailable = false
        var teapotsOn = false
        useSameSnapIn {
            teapotsAvailable = checkTeapotsAtRegion(region)
            teapotsOn = images[Images.TeapotsOn] in region
        }
        // If either teapots on or off is not available, return
        if (!teapotsAvailable) return

        val shouldUseTeapots = prefs.selectedServerConfigPref.shouldUseTeapots
        val limitTeapots = prefs.selectedServerConfigPref.limitTeapots

        when {
            // Teapots is turn off and we still need to use teapots
            !teapotsOn && limitTeapots > 0 && shouldUseTeapots -> {
                region.click()
                isTeapotsOn = true
            }
            // Teapots is turn on and we still have teapots to use or we don't need to use teapots
            // turn off the teapots
            teapotsOn && (limitTeapots <= 0 || !shouldUseTeapots) -> {
                isTeapotsOn = false
                region.click()
            }
        }
        0.5.seconds.wait()

    }

    fun manageTeapotsAtState(runs: Int) {
        val region = locations.teapotsRepeatRegion
        var teapotsAvailable = false
        var teapotsOn = false
        useSameSnapIn {
            teapotsAvailable = checkTeapotsAtRegion(region)
            teapotsOn = images[Images.TeapotsOn] in region
        }

        val shouldUseTeapots = prefs.selectedServerConfigPref.shouldUseTeapots
        val limitTeapots = prefs.selectedServerConfigPref.limitTeapots

        if (teapotsAvailable) {
            when {
                // Teapots is turn off and we still have teapots to use
                !teapotsOn && limitTeapots > 0 && shouldUseTeapots -> {
                    isTeapotsOn = true
                    region.click()
                }
                // Teapots is turn on and we still have teapots to use
                // Need to also limit with the number of runs
                // to ensure that the teapots was used
                teapotsOn && limitTeapots > 0 && runs > 0 && shouldUseTeapots -> {
                    isTeapotsOn = true
                    teapotsUsed++
                    prefs.selectedServerConfigPref.limitTeapots--
                }
                // Teapots is turn on and we don't have teapots to use
                teapotsOn && (limitTeapots <= 0 || !shouldUseTeapots) -> {
                    isTeapotsOn = false
                    region.click()
                }
            }
        } else if (isTeapotsOn) {
            teapotsUsed++
            prefs.selectedServerConfigPref.limitTeapots--
        }

    }


    private fun checkTeapotsAtRegion(region: Region) = mapOf(
        images[Images.TeapotsOn] to region,
        images[Images.TeapotsOff] to region
    ).exists()

    fun resetTeapots() {
        if (prefs.selectedServerConfigPref.limitTeapots == 0 && prefs.selectedServerConfigPref.shouldUseTeapots){
            prefs.selectedServerConfigPref.shouldUseTeapots = false
            prefs.selectedServerConfigPref.limitTeapots = 1
        }
    }

}