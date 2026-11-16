package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

/**
 * Checks shared by the Servant, skill and CE enhancement screens.
 */
@ScriptScope
class EnhancementScreen @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {

    /**
     * The empty slot shown when no Servant (or CE, on the CE enhancement screen) is selected.
     */
    fun isNothingSelected() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    fun isSkillEnhancementMenu() = images[Images.SkillMenuBanner] in locations.enhancementBannerRegion

    /**
     * Temporary Servants get a warning dialog before the confirmation dialog.
     */
    fun isTemporaryServantWarning() = images[Images.Execute] in locations.tempServantEnhancementRegion

    val okButtonPatterns
        get() = when (prefs.gameServer) {
            // KR has 2 OK buttons
            is GameServer.Kr -> listOf(images[Images.Ok], images[Images.OkKR])
            else -> listOf(images[Images.Ok])
        }

    fun isOkButtonIn(region: Region) = okButtonPatterns in region
}
