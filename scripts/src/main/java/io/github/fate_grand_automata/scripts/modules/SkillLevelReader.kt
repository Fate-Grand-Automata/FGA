package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

const val maxSkillLevel = 10

sealed interface SkillLevel {
    /** The level label is empty. */
    data object Locked : SkillLevel

    /** OCR found text, but no level in it. */
    data object Unreadable : SkillLevel

    data class Known(val level: Int) : SkillLevel
}

/**
 * Reads the "Lv. N/10" label under a skill on the Skill Enhancement screen.
 */
@ScriptScope
class SkillLevelReader @Inject constructor(
    api: IFgoAutomataApi,
) : IFgoAutomataApi by api {

    /**
     * Reads the level of one skill from the current screen.
     *
     * @param skillNumber 1, 2 or 3
     */
    fun read(skillNumber: Int): SkillLevel =
        parseSkillLevel(locations.skill.skillLevelRegion(skillNumber).detectText(upscale = true))
}

private val levelBeforeSlash = Regex("""(\d+)\s*/""")

/**
 * Extracts N from OCR text of a "Lv. N/10" label.
 *
 * Only the number in front of the slash counts. Without a slash the text is unreadable rather
 * than guessed at, since OCR turns the label prefix into stray digits, e.g. "4 9N0" for CN's
 * "等级 9/10".
 */
fun parseSkillLevel(text: String): SkillLevel {
    if (text.isBlank()) {
        return SkillLevel.Locked
    }

    // OCR confuses these letters with digits
    val cleaned = text.replace('O', '0').replace('o', '0')

    val number = levelBeforeSlash.find(cleaned)?.groupValues?.get(1)
    val level = number?.toIntOrNull()?.takeIf { it in 1..maxSkillLevel }
        ?: return SkillLevel.Unreadable

    return SkillLevel.Known(level)
}
