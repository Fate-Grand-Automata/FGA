package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.Duration

@ScriptScope
class PartySelection @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val battleConfig: IBattleConfig
) : IFgoAutomataApi by fgAutomataApi {
    private var partySelected = false

    /**
     * Selects the party for the quest based on the AutoSkill configuration.
     *
     * The possible behaviors of this method are:
     * 1. If no value is specified, the currently selected party is used.
     * 2. If a value is specified and is the same as the currently selected party, the party is not
     * changed.
     * 3. If a value is specified and is different than the currently selected party, the party is
     * changed to the configured one by clicking on the little dots above the party names.
     */
    fun selectParty() {
        val party = battleConfig.party

        if (!partySelected && party in game.partySelectionArray.indices) {
            val currentParty = game.selectedPartyRegion
                .find(images[Images.SelectedParty])
                ?.let { match ->
                    // Find party with min distance from center of matched region
                    game.partySelectionArray.withIndex().minByOrNull {
                        (it.value.x - match.region.center.x).absoluteValue
                    }?.index
                }

            messages.log(
                ScriptLog.CurrentParty(currentParty)
            )

            /* If the currently selected party cannot be detected, we need to switch to a party
               which was not configured. The reason is that the "Start Quest" button becomes
               unresponsive if you switch from a party to the same one. */
            if (currentParty == null) {
                val tempParty = if (party == 0) 1 else 0
                game.partySelectionArray[tempParty].click()

                Duration.seconds(1).wait()
            }

            // Switch to the configured party
            if (currentParty != party) {
                game.partySelectionArray[party].click()

                Duration.seconds(1.2).wait()
            }

            /* If we select the party once, the same party will be used by the game for next fight.
               So, we don't have to select it again. */
            partySelected = true
        }
    }
}