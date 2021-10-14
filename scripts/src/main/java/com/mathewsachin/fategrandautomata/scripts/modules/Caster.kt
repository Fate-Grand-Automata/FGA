package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class Caster @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val state: BattleState,
    private val servantTracker: ServantTracker
) : IFgoAutomataApi by fgAutomataApi {
    // TODO: Shouldn't be here ideally.
    //  Once we add more spam modes, Skill spam and NP spam can have their own variants.
    fun canSpam(spam: SpamEnum): Boolean {
        val weCanSpam = spam == SpamEnum.Spam
        val weAreInDanger = spam == SpamEnum.Danger
                && state.chosenTarget != null

        return weCanSpam || weAreInDanger
    }

    private fun waitForAnimationToFinish(timeout: Duration = Duration.seconds(5)) {
        val img = images[Images.BattleScreen]

        // slow devices need this. do not remove.
        game.battleScreenRegion.waitVanish(img, Duration.seconds(2))

        game.battleScreenRegion.exists(img, timeout)
    }

    private fun confirmSkillUse() {
        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }
    }

    private fun castSkill(skill: Skill, target: ServantTarget?) {
        game.locate(skill).click()
        confirmSkillUse()

        if (target != null) {
            prefs.skillDelay.wait()

            selectSkillTarget(target)
        } else {
            // Close the window that opens up if skill is on cool-down
            game.battleExtraInfoWindowCloseClick.click()
        }

        waitForAnimationToFinish()
    }

    fun castServantSkill(skill: Skill.Servant, target: ServantTarget?) {
        castSkill(skill, target)
    }

    fun selectSkillTarget(target: ServantTarget) {
        val actualTarget = when (target) {
            ServantTarget.Left, ServantTarget.Right -> target
            else -> {
                val deployed = servantTracker.deployed

                // How many servants on field?
                when (deployed.size) {
                    1 -> ServantTarget.B
                    2 -> {
                        when (target) {
                            ServantTarget.A -> ServantTarget.Left
                            ServantTarget.C -> ServantTarget.Right
                            ServantTarget.B -> {
                                when (null) {
                                    deployed[FieldSlot.A] -> ServantTarget.Left
                                    deployed[FieldSlot.C] -> ServantTarget.Right
                                    else -> ServantTarget.Left // Assume Left when Slot B is empty
                                }
                            }
                            else -> target
                        }
                    }
                    else -> target
                }
            }
        }

        game.locate(actualTarget).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun openMasterSkillMenu() {
        game.battleMasterSkillOpenClick.click()

        Duration.seconds(0.5).wait()
    }

    fun castMasterSkill(skill: Skill.Master, target: ServantTarget? = null) {
        openMasterSkillMenu()

        castSkill(skill, target)
    }

    fun orderChange(action: AutoSkillAction.OrderChange) {
        openMasterSkillMenu()

        // Click on order change skill
        game.locate(Skill.Master.C).click()

        confirmSkillUse()

        Duration.seconds(0.3).wait()

        game.locate(action.starting).click()
        game.locate(action.sub).click()

        Duration.seconds(0.3).wait()

        game.battleOrderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        Duration.seconds(1).wait()

        waitForAnimationToFinish(Duration.seconds(15))

        // Extra wait for the lag introduced by Order change
        Duration.seconds(1).wait()

        servantTracker.orderChanged(action.starting, action.sub)
    }

    fun selectEnemyTarget(enemy: EnemyTarget) {
        game.locate(enemy).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    fun use(np: CommandCard.NP) {
        game.clickLocation(np).click()

        game.battleExtraInfoWindowCloseClick.click()
    }

    fun use(card: CommandCard.Face) {
        game.clickLocation(card).click()
    }
}