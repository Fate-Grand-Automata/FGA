package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.app.Activity
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import kotlinx.android.synthetic.main.autoskill_maker_atk.*
import kotlinx.android.synthetic.main.autoskill_maker_main.*
import kotlinx.android.synthetic.main.autoskill_maker_order_change.*
import kotlinx.android.synthetic.main.autoskill_maker_target.*

private enum class AutoSkillMakerState {
    Main, Atk, Target, OrderChange
}

const val RequestAutoSkillMaker = 1027
const val AutoSkillCommandKey = "AutoSkillCommandKey"

class AutoSkillMakerActivity : AppCompatActivity() {
    // These fields are used to Save/Restore state of the Activity
    private var npSequence = ""
    private var currentView =
        AutoSkillMakerState.Main
    private var stage = 1
    private var turn = 1
    private var currentSkill = '0'

    // Order Change selected members
    private var xSelectedParty = 1
    private var xSelectedSub = 1

    private var xParty: Array<Button> = arrayOf()
    private var xSub: Array<Button> = arrayOf()

    val skillCmdVm: AutoSkillMakerHistoryViewModel by viewModels()

    /**
     * Notifies that an enemy target was selected when undoing, so a new command should not be added
     */
    var wasEnemyTargetUndo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autoskill_maker)

        val recyclerView = auto_skill_history
        recyclerView.adapter = skillCmdVm.adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        auto_skill_undo_btn.setOnClickListener {
            onUndo()
        }

        np_4.setOnClickListener { onNpClick("4") }
        np_5.setOnClickListener { onNpClick("5") }
        np_6.setOnClickListener { onNpClick("6") }

        atk_btn.setOnClickListener {
            // Uncheck NP buttons
            np_4.isChecked = false
            np_5.isChecked = false
            np_6.isChecked = false

            npSequence = ""

            // Set cards before Np to 0
            cards_before_np_rad.check(R.id.cards_before_np_0)

            changeState(AutoSkillMakerState.Atk)
        }

        setupSkills()
        setupTargets()

        autoskill_next_battle_btn.setOnClickListener {
            ++stage
            onGoToNext(",#,")
        }

        autoskill_next_turn_btn.setOnClickListener { onGoToNext(",") }

        autoskill_done_btn.setOnClickListener {
            addNpsToSkillCmd()

            val res = Intent()
            res.putExtra(AutoSkillCommandKey, skillCmdVm.getSkillCmdString())
            setResult(Activity.RESULT_OK, res)
            finish()
        }

        enemy_target_radio.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)

            if (radioButton?.isChecked == true && !wasEnemyTargetUndo) {
                when (checkedId) {
                    R.id.enemy_target_1 -> setEnemyTarget(1)
                    R.id.enemy_target_2 -> setEnemyTarget(2)
                    R.id.enemy_target_3 -> setEnemyTarget(3)
                }
            }
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
            --stage
        }

        --turn
        updateStageAndTurn()

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
            unSelectTargets()
            return
        }

        val targetRadio = when (previousTarget[1]) {
            '1' -> enemy_target_1
            '2' -> enemy_target_2
            '3' -> enemy_target_3
            else -> return
        }

        wasEnemyTargetUndo = true
        try {
            enemy_target_radio.check(targetRadio.id)
        } finally {
            wasEnemyTargetUndo = false
        }
    }

    private fun setEnemyTarget(Target: Int) {
        val targetCmd = "t${Target}"

        skillCmdVm.let {
            // Merge consecutive target changes
            if (!it.isEmpty() && it.last[0] == 't') {
                it.last = targetCmd
            } else {
                it.add(targetCmd)
            }
        }
    }

    private fun setupOrderChange() {
        xParty = arrayOf(x_party_1, x_party_2, x_party_3)
        xSub = arrayOf(x_sub_1, x_sub_2, x_sub_3)

        for (i in 0 until 3) {
            val member = i + 1

            xParty[i].setOnClickListener { setOrderChangePartyMember(member) }
            xSub[i].setOnClickListener { setOrderChangeSubMember(member) }
        }

        master_x_btn.setOnClickListener {
            changeState(AutoSkillMakerState.OrderChange)

            setOrderChangePartyMember(1)
            setOrderChangeSubMember(1)
        }

        order_change_cancel.setOnClickListener { gotToMain() }

        order_change_ok.setOnClickListener {
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

        skill_a_btn.setOnClickListener { onSkill('a') }
        skill_b_btn.setOnClickListener { onSkill('b') }
        skill_c_btn.setOnClickListener { onSkill('c') }
        skill_d_btn.setOnClickListener { onSkill('d') }
        skill_e_btn.setOnClickListener { onSkill('e') }
        skill_f_btn.setOnClickListener { onSkill('f') }
        skill_g_btn.setOnClickListener { onSkill('g') }
        skill_h_btn.setOnClickListener { onSkill('h') }
        skill_i_btn.setOnClickListener { onSkill('i') }

        master_j_btn.setOnClickListener { onSkill('j') }
        master_k_btn.setOnClickListener { onSkill('k') }
        master_l_btn.setOnClickListener { onSkill('l') }
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

        no_target_btn.setOnClickListener { onTarget(null) }
        target_1.setOnClickListener { onTarget('1') }
        target_2.setOnClickListener { onTarget('2') }
        target_3.setOnClickListener { onTarget('3') }
    }

    private fun addNpsToSkillCmd() {
        skillCmdVm.let {
            if (npSequence.isNotEmpty()) {
                when (cards_before_np_rad.checkedRadioButtonId) {
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

    private fun unSelectTargets() {
        enemy_target_radio.clearCheck()
    }

    private fun onGoToNext(Separator: String) {
        // Uncheck selected targets
        unSelectTargets()

        addNpsToSkillCmd()

        if (skillCmdVm.isEmpty()) {
            skillCmdVm.add("0")
        }

        skillCmdVm.add(Separator)

        ++turn
        updateStageAndTurn()

        gotToMain()
    }

    private fun getStateView(State: AutoSkillMakerState) = when (State) {
        AutoSkillMakerState.Atk -> autoskill_view_atk
        AutoSkillMakerState.Target -> autoskill_view_target
        AutoSkillMakerState.OrderChange -> autoskill_view_order_change
        else -> autoskill_view_main
    }

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
        outState.putInt(::stage.name, stage)
        outState.putInt(::turn.name, turn)
        outState.putInt(::xSelectedParty.name, xSelectedParty)
        outState.putInt(::xSelectedSub.name, xSelectedSub)
        outState.putChar(::currentSkill.name, currentSkill)

        skillCmdVm.saveState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val b = savedInstanceState

        npSequence = b.getString(::npSequence.name, "")
        changeState(AutoSkillMakerState.values()[b.getInt(::currentView.name, 0)])

        stage = b.getInt(::stage.name, 1)
        turn = b.getInt(::turn.name, 1)

        setOrderChangePartyMember(b.getInt(::xSelectedParty.name, 1))
        setOrderChangeSubMember(b.getInt(::xSelectedSub.name, 1))

        currentSkill = b.getChar(::currentSkill.name)
    }

    private fun updateStageAndTurn() {
        battle_stage_txt.text = stage.toString()
        battle_turn_txt.text = turn.toString()
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
