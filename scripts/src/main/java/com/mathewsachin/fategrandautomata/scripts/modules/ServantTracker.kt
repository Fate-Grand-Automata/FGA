package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.OrderChangeMember
import com.mathewsachin.fategrandautomata.scripts.models.ServantSlot
import com.mathewsachin.fategrandautomata.scripts.models.Skill
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

    private val deployed = mutableMapOf<ServantSlot, TeamSlot?>(
        ServantSlot.A to TeamSlot.A,
        ServantSlot.B to TeamSlot.B,
        ServantSlot.C to TeamSlot.C
    )

    class TeamSlotData(
        val checkImage: IPattern,
        val skill1: IPattern,
        val skill2: IPattern,
        val skill3: IPattern
    ): AutoCloseable {
        override fun close() {
            checkImage.close()
            skill1.close()
            skill2.close()
            skill3.close()
        }
    }

    fun ServantSlot.skill1() =
        when (this) {
            ServantSlot.A -> Skill.Servant.A1
            ServantSlot.B -> Skill.Servant.B1
            ServantSlot.C -> Skill.Servant.C1
        }

    fun ServantSlot.skill2() =
        when (this) {
            ServantSlot.A -> Skill.Servant.A2
            ServantSlot.B -> Skill.Servant.B2
            ServantSlot.C -> Skill.Servant.C2
        }

    fun ServantSlot.skill3() =
        when (this) {
            ServantSlot.A -> Skill.Servant.A3
            ServantSlot.B -> Skill.Servant.B3
            ServantSlot.C -> Skill.Servant.C3
        }

    private val checkImages = mutableMapOf<TeamSlot, TeamSlotData>()
    private var supportSlot: TeamSlot? = null

    override fun close() {
        checkImages.values.forEach { it.close() }
        checkImages.clear()
    }

    private fun init(teamSlot: TeamSlot, slot: ServantSlot) {
        Timber.debug { "Servant: $teamSlot in Slot: $slot" }

        checkImages[teamSlot] = TeamSlotData(
            checkImage = game.servantChangeCheckRegion(slot).getPattern(),
            skill1 = game.imageRegion(slot.skill1()).getPattern(),
            skill2 = game.imageRegion(slot.skill2()).getPattern(),
            skill3 = game.imageRegion(slot.skill3()).getPattern()
        )

        if (supportSlot == null
            && images.support in game.servantChangeSupportCheckRegion(slot)) {
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
                || ((supportSlot == teamSlot) != (images.support in game.servantChangeSupportCheckRegion(slot)))
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
        val subIndex = sub.autoSkillCode - OrderChangeMember.Sub.A.autoSkillCode + 3

        if (subIndex in servantQueue.indices) {
            deployed[startingSlot]?.let { swapOut ->
                deployed[startingSlot] = servantQueue[subIndex]
                servantQueue[subIndex] = swapOut

                check(startingSlot)
            }
        }
    }
}