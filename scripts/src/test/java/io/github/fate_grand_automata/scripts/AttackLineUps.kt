package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot

class AttackLineUps {
    data object Standard {
        val scathach1WB = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val kama2Q = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Quick
        )
        val nero3RA = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val nero4RA = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val scathach5WQ = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )

        /**
         * [Kama, Scathach, Nero]
         * [Scathach WB] [Kama Q] [Nero RA] [Nero RA] [Scathach WQ]
         */
        val lineup1 = listOf(scathach1WB, kama2Q, nero3RA, nero4RA, scathach5WQ)

        val lineup2 = listOf(scathach1WB, scathach5WQ, kama2Q, nero3RA, nero4RA)
    }

    data object BusterFocus {
        val kiyohime1B = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val kiyohime2B = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val nero3RA = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val nero4RA = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val kiyohime5B = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val nero3QAlt = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Resist
        )
        val nero4BAlt = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Resist
        )
        val nero5BAlt = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Resist
        )

        // Buster brave chain
        val lineup01 = listOf(kiyohime1B, kiyohime2B, nero3RA, nero4RA, kiyohime5B)
        val lineup02 = listOf(kiyohime1B, kiyohime5B, kiyohime2B, nero3RA, nero4RA)

        // Buster chain only
        val lineup03 = listOf(kiyohime1B, kiyohime2B, nero3RA, nero4RA, nero5BAlt)
        val lineup04 = listOf(kiyohime1B, nero5BAlt, kiyohime2B, nero3RA, nero4RA)

        // Buster chain with either Brave or non-Brave
        val lineup05 = listOf(kiyohime1B, kiyohime2B, nero3RA, nero4BAlt, kiyohime5B)
        val lineup06 = listOf(kiyohime1B, kiyohime5B, kiyohime2B, nero3RA, nero4BAlt)

        // Buster brave chain or Mighty chain
        val lineup07 = listOf(kiyohime1B, kiyohime2B, nero3QAlt, nero4RA, kiyohime5B)
        val lineup08 = listOf(kiyohime1B, kiyohime5B, kiyohime2B, nero3QAlt, nero4RA)

        // Buster chain or Mighty brave chain
        val lineup09 = listOf(kiyohime1B, kiyohime2B, nero3QAlt, nero4RA, nero5BAlt)
        val lineup10 = listOf(kiyohime1B, nero5BAlt, kiyohime2B, nero3QAlt, nero4RA)
    }

    data object SingleServantOnly {
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
        val scathach3WAltQ = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
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

        // 3 Quick, 2 Buster; Buster starting card
        val lineup4 = listOf(scathach1WB, scathach2WQ, scathach3WAltQ, scathach4WB, scathach5WQ)
        // 3 Quick, 2 Buster; Quick starting card
        val lineup5 = listOf(scathach5WQ, scathach2WQ, scathach3WAltQ, scathach1WB, scathach4WB)
    }
}