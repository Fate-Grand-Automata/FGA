package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.enums.AppendSkillRequirement
import io.github.fate_grand_automata.scripts.enums.ClassScoreRequirement
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.HsvRange
import io.github.lib_automata.Pattern
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportColorChecker @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences
) : IFgoAutomataApi by api {
    private val brightColor = HsvRange(0..179, 80..255, 150..255)
    private val maxScoreYellow = HsvRange(18..39, 80..255, 70..255)

    fun needsColorCheckInAppendMode(): Boolean =
        supportPrefs.classScore == ClassScoreRequirement.Max ||
            appendRequirements().any { it == AppendSkillRequirement.Unlocked }

    fun needsClassScoreCheck() = supportPrefs.classScore == ClassScoreRequirement.Max

    fun checkClassScore(bounds: SupportBounds): Boolean =
        supportPrefs.classScore != ClassScoreRequirement.Max ||
            locations.support.classScoreRegion(bounds.region).matchesColor(
                range = maxScoreYellow,
                minimumRatio = prefs.support.classScoreMinRatio
            )

    fun checkUnlockedAppendSkills(bounds: SupportBounds): Boolean =
        appendRequirements()
            .zip(locations.support.appendSkillRegions(bounds.region))
            .all { (requirement, region) ->
                requirement != AppendSkillRequirement.Unlocked ||
                    region.matchesColor(
                        range = brightColor,
                        minimumRatio = prefs.support.appendUnlockedMinRatio
                    )
            }

    private fun appendRequirements() = listOf(
        supportPrefs.appendSkill1,
        supportPrefs.appendSkill2,
        supportPrefs.appendSkill3,
        supportPrefs.appendSkill4,
        supportPrefs.appendSkill5
    )

    private fun Region.matchesColor(
        range: HsvRange,
        minimumRatio: Double
    ): Boolean = getPattern().use { pattern ->
        val count = pattern.countHsvPixels(range)
        count.toDouble() / pattern.pixelCount >= minimumRatio
    }

    private val Pattern.pixelCount get() = width * height
}
