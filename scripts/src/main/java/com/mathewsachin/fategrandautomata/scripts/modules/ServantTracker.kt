package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.models.OrderChangeMember
import com.mathewsachin.fategrandautomata.scripts.models.ServantSlot
import com.mathewsachin.fategrandautomata.scripts.models.skills
import com.mathewsachin.libautomata.IPattern
import timber.log.Timber
import timber.log.debug

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

    override fun close() {
        checkImages.values.forEach { it.close() }
        checkImages.clear()
    }

    private fun init(teamSlot: TeamSlot, slot: ServantSlot) {
        Timber.debug { "Servant: $teamSlot in Slot: $slot" }

        checkImages[teamSlot] = TeamSlotData(
            checkImage = game.servantChangeCheckRegion(slot).getPattern(),
            skills = slot.skills().map { game.imageRegion(it).getPattern() }
        )

        if (supportSlot == null
            && images[Images.Support] in game.servantChangeSupportCheckRegion(slot)) {
            supportSlot = teamSlot
        }
    }

    private fun check(slot: ServantSlot) {
        val teamSlot = deployed[slot] ?: return

        checkImages[teamSlot].let {
            if (it == null) {
                init(teamSlot, slot)
            }
            else if (
                it.checkImage !in game.servantChangeCheckRegion(slot)
                || ((supportSlot == teamSlot) != (images[Images.Support] in game.servantChangeSupportCheckRegion(slot)))
            ) {
                val newTeamSlot = servantQueue.removeFirstOrNull()
                deployed[slot] = newTeamSlot

                if (newTeamSlot != null) {
                    init(newTeamSlot, slot)
                }
            }
        }

    }

    fun beginTurn() {
        check(ServantSlot.A)
        check(ServantSlot.B)
        check(ServantSlot.C)
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
}