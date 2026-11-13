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
     * @param skillNumber 1, 2 or 3
     */
    fun read(skillNumber: Int): SkillLevel =
        parseSkillLevel(locations.skill.skillLevelRegion(skillNumber).detectText())
}

private val levelBeforeSlash = Regex("""(\d+)\s*/""")
private val anyNumber = Regex("""\d+""")

/**
 * Extracts N from OCR text of a "Lv. N/10" label.
 *
 * With a slash, only the number in front of it counts, so the "/10" can't be mistaken for the
 * level. Without one, OCR lost the slash and the first number is taken.
 */
fun parseSkillLevel(text: String): SkillLevel {
    if (text.isBlank()) {
        return SkillLevel.Locked
    }

    // OCR confuses these letters with digits
    val cleaned = text.replace('O', '0').replace('o', '0')

    val number = if ('/' in cleaned) {
        levelBeforeSlash.find(cleaned)?.groupValues?.get(1)
    } else {
        anyNumber.find(cleaned)?.value
    }

    val level = number?.toIntOrNull()?.takeIf { it in 1..maxSkillLevel }
        ?: return SkillLevel.Unreadable

    return SkillLevel.Known(level)
}
