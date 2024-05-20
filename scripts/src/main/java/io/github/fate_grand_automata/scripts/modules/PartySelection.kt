package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class PartySelection @Inject constructor(
    api: IFgoAutomataApi,
    private val battleConfig: IBattleConfig
) : IFgoAutomataApi by api {
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

        if (!partySelected && party in locations.partySelectionArray.indices) {
            val currentParty = locations.selectedPartyRegion
                .find(images[Images.SelectedParty])
                ?.let { match ->
                    // Find party with min distance from center of matched region
                    locations.partySelectionArray.withIndex().minByOrNull {
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
                locations.partySelectionArray[tempParty].click()

                1.seconds.wait()
            }

            // Switch to the configured party
            if (currentParty != party) {
                locations.partySelectionArray[party].click()

                1.2.seconds.wait()
            }

            /* If we select the party once, the same party will be used by the game for next fight.
               So, we don't have to select it again. */
            partySelected = true
        }
    }

    fun checkIfPresetQuest() {
        val partyExist = images[Images.SelectedParty] in locations.selectedPartyRegion
        val exitOnPresetQuest = prefs.selectedServerConfigPref.exitOnPresetQuest
        if (!partyExist && exitOnPresetQuest) {
            messages.log(ScriptLog.PresetQuestDetected)
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.PresetQuest)
        }
    }
}