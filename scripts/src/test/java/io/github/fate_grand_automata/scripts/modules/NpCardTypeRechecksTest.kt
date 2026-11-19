package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.TeamSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NpCardTypeRechecksTest {
    @Test
    fun readableTypeEstablishesMissingFormBaseline() {
        assertNull(confirmedFormNpCardType(null, null))
        assertEquals(CardTypeEnum.Buster, confirmedFormNpCardType(CardTypeEnum.Buster, null))
        assertEquals(CardTypeEnum.Buster, confirmedFormNpCardType(CardTypeEnum.Buster, CardTypeEnum.Buster))
        assertNull(confirmedFormNpCardType(CardTypeEnum.Arts, CardTypeEnum.Buster))
    }

    @Test
    fun parsesOnlyAnUnambiguousPositiveCounter() {
        assertEquals(1, parseNpChangeTurns(" １\n"))
        assertEquals(12, parseNpChangeTurns("12"))
        for (text in listOf("", "0", "-1", "+2", "I", "1 2", "1ターン", "999999999999")) {
            assertNull(parseNpChangeTurns(text), text)
        }
    }

    @Test
    fun countsTurnStartsAndKeepsBenchedServantsDue() {
        val checks = NpCardTypeRechecks()
        checks.schedule(TeamSlot.A, 1)
        checks.schedule(TeamSlot.B, 3)
        assertFalse(checks.isDue(TeamSlot.A))
        checks.beginTurn()
        assertTrue(checks.isDue(TeamSlot.A))
        assertFalse(checks.isDue(TeamSlot.B))
        checks.beginTurn()
        assertFalse(checks.isDue(TeamSlot.B))
        checks.beginTurn()
        assertTrue(checks.isDue(TeamSlot.A))
        assertTrue(checks.isDue(TeamSlot.B))
    }

    @Test
    fun retriesNextTurnAndDiscardsObsoleteSchedules() {
        val checks = NpCardTypeRechecks()
        checks.schedule(TeamSlot.A, 3)
        checks.schedule(TeamSlot.A, 0)
        assertTrue(checks.isDue(TeamSlot.A))
        repeat(2) {
            checks.schedule(TeamSlot.A, 1)
            assertFalse(checks.isDue(TeamSlot.A))
            checks.beginTurn()
            assertTrue(checks.isDue(TeamSlot.A))
        }
        checks.schedule(TeamSlot.B, 1)
        checks.remove(TeamSlot.A)
        checks.beginTurn()
        assertFalse(checks.isDue(TeamSlot.A))
        assertTrue(checks.isDue(TeamSlot.B))
        checks.clear()
        checks.beginTurn()
        assertFalse(checks.isDue(TeamSlot.B))
    }
}
