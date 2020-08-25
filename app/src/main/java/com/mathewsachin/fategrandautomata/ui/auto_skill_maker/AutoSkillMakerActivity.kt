package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.observe
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerBinding
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private enum class AutoSkillMakerState {
    Main, Atk, Target, OrderChange
}

@AndroidEntryPoint
class AutoSkillMakerActivity : AppCompatActivity() {
    // These fields are used to Save/Restore state of the Activity
    private var npSequence = ""
    private var currentView =
        AutoSkillMakerState.Main
    private var currentSkill = '0'

    // Order Change selected members
    private var xSelectedParty = 1
    private var xSelectedSub = 1

    private var xParty: Array<Button> = arrayOf()
    private var xSub: Array<Button> = arrayOf()

    val skillCmdVm: AutoSkillMakerHistoryViewModel by viewModels()
    val args: AutoSkillMakerActivityArgs by navArgs()

    @Inject
    lateinit var prefs: IPreferences

    lateinit var binding: AutoskillMakerBinding

    /**
     * Notifies that an enemy target was selected when undoing, so a new command should not be added
     */
    var wasEnemyTargetUndo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AutoskillMakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vm = skillCmdVm
        binding.lifecycleOwner = this

        val recyclerView = binding.autoSkillMain.autoSkillHistory
        recyclerView.adapter = skillCmdVm.adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.autoSkillAtk.np4.setOnClickListener { onNpClick("4") }
        binding.autoSkillAtk.np5.setOnClickListener { onNpClick("5") }
        binding.autoSkillAtk.np6.setOnClickListener { onNpClick("6") }

        binding.autoSkillMain.atkBtn.setOnClickListener {
            // Uncheck NP buttons
            binding.autoSkillAtk.np4.isChecked = false
            binding.autoSkillAtk.np5.isChecked = false
            binding.autoSkillAtk.np6.isChecked = false

            npSequence = ""

            // Set cards before Np to 0
            binding.autoSkillAtk.cardsBeforeNpRad.check(R.id.cards_before_np_0)

            changeState(AutoSkillMakerState.Atk)
        }

        setupSkills()
        setupTargets()

        binding.autoSkillAtk.autoskillNextBattleBtn.setOnClickListener {
            skillCmdVm.nextStage()
            onGoToNext(",#,")
        }

        binding.autoSkillAtk.autoskillNextTurnBtn.setOnClickListener { onGoToNext(",") }

        binding.autoSkillAtk.autoskillDoneBtn.setOnClickListener {
            addNpsToSkillCmd()

            val autoSkillPrefs = prefs.forAutoSkillConfig(args.key)
            autoSkillPrefs.skillCommand = skillCmdVm.getSkillCmdString()
            finish()
        }

        binding.autoSkillMain.enemyTargetRadio.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)

            if (radioButton?.isChecked == true && !wasEnemyTargetUndo) {
                val target = when (checkedId) {
                    R.id.enemy_target_1 -> 1
                    R.id.enemy_target_2 -> 2
                    R.id.enemy_target_3 -> 3
                    else -> AutoSkillMakerHistoryViewModel.NoEnemy
                }

                skillCmdVm.setEnemyTarget(target)
            }
        }

        skillCmdVm.enemyTarget.observe(this) {
            val targetId = when (it) {
                1 -> R.id.enemy_target_1
                2 -> R.id.enemy_target_2
                3 -> R.id.enemy_target_3
                else -> null
            }

            if (targetId != null) {
                binding.autoSkillMain.enemyTargetRadio.check(targetId)
            } else binding.autoSkillMain.enemyTargetRadio.clearCheck()
        }

        setupOrderChange()
    }

    private fun onUndo() {
        if (!skillCmdVm.isEmpty()) {
            // Un-select target
            when {
                skillCmdVm.last.startsWith('t') -> {
                    skillCmdVm.undo()
                    revertToPreviousEnemyTarget()
                }
                // Battle/Turn change
                skillCmdVm.last.contains(',') -> {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm NP deletion")
                        .setMessage("If you delete Battle/Turn separator, NPs and cards before NP for that turn will also be deleted. Are you sure?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            undoStageOrTurn()
                        }
                        .show()
                }
                else -> skillCmdVm.undo()
            }
        }
    }

    private fun undoStageOrTurn() {
        // Decrement Battle/Turn count
        if (skillCmdVm.last.contains('#')) {
            skillCmdVm.prevStage()
        }

        skillCmdVm.prevTurn()

        // Undo the Battle/Turn change
        skillCmdVm.undo()

        val itemsToRemove = setOf('4', '5', '6', 'n', '0')

        // Remove NPs and cards before NPs
        while (!skillCmdVm.isEmpty()
            && skillCmdVm.last[0] in itemsToRemove
        ) {
            skillCmdVm.undo()
        }

        revertToPreviousEnemyTarget()
    }

    private fun revertToPreviousEnemyTarget() {
        // Find the previous target, but within the same turn
        val previousTarget = skillCmdVm
            .reverseIterate()
            .takeWhile { !it.contains(',') }
            .firstOrNull { it.startsWith('t') }

        if (previousTarget == null) {
            skillCmdVm.unSelectTargets()
            return
        }

        val targetRadio = when (previousTarget[1]) {
            '1' -> binding.autoSkillMain.enemyTarget1
            '2' -> binding.autoSkillMain.enemyTarget2
            '3' -> binding.autoSkillMain.enemyTarget3
            else -> return
        }

        wasEnemyTargetUndo = true
        try {
            binding.autoSkillMain.enemyTargetRadio.check(targetRadio.id)
        } finally {
            wasEnemyTargetUndo = false
        }
    }

    private fun setupOrderChange() {
        xParty = arrayOf(
            binding.autoSkillOrderChange.xParty1,
            binding.autoSkillOrderChange.xParty2,
            binding.autoSkillOrderChange.xParty3
        )
        xSub = arrayOf(
            binding.autoSkillOrderChange.xSub1,
            binding.autoSkillOrderChange.xSub2,
            binding.autoSkillOrderChange.xSub3
        )

        for (i in 0 until 3) {
            val member = i + 1

            xParty[i].setOnClickListener { setOrderChangePartyMember(member) }
            xSub[i].setOnClickListener { setOrderChangeSubMember(member) }
        }

        binding.autoSkillMain.masterXBtn.setOnClickListener {
            changeState(AutoSkillMakerState.OrderChange)

            setOrderChangePartyMember(1)
            setOrderChangeSubMember(1)
        }

        binding.autoSkillOrderChange.orderChangeCancel.setOnClickListener { gotToMain() }

        binding.autoSkillOrderChange.orderChangeOk.setOnClickListener {
            skillCmdVm.add("x${xSelectedParty}${xSelectedSub}")

            gotToMain()
        }
    }

    private fun gotToMain() {
        changeState(AutoSkillMakerState.Main)
    }

    private fun setupSkills() {
        fun onSkill(SkillCode: Char) {
            currentSkill = SkillCode

            changeState(AutoSkillMakerState.Target)
        }

        binding.autoSkillMain.skillABtn.setOnClickListener { onSkill('a') }
        binding.autoSkillMain.skillBBtn.setOnClickListener { onSkill('b') }
        binding.autoSkillMain.skillCBtn.setOnClickListener { onSkill('c') }
        binding.autoSkillMain.skillDBtn.setOnClickListener { onSkill('d') }
        binding.autoSkillMain.skillEBtn.setOnClickListener { onSkill('e') }
        binding.autoSkillMain.skillFBtn.setOnClickListener { onSkill('f') }
        binding.autoSkillMain.skillGBtn.setOnClickListener { onSkill('g') }
        binding.autoSkillMain.skillHBtn.setOnClickListener { onSkill('h') }
        binding.autoSkillMain.skillIBtn.setOnClickListener { onSkill('i') }

        binding.autoSkillMain.masterJBtn.setOnClickListener { onSkill('j') }
        binding.autoSkillMain.masterKBtn.setOnClickListener { onSkill('k') }
        binding.autoSkillMain.masterLBtn.setOnClickListener { onSkill('l') }
    }

    private fun setupTargets() {
        fun onTarget(TargetCommand: Char?) {
            var cmd = currentSkill.toString()

            if (TargetCommand != null) {
                cmd += TargetCommand
            }

            skillCmdVm.add(cmd)

            gotToMain()
        }

        binding.autoSkillTarget.noTargetBtn.setOnClickListener { onTarget(null) }
        binding.autoSkillTarget.target1.setOnClickListener { onTarget('1') }
        binding.autoSkillTarget.target2.setOnClickListener { onTarget('2') }
        binding.autoSkillTarget.target3.setOnClickListener { onTarget('3') }
    }

    private fun addNpsToSkillCmd() {
        skillCmdVm.let {
            if (npSequence.isNotEmpty()) {
                when (binding.autoSkillAtk.cardsBeforeNpRad.checkedRadioButtonId) {
                    R.id.cards_before_np_1 -> it.add("n1")
                    R.id.cards_before_np_2 -> it.add("n2")
                }
            }

            // Show each NP as separate entry
            for (np in npSequence) {
                it.add(np.toString())
            }

            // Add a '0' before consecutive turn/battle changes
            if (!it.isEmpty() && it.last.last() == ',') {
                it.add("0")
            }
        }

        npSequence = ""
    }

    private fun onGoToNext(Separator: String) {
        // Uncheck selected targets
        skillCmdVm.unSelectTargets()

        addNpsToSkillCmd()

        if (skillCmdVm.isEmpty()) {
            skillCmdVm.add("0")
        }

        skillCmdVm.add(Separator)

        skillCmdVm.nextTurn()

        gotToMain()
    }

    private fun getStateView(State: AutoSkillMakerState) = when (State) {
        AutoSkillMakerState.Atk -> binding.autoSkillAtk
        AutoSkillMakerState.Target -> binding.autoSkillTarget
        AutoSkillMakerState.OrderChange -> binding.autoSkillOrderChange
        else -> binding.autoSkillMain
    }.root

    private fun changeState(NewState: AutoSkillMakerState) {
        // Hide current if not main
        if (currentView != AutoSkillMakerState.Main) {
            getStateView(currentView).visibility = View.GONE
        }

        // Show new state
        currentView = NewState
        getStateView(NewState).visibility = View.VISIBLE
    }

    private fun onNpClick(NpCommand: String) {
        if (npSequence.contains(NpCommand)) {
            npSequence = npSequence.replace(NpCommand, "")
        } else npSequence += NpCommand
    }

    private fun setOrderChangeMember(Members: Array<Button>, Member: Int) {

        for ((i, button) in Members.withIndex()) {
            val selected = i + 1 == Member

            if (selected) {
                val color = ContextCompat.getColor(this, R.color.colorAccent)
                button.background.colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(color, BlendModeCompat.SRC)
            } else {
                button.background.clearColorFilter()
            }
        }
    }

    private fun setOrderChangePartyMember(Member: Int) {
        xSelectedParty = Member

        setOrderChangeMember(xParty, Member)
    }

    private fun setOrderChangeSubMember(Member: Int) {
        xSelectedSub = Member

        setOrderChangeMember(xSub, Member)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(::npSequence.name, npSequence)
        outState.putInt(::currentView.name, currentView.ordinal)
        outState.putInt(::xSelectedParty.name, xSelectedParty)
        outState.putInt(::xSelectedSub.name, xSelectedSub)
        outState.putChar(::currentSkill.name, currentSkill)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val b = savedInstanceState

        npSequence = b.getString(::npSequence.name, "")
        changeState(AutoSkillMakerState.values()[b.getInt(::currentView.name, 0)])

        setOrderChangePartyMember(b.getInt(::xSelectedParty.name, 1))
        setOrderChangeSubMember(b.getInt(::xSelectedSub.name, 1))

        currentSkill = b.getChar(::currentSkill.name)
    }

    override fun onBackPressed() {
        when (currentView) {
            AutoSkillMakerState.Main -> {
                AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit? AutoSkill command will be lost.")
                    .setTitle("Confirm Exit")
                    .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
            else -> gotToMain()
        }
    }
}
