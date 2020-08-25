package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Parcelable
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize

class AutoSkillMakerHistoryViewModel @ViewModelInject constructor(
    @Assisted val savedState: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    @Parcelize
    data class AutoSkillMakerViewModelState(
        val skillCommand: MutableList<String> = mutableListOf(),
        val enemyTarget: Int = -1,
        val stage: Int = 1,
        val turn: Int = 1,
        val currentView: AutoSkillMakerState = AutoSkillMakerState.Main,
        val currentSkill: Char = '0',
        val npSequence: String = "",
        val cardsBeforeNp: Int = 0,
        val xSelectedParty: Int = 1,
        val xSelectedSub: Int = 1
    ) : Parcelable

    val state = savedState.get(::savedState.name)
        ?: AutoSkillMakerViewModelState()

    private var currentSkill = state.currentSkill
    private var npSequence = state.npSequence

    override fun onCleared() {
        super.onCleared()

        val saveState = AutoSkillMakerViewModelState(
            state.skillCommand,
            enemyTarget.value ?: NoEnemy,
            stage.value ?: 1,
            turn.value ?: 1,
            currentView.value ?: AutoSkillMakerState.Main,
            currentSkill,
            npSequence,
            cardsBeforeNp.value ?: 0,
            xSelectedParty.value ?: 1,
            xSelectedSub.value ?: 1
        )

        savedState.set(::savedState.name, saveState)
    }

    val adapter = AutoSkillMakerHistoryAdapter(state.skillCommand)

    fun getSkillCmdString() = state.skillCommand.joinToString("")

    fun add(Cmd: String) {
        state.skillCommand.add(Cmd)

        adapter.notifyItemInserted(state.skillCommand.lastIndex)
    }

    fun undo() {
        val pos = state.skillCommand.lastIndex

        state.skillCommand.removeAt(pos)

        adapter.notifyItemRemoved(pos)
    }

    fun isEmpty() = state.skillCommand.isEmpty()

    var last
        get() = state.skillCommand.last()
        set(value) {
            state.skillCommand[state.skillCommand.lastIndex] = value

            adapter.notifyItemChanged(state.skillCommand.lastIndex)
        }

    fun reverseIterate() = state.skillCommand.reversed()

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

    fun unSelectTargets() {
        setEnemyTarget(AutoSkillMakerHistoryViewModel.NoEnemy)
    }

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

    fun nextStage() = _stage.next()
    fun nextTurn() = _turn.next()

    fun prevStage() = _stage.prev()
    fun prevTurn() = _turn.prev()

    val currentView = MutableLiveData<AutoSkillMakerState>(state.currentView)

    fun gotToMain() {
        currentView.value = AutoSkillMakerState.Main
    }

    fun onSkill(SkillCode: Char) {
        currentSkill = SkillCode

        currentView.value = AutoSkillMakerState.Target
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
        if (npSequence.contains(command)) {
            npSequence = npSequence.filterNot { it == command }
        } else npSequence += command
    }

    fun clearNpSequence() {
        npSequence = ""
    }

    fun addNpsToSkillCmd() {
        if (npSequence.isNotEmpty()) {
            when (cardsBeforeNp.value) {
                1 -> add("n1")
                2 -> add("n2")
            }
        }

        // Show each NP as separate entry
        for (np in npSequence) {
            add(np.toString())
        }

        // Add a '0' before consecutive turn/battle changes
        if (!isEmpty() && last.last() == ',') {
            add("0")
        }

        clearNpSequence()
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
        currentView.value = AutoSkillMakerState.OrderChange

        setOrderChangePartyMember(1)
        setOrderChangeSubMember(1)
    }

    fun orderChangeOk() {
        add("x${xSelectedParty.value}${xSelectedSub.value}")

        gotToMain()
    }

    fun canGoBack() = currentView.value != AutoSkillMakerState.Main

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

        setEnemyTarget(target)
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
}