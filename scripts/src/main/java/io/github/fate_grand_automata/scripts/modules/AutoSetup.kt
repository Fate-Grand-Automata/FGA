package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSetup @Inject constructor(
    api: IFgoAutomataApi,
) : IFgoAutomataApi by api {

    val playButton = prefs.playButtonRegion

    val isPlayButtonInGoodXLocation = playButton.location.x in
            0..locations.scriptAreaRaw.width / 2

    val isPlayButtonInGoodYLocation = playButton.location.y in
            locations.scriptAreaRaw.height * 5 / 8..locations.scriptAreaRaw.height

}