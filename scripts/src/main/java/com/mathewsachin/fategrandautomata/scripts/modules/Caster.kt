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
    api: IFgoAutomataApi,
    private val state: BattleState,
    private val servantTracker: ServantTracker
) : IFgoAutomataApi by api {
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
        locations.battle.screenCheckRegion.waitVanish(img, Duration.seconds(2))

        locations.battle.screenCheckRegion.exists(img, timeout)
    }

    private fun confirmSkillUse() {
        if (prefs.skillConfirmation) {
            locations.battle.skillOkClick.click()
        }
    }

    private fun castSkill(skill: Skill, target: ServantTarget?) {
        when (skill) {
            is Skill.Master -> locations.battle.master.locate(skill)
            is Skill.Servant -> locations.battle.locate(skill)
        }.click()

        confirmSkillUse()

        if (target != null) {
            prefs.skillDelay.wait()

            selectSkillTarget(target)
        } else {
            // Close the window that opens up if skill is on cool-down
            locations.battle.extraInfoWindowCloseClick.click()
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

        locations.battle.locate(actualTarget).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        locations.battle.extraInfoWindowCloseClick.click()
    }

    private fun openMasterSkillMenu() {
        locations.battle.master.masterSkillOpenClick.click()

        Duration.seconds(0.5).wait()
    }

    fun castMasterSkill(skill: Skill.Master, target: ServantTarget? = null) {
        openMasterSkillMenu()

        castSkill(skill, target)
    }

    fun orderChange(action: AutoSkillAction.OrderChange) {
        openMasterSkillMenu()

        // Click on order change skill
        locations.battle.master.locate(Skill.Master.C).click()

        confirmSkillUse()

        Duration.seconds(0.3).wait()

        locations.battle.locate(action.starting).click()
        locations.battle.locate(action.sub).click()

        Duration.seconds(0.3).wait()

        locations.battle.orderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        Duration.seconds(1).wait()

        waitForAnimationToFinish(Duration.seconds(15))

        // Extra wait for the lag introduced by Order change
        Duration.seconds(1).wait()

        servantTracker.orderChanged(action.starting, action.sub)
    }

    fun selectEnemyTarget(enemy: EnemyTarget) {
        locations.battle.locate(enemy).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        locations.battle.extraInfoWindowCloseClick.click()
    }

    fun use(np: CommandCard.NP) {
        locations.attack.clickLocation(np).click()

        locations.battle.extraInfoWindowCloseClick.click()
    }

    fun use(card: CommandCard.Face) {
        locations.attack.clickLocation(card).click()
    }
}