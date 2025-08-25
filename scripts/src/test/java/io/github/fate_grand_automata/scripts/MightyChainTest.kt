package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot

class MightyChainTest {
    companion object {
        val scathach1WB = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val scathach2WQ = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )
        val scathach3WA = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Weak
        )
        val scathach4WB = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val scathach5WQ = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )

        val lineup1 = listOf(scathach1WB, scathach2WQ, scathach3WA, scathach4WB, scathach5WQ)

        val lineup2 = listOf(scathach1WB, scathach4WB, scathach5WQ, scathach2WQ, scathach3WA)

        val lineup3 = listOf(scathach5WQ, scathach2WQ, scathach3WA, scathach1WB, scathach4WB)
    }
}