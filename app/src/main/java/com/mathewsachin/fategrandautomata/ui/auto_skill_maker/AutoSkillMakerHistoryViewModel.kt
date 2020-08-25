package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.content.Context
import android.os.Parcelable
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.parcel.Parcelize

class AutoSkillMakerHistoryViewModel @ViewModelInject constructor(
    @Assisted val savedState: SavedStateHandle,
    @ApplicationContext val context: Context
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    @Parcelize
    data class AutoSkillMakerViewModelState(
        val skillCommand: MutableList<String> = mutableListOf(),
        var enemyTarget: Int = -1,
        var stage: Int = 1,
        var turn: Int = 1
    ) : Parcelable

    val state = savedState.get(::savedState.name)
        ?: AutoSkillMakerViewModelState()

    override fun onCleared() {
        super.onCleared()

        state.enemyTarget = enemyTarget.value ?: NoEnemy
        state.stage = stage.value ?: 1
        state.turn = turn.value ?: 1
        savedState.set(::savedState.name, state)
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
}