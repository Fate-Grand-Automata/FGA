package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences

class SkillMakerViewModel @ViewModelInject constructor(
    val prefs: IPreferences,
    @Assisted val savedState: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    val battleConfigKey: String = savedState[SkillMakerActivityArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    val battleConfig = prefs.forBattleConfig(battleConfigKey)

    val state = savedState.get(::savedState.name)
        ?: SkillMakerSavedState()

    private val model: SkillMakerModel
    private val _stage: MutableLiveData<Int>

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex

    init {
        model = if (state.skillString != null) {
            SkillMakerModel(state.skillString)
        } else {
            val skillString = battleConfig.skillCommand
            val m = try {
                SkillMakerModel(skillString)
            } catch (e: Exception) {
                SkillMakerModel("")
            }

            if (skillString.isNotEmpty()) {
                when (val l = m.skillCommand.last()) {
                    is SkillMakerEntry.Action -> when (l.action) {
                        is AutoSkillAction.Atk -> {
                            m.skillCommand.removeLast()
                            m.skillCommand.add(SkillMakerEntry.Next.Wave(l.action))
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
            }

            m
        }

        _stage = if (state.skillString != null) {
            MutableLiveData(state.stage)
        } else {
            MutableLiveData(
                model.skillCommand.count { it is SkillMakerEntry.Next.Wave } + 1
            )
        }

        _currentIndex.value = if (state.skillString != null) {
            state.currentIndex
        } else model.skillCommand.lastIndex
    }

    private var currentSkill = state.currentSkill

    fun saveState() {
        val saveState = SkillMakerSavedState(
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

    val skillCommand: LiveData<List<SkillMakerEntry>> = Transformations.map(_skillCommand) { it }

    private fun notifySkillCommandUpdate() {
        _skillCommand.value = model.skillCommand
    }

    private fun getSkillCmdString() = model.toString()

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index

        notifySkillCommandUpdate()
        revertToPreviousEnemyTarget()
    }

    private fun add(entry: SkillMakerEntry) {
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

    private var last: SkillMakerEntry
        get() = model.skillCommand[currentIndex.value ?: 0]
        set(value) {
            model.skillCommand[currentIndex.value ?: 0] = value

            notifySkillCommandUpdate()
        }

    private fun reverseIterate(): List<SkillMakerEntry> =
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

        val targetCmd = SkillMakerEntry.Action(
            AutoSkillAction.TargetEnemy(
                EnemyTarget.list[target - 1]
            )
        )

        val l = last

        // Merge consecutive target changes
        if (!isEmpty() && l is SkillMakerEntry.Action && l.action is AutoSkillAction.TargetEnemy) {
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
            SkillMakerEntry.Action(
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

        while (last.let { l -> l is SkillMakerEntry.Next && l.action == AutoSkillAction.Atk.noOp() }) {
            undo()
        }

        return getSkillCmdString()
    }

    private fun atk(): AutoSkillAction.Atk =
        npSequence.value?.let { nps ->
            AutoSkillAction.Atk(
                nps.map { np ->
                    CommandCard.NP.list.first { it.autoSkillCode == np }
                }.toSet(),
                (if (nps.isNotEmpty()) cardsBeforeNp.value else 0) ?: 0
            )
        } ?: AutoSkillAction.Atk.noOp()

    private fun onNext(separator: (AutoSkillAction.Atk) -> SkillMakerEntry.Next) {
        add(separator(atk()))

        clearNpSequence()
    }

    fun nextTurn() = onNext { SkillMakerEntry.Next.Turn(it) }

    fun nextStage() {
        _stage.next()

        // Uncheck selected targets
        unSelectTargets()

        onNext { SkillMakerEntry.Next.Wave(it) }
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
            SkillMakerEntry.Action(
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
            .takeWhile { it !is SkillMakerEntry.Next.Wave }
            .filterIsInstance<SkillMakerEntry.Action>()
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
        if (last is SkillMakerEntry.Next.Wave) {
            prevStage()
        }

        // Undo the Battle/Turn change
        undo()

        revertToPreviousEnemyTarget()
    }

    fun clearAll() {
        _currentIndex.value = model.skillCommand.lastIndex

        while (!isEmpty()) {
            onUndo()
        }
    }

    fun onUndo() {
        if (!isEmpty()) {
            // Un-select target
            when (val last = last) {
                // Battle/Turn change
                is SkillMakerEntry.Next -> {
                    undoStageOrTurn()
                }
                is SkillMakerEntry.Action -> {
                    if (last.action is AutoSkillAction.TargetEnemy) {
                        undo()
                        revertToPreviousEnemyTarget()
                    } else undo()
                }
                // Do nothing
                is SkillMakerEntry.Start -> {
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
