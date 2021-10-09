package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.NPSpamEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SkillSpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import kotlin.time.Duration

class AutoSkill(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var battle: Battle
    private lateinit var card: Card

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

    private fun castServantSkill(skill: Skill.Servant, target: ServantTarget?) {
        castSkill(skill, target)
    }

    private fun selectSkillTarget(target: ServantTarget) {
        game.locate(target).click()

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

    private fun orderChange(action: AutoSkillAction.OrderChange) {
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

        battle.servantTracker.orderChanged(action.starting, action.sub)
    }

    private fun selectEnemyTarget(enemy: EnemyTarget) {
        game.locate(enemy).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> card.atk = action
        is AutoSkillAction.ServantSkill -> castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> orderChange(action)
    }

    private fun isDanger() = battle.state.chosenTarget != null

    private fun isTargetNPAvailable(config: SkillSpamConfig, slot: ServantSlot) =
        npsAvailable[config.determineTargetSlot(slot).position - 1]

    fun canSpam(config: SkillSpamConfig, slot: ServantSlot) =
        when (config.spam) {
            SkillSpamEnum.None -> false
            SkillSpamEnum.Spam -> true
            SkillSpamEnum.Danger -> isDanger()
            SkillSpamEnum.WithNP -> isTargetNPAvailable(config, slot)
            SkillSpamEnum.ChargeNP -> !isTargetNPAvailable(config, slot)
        }
                && (battle.state.stage + 1) in config.waves

    fun canSpam(config: NpSpamConfig, slot: ServantSlot) =
        when (config.spam) {
            NPSpamEnum.None -> false
            NPSpamEnum.Spam -> true
            NPSpamEnum.Danger -> isDanger()
        }
                && (battle.state.stage + 1) in config.waves
                && npsAvailable[slot.position - 1]

    val skillSpamDelay = Duration.seconds(0.25)

    private fun skillSpam() {
        val npChargingSkills = mutableListOf<SkillSpamEntry>()
        val regularSkills = mutableListOf<SkillSpamEntry>()
        val withNPSkills = mutableListOf<SkillSpamEntry>()

        ServantSlot.list.forEach { servantSlot ->
            val skills = servantSlot.skills()
            val teamSlot = battle.servantTracker.deployed[servantSlot] ?: ServantTracker.TeamSlot.A
            val servantSpamConfig = battle.spamConfig.getOrElse(teamSlot.position - 1) { ServantSpamConfig() }

            val entries = servantSpamConfig.skills.zip(skills) { spamConfig, skill ->
                SkillSpamEntry(
                    skill = skill,
                    config = spamConfig,
                    slot = servantSlot,
                    teamSlot = teamSlot
                )
            }

            for (entry in entries) {
                val listToAddTo = when (entry.config.spam) {
                    SkillSpamEnum.None -> null
                    SkillSpamEnum.Spam, SkillSpamEnum.Danger -> regularSkills
                    SkillSpamEnum.WithNP -> withNPSkills
                    SkillSpamEnum.ChargeNP -> npChargingSkills
                }
                listToAddTo?.add(entry)
            }
        }

        spamNPChargingSkills(npChargingSkills)

        regularSkills.forEach { spam(it) }

        if (withNPSkills.isNotEmpty()) {
            detectNps()

            withNPSkills.forEach { spam(it) }
        }
    }

    private fun spamNPChargingSkills(entries: List<SkillSpamEntry>) {
        var lastSpammed = true
        for (entry in entries) {
            if (lastSpammed) {
                detectNps()
            }

            lastSpammed = spam(entry)
        }
    }

    class SkillSpamEntry(
        val skill: Skill.Servant,
        val config: SkillSpamConfig,
        val slot: ServantSlot,
        val teamSlot: ServantTracker.TeamSlot
    )

    private fun spam(entry: SkillSpamEntry): Boolean {
        if (canSpam(entry.config, slot = entry.slot)) {
            val skillImage = battle.servantTracker
                .checkImages[entry.teamSlot]
                ?.skills
                ?.getOrNull(entry.skill.index)

            if (skillImage != null) {
                // Some delay for skill icon to be loaded
                skillSpamDelay.wait()

                if (skillImage in game.imageRegion(entry.skill)) {
                    val target = entry.config.determineTarget(entry.slot)

                    castSkill(entry.skill, target)
                    return true
                }
            }
        }

        return false
    }

    private fun SkillSpamConfig.determineTarget(servantSlot: ServantSlot) =
        when (target) {
            SkillSpamTarget.None -> null
            SkillSpamTarget.Self -> when (servantSlot) {
                ServantSlot.A -> ServantTarget.A
                ServantSlot.B -> ServantTarget.B
                ServantSlot.C -> ServantTarget.C
            }
            SkillSpamTarget.Slot1 -> ServantTarget.A
            SkillSpamTarget.Slot2 -> ServantTarget.B
            SkillSpamTarget.Slot3 -> ServantTarget.C
            SkillSpamTarget.Left -> ServantTarget.Left
            SkillSpamTarget.Right -> ServantTarget.Right
        }

    private fun SkillSpamConfig.determineTargetSlot(servantSlot: ServantSlot) =
        when (target) {
            SkillSpamTarget.None, SkillSpamTarget.Left, SkillSpamTarget.Right, SkillSpamTarget.Self -> servantSlot
            SkillSpamTarget.Slot1 -> ServantSlot.A
            SkillSpamTarget.Slot2 -> ServantSlot.B
            SkillSpamTarget.Slot3 -> ServantSlot.C
        }

    lateinit var commandTable: AutoSkillCommand

    fun init(BattleModule: Battle, CardModule: Card) {
        battle = BattleModule
        card = CardModule

        commandTable = AutoSkillCommand.parse(
            prefs.selectedBattleConfig.skillCommand
        )
    }

    private var npsAvailable = (1..3).map { false }

    private fun detectNps() {
        infix fun List<Boolean>.and(other: List<Boolean>) =
            zip(other) { a, b -> a && b }

        npsAvailable = (1..7)
            .map {
                Duration.milliseconds(150).wait()

                useColor {
                    useSameSnapIn {
                        game.servantNPCheckRegions.map {
                            images[Images.ServantExist] in it
                        }
                    }
                }
            }
            .reduce { acc, list -> acc and list }
            .map { !it }
    }

    fun execute() {
        val stage = battle.state.stage
        val turn = battle.state.turn

        val commandList = commandTable[stage, turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        }

        skillSpam()

        if (npSpamThisWave()) {
            detectNps()
        }
    }

    private fun npSpamThisWave() =
        battle.servantTracker.deployed
            .values
            .filterNotNull()
            .any { teamSlot ->
                val spamConfig = battle.spamConfig.getOrElse(teamSlot.position - 1) { ServantSpamConfig() }

                spamConfig.np.spam != NPSpamEnum.None
            }
}