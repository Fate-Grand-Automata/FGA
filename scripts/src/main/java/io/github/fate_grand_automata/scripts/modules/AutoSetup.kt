package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.Scale
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSetup @Inject constructor(
    api: IFgoAutomataApi,
    private val scale: Scale
) : IFgoAutomataApi by api {

    fun checkIfEmptyEnhance() {
        val emptyEnhance = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

        prefs.emptyEnhance = emptyEnhance
    }

    fun checkAppendLocks(){
        useSameSnapIn {
            prefs.append.appendOneLocked = images[Images.AppendLock] in locations.append.lockLocations(0)
            prefs.append.appendTwoLocked = images[Images.AppendLock] in locations.append.lockLocations(1)
            prefs.append.appendThreeLocked = images[Images.AppendLock] in locations.append.lockLocations(2)
        }
    }

}