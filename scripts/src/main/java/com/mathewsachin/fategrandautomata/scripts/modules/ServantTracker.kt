package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.models.OrderChangeMember
import com.mathewsachin.fategrandautomata.scripts.models.FieldSlot
import com.mathewsachin.fategrandautomata.scripts.models.skills
import com.mathewsachin.libautomata.IPattern
import kotlin.time.Duration

class ServantTracker(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi, AutoCloseable {
    sealed class TeamSlot(val position: Int) {
        object A: TeamSlot(1)
        object B: TeamSlot(2)
        object C: TeamSlot(3)
        object D: TeamSlot(4)
        object E: TeamSlot(5)
        object F: TeamSlot(6)

        override fun toString() = "[$position]"

        companion object {
            val list by lazy {
                listOf(A, B, C, D, E, F)
            }
        }
    }

    private val servantQueue = mutableListOf<TeamSlot>()
    val deployed = mutableMapOf<FieldSlot, TeamSlot>()

    fun nextRun() {
        servantQueue.clear()
        servantQueue.addAll(
            listOf(TeamSlot.D, TeamSlot.E, TeamSlot.F)
        )

        deployed.clear()
        deployed.putAll(
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

        if (teamSlot !in checkImages) {
            useSameSnapIn {
                checkImages[teamSlot] = TeamSlotData(
                    checkImage = game.servantChangeCheckRegion(slot)
                        .getPattern()
                        .tag("Servant $teamSlot"),
                    skills = slot.skills().mapIndexed { index, it ->
                        game.imageRegion(it)
                            .getPattern()
                            .tag("Servant $teamSlot S${index + 1}")
                    }
                )
            }
        }

        if (supportSlot == null
            && images[Images.ServantCheckSupport] in game.servantChangeSupportCheckRegion(slot)) {
            supportSlot = teamSlot
        } else {
            // Don't useSameSnapIn here, since we open a dialog
            initFaceCard(teamSlot, slot)
        }
    }

    private fun initFaceCard(teamSlot: TeamSlot, slot: FieldSlot) {
        if (teamSlot in faceCardImages)
            return

        // Open details dialog and click on INFO
        game.servantOpenDetailsClick(slot).click()
        game.servantDetailsInfoClick.click()

        Duration.milliseconds(250).wait()

        val image = game.servantDetailsFaceCardRegion.getPattern().tag("Face $teamSlot")

        // Close dialog
        game.battleExtraInfoWindowCloseClick.click()

        faceCardImages[teamSlot] = image
    }

    private fun check(slot: FieldSlot) {
        // If a servant is not present, that means none are left in the backline
        if (images[Images.ServantExist] !in game.servantPresentRegion(slot)) {
            deployed.remove(slot)
            servantQueue.clear()
            return
        }

        val teamSlot = deployed[slot] ?: return

        checkImages[teamSlot].let {
            if (it == null) {
                init(teamSlot, slot)
            }
            else if (
                it.checkImage !in game.servantChangeCheckRegion(slot)
                // In case of dual-servant comps, identify between support and our servant with SUPPORT marker.
                || ((supportSlot == teamSlot) != (images[Images.ServantCheckSupport] in game.servantChangeSupportCheckRegion(slot)))
            ) {
                val newTeamSlot = servantQueue.removeFirstOrNull()

                if (newTeamSlot != null) {
                    deployed[slot] = newTeamSlot
                    init(newTeamSlot, slot)
                } else deployed.remove(slot)
            }
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
                deployed[startingSlot] = servantQueue[subIndex]
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
                    images[Images.Support] in game.supportCheckRegion(card)
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
                    img in game.servantMatchRegion(card)
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