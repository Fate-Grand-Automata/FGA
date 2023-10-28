package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSetup @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {


    fun Region.detectNumberInText(): Int? {
        val text = this
            .detectText(false) // replace common OCR mistakes
            .replace("%", "x")
            .replace("S", "5")
            .replace("O", "0")
            .lowercase()
        val regex = Regex("""(\d+)""")
        return regex.find(text)?.groupValues?.getOrNull(1)?.toInt()
    }

    fun getMinimumSkillLevel() {
        val skill1Text = locations.skillUpgrade.skill1TextRegion.detectNumberInText()
        prefs.skillUpgrade.minSkill1 = skill1Text ?: 1
        val skill2Text = locations.skillUpgrade.skill2TextRegion.detectNumberInText()

        prefs.skillUpgrade.minSkill2 = skill2Text ?: 1
        prefs.skillUpgrade.skill2Available = skill2Text != null

        val skill3Text = locations.skillUpgrade.skill3TextRegion.detectNumberInText()

        prefs.skillUpgrade.minSkill3 = skill3Text ?: 1
        prefs.skillUpgrade.skill3Available = skill3Text != null
    }

    fun checkIfEmptyEnhance() {
        val emptyEnhance = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

        prefs.craftEssence.emptyEnhance = emptyEnhance
    }

    val isPlayButtonInGoodXLocation = prefs.playButtonLocation.x in
            0..locations.scriptAreaRaw.width / 4

    val isPlayButtonInGoodYLocation = prefs.playButtonLocation.y in
            locations.scriptAreaRaw.height * 5 / 8..locations.scriptAreaRaw.height

}