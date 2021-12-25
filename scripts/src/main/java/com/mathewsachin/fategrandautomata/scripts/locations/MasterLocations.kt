package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.libautomata.AutomataApi
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class MasterLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    private val images: IImageLoader,
    private val automataApi: AutomataApi,
    private val messages: IScriptMessages,
): IScriptAreaTransforms by scriptAreaTransforms {
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
        val location = Location(x, 620)

        if (isNewUI)
            location + Location(178, 0) + masterOffsetNewUI
        else location.xFromRight()
    }

    val stageCountRegion
        get() = when {
            isNewUI -> Region(if (isWide) -571 else -638, 23, 33, 53) + masterOffsetNewUI
            gameServer == GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            else -> Region(1722, 25, 46, 53)
        }

    val masterSkillOpenClick
        get() =
            if (isNewUI)
                Location(0, 640) + masterOffsetNewUI
            else Location(-180, 640).xFromRight()
}