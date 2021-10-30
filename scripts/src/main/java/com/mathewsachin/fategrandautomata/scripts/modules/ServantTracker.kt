package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class ServantTracker @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi, AutoCloseable {

    private val servantQueue = mutableListOf<TeamSlot>()
    private val _deployed = mutableMapOf<FieldSlot, TeamSlot>()
    val deployed: Map<FieldSlot, TeamSlot> = _deployed

    fun nextRun() {
        servantQueue.clear()
        servantQueue.addAll(
            listOf(TeamSlot.D, TeamSlot.E, TeamSlot.F)
        )

        _deployed.clear()
        _deployed.putAll(
            mapOf(
                FieldSlot.A to TeamSlot.A,
                FieldSlot.B to TeamSlot.B,
                FieldSlot.C to TeamSlot.C
            )
        )
    }

    init {
        nextRun()
    }

    class TeamSlotData(
        val checkImage: IPattern,
        val skills: List<IPattern>
    ): AutoCloseable {
        override fun close() {
            checkImage.close()
            skills.forEach { it.close() }
        }
    }

    val checkImages = mutableMapOf<TeamSlot, TeamSlotData>()
    private var supportSlot: TeamSlot? = null

    val faceCardImages = mutableMapOf<TeamSlot, IPattern>()

    override fun close() {
        checkImages.values.forEach { it.close() }
        faceCardImages.values.forEach { it.close() }
        checkImages.clear()
    }

    private fun init(teamSlot: TeamSlot, slot: FieldSlot) {
        messages.log(
            ScriptLog.ServantEnteredSlot(
                servant = teamSlot,
                slot = slot
            )
        )

        val isSupport = images[Images.ServantCheckSupport] in locations.battle.servantChangeSupportCheckRegion(slot)

        if (teamSlot !in checkImages || isSupport) {
            useSameSnapIn {
                checkImages[teamSlot] = TeamSlotData(
                    checkImage = locations.battle.servantChangeCheckRegion(slot)
                        .getPattern()
                        .tag("Servant $teamSlot"),
                    skills = slot.skills().mapIndexed { index, it ->
                        locations.battle.imageRegion(it)
                            .getPattern()
                            .tag("Servant $teamSlot S${index + 1}")
                    }
                )
            }
        }

        if (supportSlot == null && isSupport) {
            supportSlot = teamSlot
        } else if (!isSupport) {
            // Don't useSameSnapIn here, since we open a dialog
            initFaceCard(teamSlot, slot)
        }
    }

    private fun initFaceCard(teamSlot: TeamSlot, slot: FieldSlot) {
        if (teamSlot in faceCardImages)
            return

        // Open details dialog and click on INFO
        locations.battle.servantOpenDetailsClick(slot).click()
        locations.battle.servantDetailsInfoClick.click()

        Duration.milliseconds(250).wait()

        val image = locations.battle.servantDetailsFaceCardRegion.getPattern().tag("Face $teamSlot")

        // Close dialog
        locations.battle.extraInfoWindowCloseClick.click()

        faceCardImages[teamSlot] = image

        Duration.milliseconds(250).wait()
    }

    private fun check(slot: FieldSlot) {
        // If a servant is not present, that means none are left in the backline
        if (images[Images.ServantExist] !in locations.battle.servantPresentRegion(slot)) {
            _deployed.remove(slot)
            servantQueue.clear()
            return
        }

        val teamSlot = deployed[slot] ?: return
        val checkImage = checkImages[teamSlot]?.checkImage

        if (checkImage == null) {
            init(teamSlot, slot)
            return
        }

        val isDifferentServant = checkImage !in locations.battle.servantChangeCheckRegion(slot)
        val isSupport = images[Images.ServantCheckSupport] in locations.battle.servantChangeSupportCheckRegion(slot)
        val wasSupport = supportSlot == teamSlot

        // New run with different support
        if (wasSupport && isSupport && isDifferentServant) {
            init(teamSlot, slot)
        }
        else if (isDifferentServant || (wasSupport != isSupport)) {
            val newTeamSlot = servantQueue.removeFirstOrNull()

            if (newTeamSlot != null) {
                _deployed[slot] = newTeamSlot
                init(newTeamSlot, slot)
            } else _deployed.remove(slot)
        }
    }

    fun beginTurn() =
        FieldSlot.list.forEach {
            check(it)
        }

    fun orderChanged(starting: OrderChangeMember.Starting, sub: OrderChangeMember.Sub) {
        val startingSlot = when (starting) {
            OrderChangeMember.Starting.A -> FieldSlot.A
            OrderChangeMember.Starting.B -> FieldSlot.B
            OrderChangeMember.Starting.C -> FieldSlot.C
        }
        val subIndex = sub.autoSkillCode - OrderChangeMember.Sub.A.autoSkillCode

        if (subIndex in servantQueue.indices) {
            deployed[startingSlot]?.let { swapOut ->
                _deployed[startingSlot] = servantQueue[subIndex]
                servantQueue[subIndex] = swapOut

                check(startingSlot)
            }
        }
    }

    fun faceCardsGroupedByServant(): Map<TeamSlot, List<CommandCard.Face>> {
        val cardsRemaining = CommandCard.Face.list.toMutableSet()
        val result = mutableMapOf<TeamSlot, List<CommandCard.Face>>()

        supportSlot?.let { supportSlot ->
            if (supportSlot in deployed.values) {
                val matched = cardsRemaining.filter { card ->
                    images[Images.Support] in locations.attack.supportCheckRegion(card)
                }

                messages.log(
                    ScriptLog.CardsBelongToServant(
                        cards = matched,
                        servant = supportSlot,
                        isSupport = true
                    )
                )

                cardsRemaining -= matched

                result[supportSlot] = matched
            }
        }

        for (teamSlot in deployed.values) {
            if (supportSlot != teamSlot) {
                val img = faceCardImages[teamSlot] ?: continue

                val matched = cardsRemaining.filter { card ->
                    img in locations.attack.servantMatchRegion(card)
                }

                messages.log(
                    ScriptLog.CardsBelongToServant(
                        cards = matched,
                        servant = teamSlot
                    )
                )

                cardsRemaining -= matched

                result[teamSlot] = matched
            }
        }

        return result
    }
}