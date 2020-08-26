package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*

class AutoSkillMakerViewModel @ViewModelInject constructor(
    @Assisted val savedState: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    val state = savedState.get(::savedState.name)
        ?: AutoSkillMakerSavedState()

    private var currentSkill = state.currentSkill

    fun saveState() {
        val saveState = AutoSkillMakerSavedState(
            state.skillCommand,
            enemyTarget.value ?: NoEnemy,
            stage.value ?: 1,
            turn.value ?: 1,
            currentView.value ?: AutoSkillMakerViewState.Main,
            currentSkill,
            npSequence.value ?: emptyList(),
            cardsBeforeNp.value ?: 0,
            xSelectedParty.value ?: 1,
            xSelectedSub.value ?: 1
        )

        savedState.set(::savedState.name, saveState)
    }

    override fun onCleared() {
        super.onCleared()

        saveState()
    }

    private val _skillCommand = MutableLiveData(state.skillCommand)

    val skillCommand: LiveData<List<String>> = Transformations.map(_skillCommand) { it }

    private fun notifySkillCommandUpdate() {
        _skillCommand.value = state.skillCommand
    }

    private fun getSkillCmdString() = state.skillCommand.joinToString("")

    private fun add(Cmd: String) {
        state.skillCommand.add(Cmd)

        notifySkillCommandUpdate()
    }

    private fun undo() {
        val pos = state.skillCommand.lastIndex

        state.skillCommand.removeAt(pos)

        notifySkillCommandUpdate()
    }

    private fun isEmpty() = state.skillCommand.isEmpty()

    private var last
        get() = state.skillCommand.last()
        set(value) {
            state.skillCommand[state.skillCommand.lastIndex] = value

            notifySkillCommandUpdate()
        }

    private fun reverseIterate() = state.skillCommand.reversed()

    private val _enemyTarget = MutableLiveData<Int>(state.enemyTarget)

    val enemyTarget: LiveData<Int> = _enemyTarget

    fun setEnemyTarget(target: Int) {
        _enemyTarget.value = target

        if (target == NoEnemy) {
            return
        }

        val targetCmd = "t${target}"

        // Merge consecutive target changes
        if (!isEmpty() && last[0] == 't') {
            last = targetCmd
        } else {
            add(targetCmd)
        }
    }

    fun unSelectTargets() = setEnemyTarget(NoEnemy)

    private val _cardsBeforeNp = MutableLiveData<Int>(state.cardsBeforeNp)

    val cardsBeforeNp: LiveData<Int> = _cardsBeforeNp

    fun setCardsBeforeNp(cards: Int) {
        _cardsBeforeNp.value = cards
    }

    private val _stage = MutableLiveData<Int>(state.stage)
    private val _turn = MutableLiveData<Int>(state.turn)

    fun MutableLiveData<Int>.next() {
        value = (value ?: 1) + 1
    }

    fun MutableLiveData<Int>.prev() {
        value = (value ?: 1) - 1
    }

    val stage: LiveData<Int> = _stage
    val turn: LiveData<Int> = _turn

    private fun nextStage() = _stage.next()
    private fun nextTurn() = _turn.next()

    private fun prevStage() = _stage.prev()
    private fun prevTurn() = _turn.prev()

    val currentView = MutableLiveData<AutoSkillMakerViewState>(state.currentView)

    fun gotToMain() {
        currentView.value = AutoSkillMakerViewState.Main
    }

    fun onSkill(SkillCode: Char) {
        currentSkill = SkillCode

        currentView.value = AutoSkillMakerViewState.Target
    }

    // Data Binding doesn't seem to work with default parameters or null
    fun onSkillTarget() = onSkillTarget(null)

    fun onSkillTarget(TargetCommand: Char?) {
        var cmd = currentSkill.toString()

        if (TargetCommand != null) {
            cmd += TargetCommand
        }

        add(cmd)

        gotToMain()
    }

    fun onNpClick(command: Char) {
        npSequence.value?.let { nps ->
            if (nps.contains(command)) {
                _npSequence.value = nps.filterNot { it == command }
            } else _npSequence.value = nps + command
        }
    }

    private fun clearNpSequence() {
        _npSequence.value = emptyList()
    }

    fun finish(): String {
        addNpsToSkillCmd()

        return getSkillCmdString()
    }

    private fun addNpsToSkillCmd() {
        npSequence.value?.let { nps ->
            if (nps.isNotEmpty()) {
                when (cardsBeforeNp.value) {
                    1 -> add("n1")
                    2 -> add("n2")
                }
            }

            // Show each NP as separate entry
            for (np in nps) {
                add(np.toString())
            }

            // Add a '0' before consecutive turn/battle changes
            if (!isEmpty() && last.last() == ',') {
                add("0")
            }

            clearNpSequence()
        }
    }

    fun onGoToNext(Separator: String) {
        // Uncheck selected targets
        unSelectTargets()

        addNpsToSkillCmd()

        if (isEmpty()) {
            add("0")
        }

        add(Separator)

        nextTurn()

        gotToMain()
    }

    fun goToNextTurn() = onGoToNext(",")

    fun goToNextStage() {
        nextStage()
        onGoToNext(",#,")
    }

    private val _xSelectedParty = MutableLiveData<Int>(state.xSelectedParty)
    private val _xSelectedSub = MutableLiveData<Int>(state.xSelectedSub)

    val xSelectedParty: LiveData<Int> = _xSelectedParty
    val xSelectedSub: LiveData<Int> = _xSelectedSub

    fun setOrderChangePartyMember(member: Int) {
        _xSelectedParty.value = member
    }

    fun setOrderChangeSubMember(member: Int) {
        _xSelectedSub.value = member
    }

    fun goToOrderChange() {
        currentView.value = AutoSkillMakerViewState.OrderChange

        setOrderChangePartyMember(1)
        setOrderChangeSubMember(1)
    }

    fun orderChangeOk() {
        add("x${xSelectedParty.value}${xSelectedSub.value}")

        gotToMain()
    }

    fun canGoBack() = currentView.value != AutoSkillMakerViewState.Main

    fun goBack() = gotToMain()

    private fun revertToPreviousEnemyTarget() {
        // Find the previous target, but within the same turn
        val previousTarget = reverseIterate()
            .takeWhile { !it.contains(',') }
            .firstOrNull { it.startsWith('t') }

        if (previousTarget == null) {
            unSelectTargets()
            return
        }

        val target = when (previousTarget[1]) {
            '1' -> 1
            '2' -> 2
            '3' -> 3
            else -> return
        }

        _enemyTarget.value = target
    }

    private fun undoStageOrTurn() {
        // Decrement Battle/Turn count
        if (last.contains('#')) {
            prevStage()
        }

        prevTurn()

        // Undo the Battle/Turn change
        undo()

        val itemsToRemove = setOf('4', '5', '6', 'n', '0')

        // Remove NPs and cards before NPs
        while (!isEmpty()
            && last[0] in itemsToRemove
        ) {
            undo()
        }

        revertToPreviousEnemyTarget()
    }

    fun onUndo(alertDialog: (onPositiveClick: () -> Unit) -> Unit) {
        if (!isEmpty()) {
            // Un-select target
            when {
                last.startsWith('t') -> {
                    undo()
                    revertToPreviousEnemyTarget()
                }
                // Battle/Turn change
                last.contains(',') -> {
                    alertDialog {
                        undoStageOrTurn()
                    }
                }
                else -> undo()
            }
        }
    }

    private val _npSequence = MutableLiveData<List<Char>>(state.npSequence)

    val npSequence: LiveData<List<Char>> = _npSequence

    fun goToAtk() {
        clearNpSequence()

        setCardsBeforeNp(0)

        currentView.value = AutoSkillMakerViewState.Atk
    }
}