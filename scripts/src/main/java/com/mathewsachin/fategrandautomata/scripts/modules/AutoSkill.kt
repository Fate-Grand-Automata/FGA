package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.ScriptExitException
import kotlin.time.Duration
import kotlin.time.seconds

typealias AutoSkillMap = Map<Char, () -> Unit>

class AutoSkill(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private val defaultFunctionArray: AutoSkillMap = mapOf(
        'a' to { castSkill(game.battleSkill1Click) },
        'b' to { castSkill(game.battleSkill2Click) },
        'c' to { castSkill(game.battleSkill3Click) },
        'd' to { castSkill(game.battleSkill4Click) },
        'e' to { castSkill(game.battleSkill5Click) },
        'f' to { castSkill(game.battleSkill6Click) },
        'g' to { castSkill(game.battleSkill7Click) },
        'h' to { castSkill(game.battleSkill8Click) },
        'i' to { castSkill(game.battleSkill9Click) },

        'j' to { castMasterSkill(game.battleMasterSkill1Click) },
        'k' to { castMasterSkill(game.battleMasterSkill2Click) },
        'l' to { castMasterSkill(game.battleMasterSkill3Click) },

        'x' to { beginOrderChange() },
        't' to { selectTarget() },
        'n' to { useCommandCardsBeforeNp() },

        '0' to { },

        '1' to { selectSkillTarget(game.battleServant1Click) },
        '2' to { selectSkillTarget(game.battleServant2Click) },
        '3' to { selectSkillTarget(game.battleServant3Click) },

        '4' to { castNoblePhantasm(0) },
        '5' to { castNoblePhantasm(1) },
        '6' to { castNoblePhantasm(2) }
    )

    private val startingMemberFunctionArray: AutoSkillMap = mapOf(
        '1' to { selectStartingMember(game.battleStartingMember1Click) },
        '2' to { selectStartingMember(game.battleStartingMember2Click) },
        '3' to { selectStartingMember(game.battleStartingMember3Click) }
    )

    private val subMemberFunctionArray: AutoSkillMap = mapOf(
        '1' to { selectSubMember(game.battleSubMember1Click) },
        '2' to { selectSubMember(game.battleSubMember2Click) },
        '3' to { selectSubMember(game.battleSubMember3Click) }
    )

    private val enemyTargetArray: AutoSkillMap = mapOf(
        '1' to { selectEnemyTarget(game.battleTargetClickArray[0]) },
        '2' to { selectEnemyTarget(game.battleTargetClickArray[1]) },
        '3' to { selectEnemyTarget(game.battleTargetClickArray[2]) }
    )

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

    private fun castSkill(Location: Location) {
        Location.click()

        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }

        waitForAnimationToFinish()
    }

    private fun selectSkillTarget(Location: Location) {
        Location.click()

        0.5.seconds.wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()

        waitForAnimationToFinish()
    }

    private fun castNoblePhantasm(index: Int) {
        if (!battle.hasClickedAttack) {
            battle.clickAttack()

            // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
            2.seconds.wait()
        }

        card.clickNp(index)
    }

    private fun openMasterSkillMenu() {
        game.battleMasterSkillOpenClick.click()

        0.5.seconds.wait()
    }

    private fun castMasterSkill(Location: Location) {
        openMasterSkillMenu()

        castSkill(Location)
    }

    private fun changeArray(NewArray: AutoSkillMap) {
        currentArray = NewArray
    }

    private fun beginOrderChange() {
        openMasterSkillMenu()

        game.battleMasterSkill3Click.click()

        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }

        0.3.seconds.wait()

        changeArray(startingMemberFunctionArray)
    }

    private fun selectStartingMember(Location: Location) {
        Location.click()

        changeArray(subMemberFunctionArray)
    }

    private fun selectSubMember(Location: Location) {
        Location.click()

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

    private fun selectEnemyTarget(Location: Location) {
        Location.click()

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

    private val commandTable = mutableListOf<MutableList<String>>()

    private fun initCommands() {
        var stageCount = 0

        for (commandList in prefs.selectedAutoSkillConfig.skillCommand.splitToSequence(',')) {
            if (commandList != "0") {
                if (Regex("""^[1-3]""").containsMatchIn(commandList)) {
                    throw ScriptExitException("Error at '${commandList}': Skill Command cannot start with number '1', '2' and '3'!")
                }

                if (Regex("""([^,]#)|(#[^,])""").containsMatchIn(commandList)) {
                    throw ScriptExitException("Error at '${commandList}': '#' must be preceded and followed by ','! Correct: ',#,'")
                }

                if (Regex("""[^a-l1-6#ntx]""").containsMatchIn(commandList)) {
                    throw ScriptExitException("Error at '${commandList}': Skill Command exceeded alphanumeric range! Expected 'x', 'n', 't' or range 'a' to 'l' for alphabets and '0' to '6' for numbers.")
                }
            }

            if (stageCount >= commandTable.size) {
                commandTable.add(mutableListOf())
            }

            if (commandList == "#") {
                ++stageCount
            } else commandTable[stageCount].add(commandList)
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
        } else if (battle.currentStage + 1 >= commandTable.size) {
            // this will allow NP spam after all commands have been executed
            isFinished = true
        }
    }
}