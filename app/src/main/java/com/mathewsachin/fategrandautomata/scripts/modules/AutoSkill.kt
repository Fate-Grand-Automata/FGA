package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.Duration
import kotlin.time.seconds

typealias AutoSkillMap = Map<Char, () -> Unit>

class AutoSkill {
    private val defaultFunctionArray: AutoSkillMap = mapOf(
        'a' to { castSkill(Game.BattleSkill1Click) },
        'b' to { castSkill(Game.BattleSkill2Click) },
        'c' to { castSkill(Game.BattleSkill3Click) },
        'd' to { castSkill(Game.BattleSkill4Click) },
        'e' to { castSkill(Game.BattleSkill5Click) },
        'f' to { castSkill(Game.BattleSkill6Click) },
        'g' to { castSkill(Game.BattleSkill7Click) },
        'h' to { castSkill(Game.BattleSkill8Click) },
        'i' to { castSkill(Game.BattleSkill9Click) },

        'j' to { castMasterSkill(Game.BattleMasterSkill1Click) },
        'k' to { castMasterSkill(Game.BattleMasterSkill2Click) },
        'l' to { castMasterSkill(Game.BattleMasterSkill3Click) },

        'x' to { beginOrderChange() },
        't' to { selectTarget() },
        'n' to { useCommandCardsBeforeNp() },

        '0' to { },

        '1' to { selectSkillTarget(Game.BattleServant1Click) },
        '2' to { selectSkillTarget(Game.BattleServant2Click) },
        '3' to { selectSkillTarget(Game.BattleServant3Click) },

        '4' to { castNoblePhantasm(Game.BattleNpCardClickArray[0]) },
        '5' to { castNoblePhantasm(Game.BattleNpCardClickArray[1]) },
        '6' to { castNoblePhantasm(Game.BattleNpCardClickArray[2]) }
    )

    private val startingMemberFunctionArray: AutoSkillMap = mapOf(
        '1' to { selectStartingMember(Game.BattleStartingMember1Click) },
        '2' to { selectStartingMember(Game.BattleStartingMember2Click) },
        '3' to { selectStartingMember(Game.BattleStartingMember3Click) }
    )

    private val subMemberFunctionArray: AutoSkillMap = mapOf(
        '1' to { selectSubMember(Game.BattleSubMember1Click) },
        '2' to { selectSubMember(Game.BattleSubMember2Click) },
        '3' to { selectSubMember(Game.BattleSubMember3Click) }
    )

    private val enemyTargetArray: AutoSkillMap = mapOf(
        '1' to { selectEnemyTarget(Game.BattleTargetClickArray[0]) },
        '2' to { selectEnemyTarget(Game.BattleTargetClickArray[1]) },
        '3' to { selectEnemyTarget(Game.BattleTargetClickArray[2]) }
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

    var npsClicked = false
        private set

    private fun waitForAnimationToFinish(Timeout: Duration = 5.seconds) {
        val img = ImageLocator.Battle

        // slow devices need this. do not remove.
        Game.BattleScreenRegion.waitVanish(img, 2.seconds)

        Game.BattleScreenRegion.exists(img, Timeout)
    }

    private fun castSkill(Location: Location) {
        Location.click()

        if (Preferences.SkillConfirmation) {
            Game.BattleSkillOkClick.click()
        }

        waitForAnimationToFinish()
    }

    private fun selectSkillTarget(Location: Location) {
        Location.click()

        0.5.seconds.wait()

        // Exit any extra menu
        Game.BattleExtrainfoWindowCloseClick.click()

        waitForAnimationToFinish()
    }

    private fun castNoblePhantasm(Location: Location) {
        if (!battle.hasClickedAttack) {
            battle.clickAttack()

            // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
            2.seconds.wait()
        }

        Location.click()

        npsClicked = true
    }

    private fun openMasterSkillMenu() {
        Game.BattleMasterSkillOpenClick.click()

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

        Game.BattleMasterSkill3Click.click()

        if (Preferences.SkillConfirmation) {
            Game.BattleSkillOkClick.click()
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

        Game.BattleOrderChangeOkClick.click()

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
        Game.BattleExtrainfoWindowCloseClick.click()

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
        isFinished = !Preferences.EnableAutoSkill

        changeArray(defaultFunctionArray)
    }

    private val commandTable = mutableListOf<MutableList<String>>()

    private fun initCommands() {
        var stageCount = 0

        for (commandList in Preferences.SkillCommand.splitToSequence(',')) {
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

        if (Preferences.EnableAutoSkill) {
            initCommands()
        }

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

    fun execute(): Boolean {
        val commandList = getCommandListFor(battle.currentStage, battle.currentTurn)

        if (commandList.isNotEmpty()) {
            executeCommandList(commandList)
        } else if (battle.currentStage + 1 >= commandTable.size) {
            // this will allow NP spam after all commands have been executed
            isFinished = true
        }

        return npsClicked
    }

    fun resetNpTimer() {
        npsClicked = false
    }
}