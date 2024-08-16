package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSkillSetup @Inject constructor(
    api: IFgoAutomataApi,
) : IFgoAutomataApi by api {

    fun checkIfEmptyEnhance() {
        val emptyEnhance = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

        prefs.skill.isEmptyEnhance = emptyEnhance
    }


    fun getMinimumSkillLevel() {
        val skill1Text = locations.skill.skillTextRegion(1).findNumberInText()
        prefs.skill.minimumSkillOne = skill1Text ?: 1
        val skill2Text = locations.skill.skillTextRegion(2).findNumberInText()

        prefs.skill.minimumSkillTwo = skill2Text ?: 1
        prefs.skill.isSkillTwoAvailable = skill2Text != null

        val skill3Text = locations.skill.skillTextRegion(3).findNumberInText()

        prefs.skill.minimumSkillThree = skill3Text ?: 1
        prefs.skill.isSkillThreeAvailable = skill3Text != null
    }


}