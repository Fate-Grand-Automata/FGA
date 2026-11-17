package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.modules.SkillLevel
import io.github.fate_grand_automata.scripts.modules.parseSkillLevel
import kotlin.test.Test

class SkillLevelParserTest {
    @Test
    fun readsLevelInFrontOfSlash() {
        assertThat(parseSkillLevel("Lv. 1/10")).isEqualTo(SkillLevel.Known(1))
        assertThat(parseSkillLevel("Lv. 7/10")).isEqualTo(SkillLevel.Known(7))
        assertThat(parseSkillLevel("Lv. 10/10")).isEqualTo(SkillLevel.Known(10))
        assertThat(parseSkillLevel("Lv.10 / 10")).isEqualTo(SkillLevel.Known(10))
    }

    @Test
    fun fixesLetterOMisreadAsZero() {
        assertThat(parseSkillLevel("Lv. 1O/1O")).isEqualTo(SkillLevel.Known(10))
    }

    @Test
    fun rejectsTextWithoutSlash() {
        assertThat(parseSkillLevel("Lv. 4 10")).isEqualTo(SkillLevel.Unreadable)
        // Real misread of CN's "等级 9/10"
        assertThat(parseSkillLevel("4 9N0")).isEqualTo(SkillLevel.Unreadable)
    }

    @Test
    fun ignoresMaxLevelWhenLevelInFrontOfSlashIsMissing() {
        assertThat(parseSkillLevel("Lv. /10")).isEqualTo(SkillLevel.Unreadable)
        assertThat(parseSkillLevel("Lv. l/10")).isEqualTo(SkillLevel.Unreadable)
    }

    @Test
    fun blankTextMeansLocked() {
        assertThat(parseSkillLevel("")).isEqualTo(SkillLevel.Locked)
        assertThat(parseSkillLevel(" \n")).isEqualTo(SkillLevel.Locked)
    }

    @Test
    fun rejectsTextWithoutValidLevel() {
        assertThat(parseSkillLevel("Lv.")).isEqualTo(SkillLevel.Unreadable)
        assertThat(parseSkillLevel("Lv. 17")).isEqualTo(SkillLevel.Unreadable)
        assertThat(parseSkillLevel("Lv. 0/10")).isEqualTo(SkillLevel.Unreadable)
    }
}
