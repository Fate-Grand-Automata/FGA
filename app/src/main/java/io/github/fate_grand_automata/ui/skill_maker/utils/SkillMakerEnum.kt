package io.github.fate_grand_automata.ui.skill_maker.utils

import io.github.fate_grand_automata.ui.skill_maker.SkillSlot

enum class ChangeNp2Type {
    Generic,
    Emiya,
    BBDubai;

    companion object {
        val slot3 = setOf(Emiya, BBDubai)
    }
}

enum class ChangeNp3Type {
    Generic,
    SpaceIshtar;

    companion object {
        val slot2 = setOf(SpaceIshtar)
    }
}

enum class Choice2Type(val slot: SkillSlot) {
    Generic(SkillSlot.ANY),
    Kukulkan(SkillSlot.ANY),
    Dante(SkillSlot.Second),
    SummerShiki(SkillSlot.Second),
    UDKBarghest(SkillSlot.Third);

    companion object {
        fun mustSelect(current: SkillSlot) = current == SkillSlot.Second

        fun inSlot(slot: SkillSlot) = entries.filter { it != Generic && it.slot.matches(slot) }

        val slot1 = inSlot(SkillSlot.First)

        val slot2 = inSlot(SkillSlot.Second)

        val slot3 = inSlot(SkillSlot.Third)

        /** Servants whose option still needs a servant target picked afterwards. */
        val slot2TargetEntries = setOf(Kukulkan)
    }
}


enum class Choice3Type(val slot: SkillSlot) {
    Generic(SkillSlot.ANY),
    VanGogh(SkillSlot.First),
    Hakuno(SkillSlot.Third),
    Soujuurou(SkillSlot.Third),
    Charlotte(SkillSlot.Third);

    companion object {
        fun inSlot(slot: SkillSlot) = entries.filter { it != Generic && it.slot.matches(slot) }

        val slot1 = inSlot(SkillSlot.First)

        val slot3 = inSlot(SkillSlot.Third)
    }
}

enum class TransformType(val slot: SkillSlot) {
    Melusine(SkillSlot.Third),
    Ptolemy(SkillSlot.Third);

    companion object {
        val slot3 = entries.filter { it.slot.matches(SkillSlot.Third) }
    }
}
