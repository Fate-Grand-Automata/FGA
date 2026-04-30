package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.IImageLoader
import io.github.fate_grand_automata.scripts.IScriptMessages
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.lib_automata.AutomataApi
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class MasterLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    private val images: IImageLoader,
    private val automataApi: AutomataApi,
    private val messages: IScriptMessages,
) : IScriptAreaTransforms by scriptAreaTransforms {
    // Master Skills and Stage counter are right-aligned differently,
    // so we use locations relative to a matched location
    private val masterOffsetNewUI: Location by lazy {
        automataApi.run {
            Region(-400, 360, 400, 80)
                .xFromRight()
                .find(images[Images.BattleMenu])
                ?.region
                ?.center
                ?.copy(y = 0)
                ?: Location(-298, 0).xFromRight().also {
                    messages.log(ScriptLog.DefaultMasterOffset)
                }
        }
    }

    fun locate(skill: Skill.Master) = when (skill) {
        Skill.Master.A -> -740
        Skill.Master.B -> -560
        Skill.Master.C -> -400
    }.let { x ->
        Location(x + 178, 620) + masterOffsetNewUI
    }

    val stageCountRegion
        get() = Region(if (isWide) -571 else -638, 23, 33, 53) + masterOffsetNewUI

    val masterSkillOpenClick
        get() = Location(0, 640) + masterOffsetNewUI
}
