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

        val slot1 = setOf(Kukulkan)

        val slot2 = setOf(Kukulkan, Dante, SummerShiki)

        val slot2TargetEntries = setOf(Kukulkan)

        val slot3 = setOf(Kukulkan, UDKBarghest)
    }
}


enum class Choice3Type(val slot: SkillSlot) {
    Generic(SkillSlot.ANY),

    // First slot
    VanGogh(SkillSlot.First),

    // Third slot
    Hakuno(SkillSlot.Third),
    Soujuurou(SkillSlot.Third),
    Charlotte(SkillSlot.Third);

    companion object {
        val slot1 = setOf(VanGogh)

        val slot3 = setOf(Hakuno, Soujuurou, Charlotte)
    }
}

enum class TransformType(val slot: SkillSlot) {
    Melusine(SkillSlot.Third),
    Ptolemy(SkillSlot.Third);

    companion object {
        val slot3 = setOf(Melusine, Ptolemy)
    }
}