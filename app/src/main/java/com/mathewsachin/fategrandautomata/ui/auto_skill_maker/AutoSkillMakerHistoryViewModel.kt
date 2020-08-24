package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class AutoSkillMakerHistoryViewModel @ViewModelInject constructor(
    @Assisted val SavedState: SavedStateHandle
) : ViewModel() {

    private val separator = ";"
    private val skillCmd = mutableListOf<String>()

    val adapter = AutoSkillMakerHistoryAdapter(skillCmd)

    init {
        val items = SavedState.get<String>(::skillCmd.name)

        if (items != null) {
            skillCmd.addAll(items.split(separator))
        }
    }

    fun getSkillCmdString() = skillCmd.joinToString("")

    fun saveState() {
        SavedState.set(::skillCmd.name, skillCmd.joinToString(separator))
    }

    fun add(Cmd: String) {
        skillCmd.add(Cmd)

        adapter.notifyItemInserted(skillCmd.lastIndex)
    }

    fun undo() {
        val pos = skillCmd.lastIndex

        skillCmd.removeAt(pos)

        adapter.notifyItemRemoved(pos)
    }

    fun isEmpty() = skillCmd.isEmpty()

    var last
        get() = skillCmd.last()
        set(value) {
            skillCmd[skillCmd.lastIndex] = value

            adapter.notifyItemChanged(skillCmd.lastIndex)
        }

    fun reverseIterate() = skillCmd.reversed()
}