package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.ScriptModeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.TeamSlot
import kotlin.time.Duration

sealed class ScriptNotify {
    data object CEGet : ScriptNotify()
    data object CEDropped : ScriptNotify()
    class WaitForAPRegen(val minutes: Int = 1) : ScriptNotify()
    class FailedToDetermineCards(val cards: List<CommandCard.Face>,
                                 val unknownCardTypes: Boolean, val unknownServants: Boolean) : ScriptNotify()
    class SupportListUpdatingIn(val time: Duration) : ScriptNotify()
    class BetweenRuns(val refills: Int, val runs: Int, val ceDrops: Int) : ScriptNotify()
    data object BondLevelUp : ScriptNotify()
}

sealed class ScriptLog {
    object DefaultSupportBounds : ScriptLog()
    object DefaultMasterOffset : ScriptLog()
    class CurrentParty(val party: Int?) : ScriptLog()
    class MaxSkills(
        val needMaxedSkills: List<Boolean>,
        val isSkillMaxed: List<Boolean>
    ) : ScriptLog()

    class ClickingNPs(val nps: Iterable<CommandCard.NP>) : ScriptLog()
    class ClickingCards(val cards: Iterable<CommandCard.Face>) : ScriptLog()
    class NPsGroupedByFaceCards(val groups: Map<CommandCard.NP, List<CommandCard.Face>>) : ScriptLog()
    class SupportFaceCardGroup(val group: List<CommandCard.Face>) : ScriptLog()
    class FaceCardGroups(val groups: List<List<CommandCard.Face>>) : ScriptLog()
    class ServantEnteredSlot(
        val servant: TeamSlot,
        val slot: FieldSlot
    ) : ScriptLog()

    class CardsBelongToServant(
        val cards: Iterable<CommandCard.Face>,
        val servant: TeamSlot,
        val isSupport: Boolean = false
    ) : ScriptLog()
}

sealed class ScriptMessage {
    class NotifyErrorWarningScript(val script: ScriptModeEnum): ScriptMessage()
}

interface IScriptMessages {
    fun notify(action: ScriptNotify)

    fun log(item: ScriptLog)

    fun storeString(item: ScriptMessage)
}