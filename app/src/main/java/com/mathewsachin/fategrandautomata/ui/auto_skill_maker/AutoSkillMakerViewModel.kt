package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences

class AutoSkillMakerViewModel @ViewModelInject constructor(
    val prefs: IPreferences,
    @Assisted val savedState: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    val autoSkillItemKey: String = savedState[AutoSkillMakerActivityArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get AutoSkill key")

    val autoSkillPrefs = prefs.forAutoSkillConfig(autoSkillItemKey)

    val state = savedState.get(::savedState.name)
        ?: AutoSkillMakerSavedState()

    private val model: AutoSkillMakerModel
    private val _stage: MutableLiveData<Int>

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex

    init {
        model = if (state.skillString != null) {
            AutoSkillMakerModel(state.skillString)
        } else {
            val skillString = autoSkillPrefs.skillCommand
            val m = try {
                AutoSkillMakerModel(skillString)
            } catch (e: Exception) {
                AutoSkillMakerModel("")
            }

            if (skillString.isNotEmpty()) {
                when (val l = m.skillCommand.last()) {
                    is AutoSkillMakerEntry.Action -> when (l.action) {
                        is AutoSkillAction.NP -> m.skillCommand.add(AutoSkillMakerEntry.NextWave)
                    }
                }
            }

            m
        }

        _stage = if (state.skillString != null) {
            MutableLiveData(state.stage)
        } else {
            MutableLiveData(
                model.skillCommand.count { it is AutoSkillMakerEntry.NextWave } + 1
            )
        }

        _currentIndex.value = if (state.skillString != null) {
            state.currentIndex
        } else model.skillCommand.lastIndex
    }

    private var currentSkill = state.currentSkill

    fun saveState() {
        val saveState = AutoSkillMakerSavedState(
            model.toString(),
            enemyTarget.value ?: NoEnemy,
            stage.value ?: 1,
            currentSkill,
            npSequence.value ?: emptyList(),
            cardsBeforeNp.value ?: 0,
            xSelectedParty.value ?: 1,
            xSelectedSub.value ?: 1,
            currentIndex.value ?: 0
        )

        savedState.set(::savedState.name, saveState)
    }

    override fun onCleared() {
        super.onCleared()

        saveState()
    }

    private val _skillCommand = MutableLiveData(model.skillCommand)

    val skillCommand: LiveData<List<AutoSkillMakerEntry>> = Transformations.map(_skillCommand) { it }

    private fun notifySkillCommandUpdate() {
        _skillCommand.value = model.skillCommand
    }

    private fun getSkillCmdString() = model.toString()

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index

        notifySkillCommandUpdate()
        revertToPreviousEnemyTarget()
    }

    private fun add(entry: AutoSkillMakerEntry) {
        model.skillCommand.add((currentIndex.value ?: 0) + 1, entry)
        _currentIndex.next()

        notifySkillCommandUpdate()
    }

    private fun undo() {
        model.skillCommand.removeAt(currentIndex.value ?: 0)
        _currentIndex.prev()

        notifySkillCommandUpdate()
    }

    private fun isEmpty() = currentIndex.value == 0

    private var last: AutoSkillMakerEntry
        get() = model.skillCommand[currentIndex.value ?: 0]
        set(value) {
            model.skillCommand[currentIndex.value ?: 0] = value

            notifySkillCommandUpdate()
        }

    private fun reverseIterate(): List<AutoSkillMakerEntry> =
        model.skillCommand
            .take((currentIndex.value ?: 0) + 1)
            .reversed()

    private val _enemyTarget = MutableLiveData(state.enemyTarget)

    val enemyTarget: LiveData<Int> = _enemyTarget

    fun setEnemyTarget(target: Int) {
        _enemyTarget.value = target

        if (target == NoEnemy) {
            return
        }

        val targetCmd = AutoSkillMakerEntry.Action(
            AutoSkillAction.TargetEnemy(
                EnemyTarget.list[target - 1]
            )
        )

        val l = last

        // Merge consecutive target changes
        if (!isEmpty() && l is AutoSkillMakerEntry.Action && l.action is AutoSkillAction.TargetEnemy) {
            last = targetCmd
        } else {
            add(targetCmd)
        }
    }

    fun unSelectTargets() = setEnemyTarget(NoEnemy)

    val cardsBeforeNp = MutableLiveData(state.cardsBeforeNp)

    fun setCardsBeforeNp(cards: Int) {
        cardsBeforeNp.value = cards
    }

    fun MutableLiveData<Int>.next() {
        value = (value ?: 1) + 1
    }

    fun MutableLiveData<Int>.prev() {
        value = (value ?: 1) - 1
    }

    val stage: LiveData<Int> = _stage
    private fun prevStage() = _stage.prev()

    fun initSkill(SkillCode: Char) {
        currentSkill = SkillCode
    }

    fun targetSkill(TargetCommand: Char?) {
        val skill = (Skill.Servant.list + Skill.Master.list)
            .first { it.autoSkillCode == currentSkill }

        val target = ServantTarget.list.firstOrNull { it.autoSkillCode == TargetCommand }

        add(
            AutoSkillMakerEntry.Action(
                when (skill) {
                    is Skill.Servant -> AutoSkillAction.ServantSkill(skill, target)
                    is Skill.Master -> AutoSkillAction.MasterSkill(skill, target)
                }
            )
        )
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
        _currentIndex.value = model.skillCommand.lastIndex

        while (true) {
            when (val last = last) {
                is AutoSkillMakerEntry.NextWave,
                is AutoSkillMakerEntry.NextTurn -> undo()
                is AutoSkillMakerEntry.Action -> {
                    when (last.action) {
                        AutoSkillAction.NoOp -> undo()
                        else -> break
                    }
                }
                else -> break
            }
        }

        return getSkillCmdString()
    }

    private fun addNpsToSkillCmd() {
        npSequence.value?.let { nps ->
            if (nps.isNotEmpty()) {
                when (cardsBeforeNp.value) {
                    1 -> add(
                        AutoSkillMakerEntry.Action(
                            AutoSkillAction.CardsBeforeNP(1)
                        )
                    )
                    2 -> add(
                        AutoSkillMakerEntry.Action(
                            AutoSkillAction.CardsBeforeNP(2)
                        )
                    )
                }
            }

            // Show each NP as separate entry
            for (np in nps) {
                add(
                    AutoSkillMakerEntry.Action(
                        AutoSkillAction.NP(
                            CommandCard.NP.list.first { it.autoSkillCode == np }
                        )
                    )
                )
            }

            // Add a '0' before consecutive turn/battle changes
            if (!isEmpty() && (last is AutoSkillMakerEntry.NextWave || last is AutoSkillMakerEntry.NextTurn)) {
                add(AutoSkillMakerEntry.Action(AutoSkillAction.NoOp))
            }
        }
    }

    private fun onNext(Separator: AutoSkillMakerEntry) {
        // Uncheck selected targets
        unSelectTargets()

        addNpsToSkillCmd()

        if (isEmpty()) {
            add(AutoSkillMakerEntry.Action(AutoSkillAction.NoOp))
        }

        add(Separator)

        clearNpSequence()
    }

    fun nextTurn() = onNext(AutoSkillMakerEntry.NextTurn)

    fun nextStage() {
        _stage.next()
        onNext(AutoSkillMakerEntry.NextWave)
    }

    private val _xSelectedParty = MutableLiveData(state.xSelectedParty)
    private val _xSelectedSub = MutableLiveData(state.xSelectedSub)

    val xSelectedParty: LiveData<Int> = _xSelectedParty
    val xSelectedSub: LiveData<Int> = _xSelectedSub

    fun setOrderChangePartyMember(member: Int) {
        _xSelectedParty.value = member
    }

    fun setOrderChangeSubMember(member: Int) {
        _xSelectedSub.value = member
    }

    fun initOrderChange() {
        setOrderChangePartyMember(1)
        setOrderChangeSubMember(1)
    }

    fun commitOrderChange() {
        add(
            AutoSkillMakerEntry.Action(
                AutoSkillAction.OrderChange(
                    OrderChangeMember.Starting.list[(xSelectedParty.value ?: 1) - 1],
                    OrderChangeMember.Sub.list[(xSelectedSub.value ?: 1) - 1]
                )
            )
        )
    }

    private fun revertToPreviousEnemyTarget() {
        // Find the previous target, but within the same wave
        val previousTarget = reverseIterate()
            .asSequence()
            .takeWhile { it !is AutoSkillMakerEntry.NextWave }
            .filterIsInstance<AutoSkillMakerEntry.Action>()
            .map { it.action }
            .filterIsInstance<AutoSkillAction.TargetEnemy>()
            .firstOrNull()

        if (previousTarget == null) {
            unSelectTargets()
            return
        }

        val target = when (previousTarget.enemy.autoSkillCode) {
            '1' -> 1
            '2' -> 2
            '3' -> 3
            else -> return
        }

        _enemyTarget.value = target
    }

    private fun undoStageOrTurn() {
        // Decrement Battle/Turn count
        if (last is AutoSkillMakerEntry.NextWave) {
            prevStage()
        }

        // Undo the Battle/Turn change
        undo()

        fun shouldRemove(entry: AutoSkillMakerEntry) =
            if (entry is AutoSkillMakerEntry.Action) {
                when (entry.action) {
                    is AutoSkillAction.NP,
                    is AutoSkillAction.CardsBeforeNP,
                    is AutoSkillAction.NoOp -> true
                    else -> false
                }
            } else false

        // Remove NPs and cards before NPs
        while (!isEmpty() && shouldRemove(last)) {
            undo()
        }

        revertToPreviousEnemyTarget()
    }

    fun clear() {
        _currentIndex.value = model.skillCommand.lastIndex

        while (!isEmpty()) {
            onUndo { it() }
        }
    }

    fun onUndo(confirmNpDeletion: (onPositiveClick: () -> Unit) -> Unit) {
        if (!isEmpty()) {
            // Un-select target
            when (val last = last) {
                // Battle/Turn change
                is AutoSkillMakerEntry.NextWave, is AutoSkillMakerEntry.NextTurn -> {
                    confirmNpDeletion {
                        undoStageOrTurn()
                    }
                }
                is AutoSkillMakerEntry.Action -> {
                    if (last.action is AutoSkillAction.TargetEnemy) {
                        undo()
                        revertToPreviousEnemyTarget()
                    } else undo()
                }
                // Do nothing
                is AutoSkillMakerEntry.Start -> {
                }
            }
        }
    }

    private val _npSequence = MutableLiveData(state.npSequence)

    val npSequence: LiveData<List<Char>> = _npSequence

    fun initAtk() {
        clearNpSequence()

        cardsBeforeNp.value = 0
    }

    init {
        revertToPreviousEnemyTarget()
    }
}