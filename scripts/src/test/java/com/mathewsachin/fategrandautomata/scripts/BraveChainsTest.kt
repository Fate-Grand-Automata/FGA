package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.FieldSlot
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.modules.ApplyBraveChains
import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class BraveChainsTest {
    private fun init(
        braveChains: BraveChainEnum = BraveChainEnum.None,
        rearrange: Boolean = false,
        stage: Int = 0,
        atk: AutoSkillAction.Atk = AutoSkillAction.Atk.noOp(),
        deployed: Map<FieldSlot, TeamSlot> = mapOf(
            FieldSlot.A to TeamSlot.A,
            FieldSlot.B to TeamSlot.B,
            FieldSlot.C to TeamSlot.C
        )
    ): ApplyBraveChains {
        val battleConfig: IBattleConfig = mockk()
        every { battleConfig.braveChains } returns listOf(braveChains)
        every { battleConfig.rearrangeCards } returns listOf(rearrange)

        val state: BattleState = mockk()
        every { state.stage } returns stage

        val servantTracker: ServantTracker = mockk()
        every { servantTracker.deployed } returns deployed

        return ApplyBraveChains(
            state = state,
            servantTracker = servantTracker,
            battleConfig = battleConfig
        )
    }

    @Test
    fun ignoreBraveChains() {
        val braveChains = init()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(cards)

        assert(picked == cards)
    }
}