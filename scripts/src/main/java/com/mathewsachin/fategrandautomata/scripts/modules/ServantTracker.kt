package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.models.OrderChangeMember
import com.mathewsachin.fategrandautomata.scripts.models.ServantSlot
import com.mathewsachin.fategrandautomata.scripts.models.skills
import com.mathewsachin.libautomata.IPattern
import timber.log.Timber
import timber.log.debug
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

    private val servantQueue = mutableListOf(
        TeamSlot.D,
        TeamSlot.E,
        TeamSlot.F
    )

    val deployed = mutableMapOf<ServantSlot, TeamSlot?>(
        ServantSlot.A to TeamSlot.A,
        ServantSlot.B to TeamSlot.B,
        ServantSlot.C to TeamSlot.C
    )

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

    private fun init(teamSlot: TeamSlot, slot: ServantSlot) {
        Timber.debug { "Servant: $teamSlot in Slot: $slot" }

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

        if (supportSlot == null
            && images[Images.ServantCheckSupport] in game.servantChangeSupportCheckRegion(slot)) {
            supportSlot = teamSlot
        } else {
            // Don't useSameSnapIn here, since we open a dialog
            initFaceCard(teamSlot, slot)
        }
    }

    private val trackFaceCards = false

    private fun initFaceCard(teamSlot: TeamSlot, slot: ServantSlot) {
        if (!trackFaceCards || teamSlot in faceCardImages)
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

    private fun check(slot: ServantSlot) {
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
                deployed[slot] = newTeamSlot

                if (newTeamSlot != null) {
                    init(newTeamSlot, slot)
                }
            }
        }
    }

    fun beginTurn() =
        ServantSlot.list.forEach {
            check(it)
        }

    fun orderChanged(starting: OrderChangeMember.Starting, sub: OrderChangeMember.Sub) {
        val startingSlot = when (starting) {
            OrderChangeMember.Starting.A -> ServantSlot.A
            OrderChangeMember.Starting.B -> ServantSlot.B
            OrderChangeMember.Starting.C -> ServantSlot.C
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

    fun logFaceCardForServants(): Map<TeamSlot, List<CommandCard.Face>> {
        if (!trackFaceCards)
            return mapOf()

        val cardsRemaining = CommandCard.Face.list.toMutableSet()
        val result = mutableMapOf<TeamSlot, List<CommandCard.Face>>()

        supportSlot?.let { supportSlot ->
            if (supportSlot in deployed.values) {
                val matched = cardsRemaining.filter { card ->
                    images[Images.Support] in game.supportCheckRegion(card)
                }

                Timber.debug { "$matched belong to Support $supportSlot" }
                cardsRemaining -= matched

                result[supportSlot] = matched
            }
        }

        deployed.forEach { (slot, teamSlot) ->
            if (supportSlot != teamSlot && teamSlot != null) {
                val img = faceCardImages[teamSlot] ?: return@forEach

                val matched = cardsRemaining.filter { card ->
                    img in game.servantMatchRegion(card)
                }

                Timber.debug { "$matched belong to $slot" }
                cardsRemaining -= matched

                result[teamSlot] = matched
            }
        }

        return result
    }
}