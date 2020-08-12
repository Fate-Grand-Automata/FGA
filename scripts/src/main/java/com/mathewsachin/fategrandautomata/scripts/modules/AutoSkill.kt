package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.ScriptExitException
import kotlin.time.Duration
import kotlin.time.seconds

typealias AutoSkillMap = Map<Char, () -> Unit>

class AutoSkill(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private val defaultFunctionArray: AutoSkillMap = listOf(
        Skill.Servant.list.map {
            it.autoSkillCode to { castSkill(it) }
        },
        Skill.Master.list.map {
            it.autoSkillCode to { castMasterSkill(it) }
        },
        CommandCard.NP.list.map {
            it.autoSkillCode to { castNoblePhantasm(it) }
        },
        ServantTarget.list.map {
            it.autoSkillCode to { selectSkillTarget(it) }
        },
        listOf(
            'x' to { beginOrderChange() },
            't' to { selectTarget() },
            'n' to { useCommandCardsBeforeNp() },
            '0' to { }
        )
    ).flatten().toMap()

    private val startingMemberFunctionArray: AutoSkillMap =
        OrderChangeMember.Starting.list
            .associate { it.autoSkillCode to { selectStartingMember(it) } }

    private val subMemberFunctionArray: AutoSkillMap =
        OrderChangeMember.Sub.list
            .associate { it.autoSkillCode to { selectSubMember(it) } }

    private val enemyTargetArray: AutoSkillMap = EnemyTarget.list
        .associate { it.autoSkillCode to { selectEnemyTarget(it) } }

    private val cardsPressedArray: AutoSkillMap = mapOf(
        '1' to { pressCards(1) },
        '2' to { pressCards(2) }
    )

    private var currentArray = defaultFunctionArray

    private lateinit var battle: Battle
    private lateinit var card: Card

    var isFinished = false
        private set

    private fun waitForAnimationToFinish(Timeout: Duration = 5.seconds) {
        val img = images.battle

        // slow devices need this. do not remove.
        game.battleScreenRegion.waitVanish(img, prefs.skillDelay)

        game.battleScreenRegion.exists(img, Timeout)
    }

    private fun castSkill(skill: Skill) {
        skill.clickLocation.click()

        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }

        waitForAnimationToFinish()
    }

    private fun selectSkillTarget(target: ServantTarget) {
        target.clickLocation.click()

        0.5.seconds.wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()

        waitForAnimationToFinish()
    }

    private fun castNoblePhantasm(noblePhantasm: CommandCard.NP) {
        if (!battle.hasClickedAttack) {
            battle.clickAttack()

            // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
            2.seconds.wait()
        }

        card.clickNp(noblePhantasm)
    }

    private fun openMasterSkillMenu() {
        game.battleMasterSkillOpenClick.click()

        0.5.seconds.wait()
    }

    private fun castMasterSkill(skill: Skill.Master) {
        openMasterSkillMenu()

        castSkill(skill)
    }

    private fun changeArray(NewArray: AutoSkillMap) {
        currentArray = NewArray
    }

    private fun beginOrderChange() {
        openMasterSkillMenu()

        // Click on order change skill
        Skill.Master.list.last()
            .clickLocation.click()

        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }

        0.3.seconds.wait()

        changeArray(startingMemberFunctionArray)
    }

    private fun selectStartingMember(member: OrderChangeMember.Starting) {
        member.clickLocation.click()

        changeArray(subMemberFunctionArray)
    }

    private fun selectSubMember(member: OrderChangeMember.Sub) {
        member.clickLocation.click()

        0.3.seconds.wait()

        game.battleOrderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        1.seconds.wait()

        waitForAnimationToFinish(15.seconds)

        // Extra wait for the lag introduced by Order change
        1.seconds.wait()

        changeArray(defaultFunctionArray)
    }

    private fun selectTarget() = changeArray(enemyTargetArray)

    private fun selectEnemyTarget(enemyTarget: EnemyTarget) {
        enemyTarget.clickLocation.click()

        0.5.seconds.wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()

        changeArray(defaultFunctionArray)
    }

    private fun useCommandCardsBeforeNp() {
        if (!battle.hasClickedAttack) {
            battle.clickAttack()

            // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
            2.seconds.wait()
        }

        changeArray(cardsPressedArray)
    }

    private fun pressCards(NoOfCards: Int) {
        card.clickCommandCards(NoOfCards)

        changeArray(defaultFunctionArray)
    }

    fun resetState() {
        isFinished = false

        changeArray(defaultFunctionArray)
    }

    private var commandTable = emptyList<List<String>>()

    private fun validate(cmd: String) {
        if (cmd != "0") {
            when {
                """^[1-3]""".toRegex().containsMatchIn(cmd) -> {
                    throw ScriptExitException("Error at '${cmd}': Skill Command cannot start with number '1', '2' and '3'!")
                }
                cmd.contains('#') -> {
                    throw ScriptExitException("Error at '${cmd}': '#' must be preceded and followed by ','! Correct: ',#,'")
                }
                """[^a-l1-6#ntx]""".toRegex().containsMatchIn(cmd) -> {
                    throw ScriptExitException("Error at '${cmd}': Skill Command exceeded alphanumeric range! Expected 'x', 'n', 't' or range 'a' to 'l' for alphabets and '0' to '6' for numbers.")
                }
            }
        }
    }

    private fun initCommands() {
        val waves = prefs.selectedAutoSkillConfig.skillCommand
            .split(",#,")

        commandTable = waves
            .map {
                val turns = it.split(',')
                turns.forEach { cmd -> validate(cmd) }

                turns
            }
    }

    fun init(BattleModule: Battle, CardModule: Card) {
        battle = BattleModule
        card = CardModule

        initCommands()

        resetState()
    }

    private fun getCommandListFor(Stage: Int, Turn: Int): String {
        if (Stage < commandTable.size) {
            val commandList = commandTable[Stage]

            if (Turn < commandList.size) {
                return commandList[Turn]
            }
        }

        return ""
    }

    private fun executeCommandList(CommandList: String) {
        for (command in CommandList) {
            currentArray[command]?.invoke()
        }
    }

    fun execute() {
        val commandList = getCommandListFor(battle.currentStage, battle.currentTurn)

        if (commandList.isNotEmpty()) {
            executeCommandList(commandList)
        } else if (battle.currentStage >= commandTable.lastIndex) {
            // this will allow NP spam after all commands have been executed
            isFinished = true
        }
    }
}