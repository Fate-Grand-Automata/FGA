package com.mathewsachin.fategrandautomata.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.mathewsachin.fategrandautomata.R
import kotlinx.android.synthetic.main.autoskill_maker.*
import kotlinx.android.synthetic.main.autoskill_maker_atk.*
import kotlinx.android.synthetic.main.autoskill_maker_main.*
import kotlinx.android.synthetic.main.autoskill_maker_order_change.*
import kotlinx.android.synthetic.main.autoskill_maker_target.*

private enum class AutoSkillMakerState {
    Main, Atk, Target, OrderChange
}

class AutoSkillMakerActivity : AppCompatActivity() {
    // These fields are used to Save/Restore state of the Activity
    private var skillCmd = StringBuilder()
    private var npSequence = ""
    private var currentView = AutoSkillMakerState.Main
    private var stage = 1
    private var turn = 1
    private var currentSkill = '0'

    // Order Change selected members
    private var xSelectedParty = 1
    private var xSelectedSub = 1

    private var xParty: Array<Button> = arrayOf()
    private var xSub: Array<Button> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autoskill_maker)

        setSupportActionBar(autoskill_maker_toolbar)

        np_4.setOnClickListener { onNpClick("4") }
        np_5.setOnClickListener { onNpClick("5") }
        np_6.setOnClickListener { onNpClick("6") }

        atk_btn.setOnClickListener {
            // Uncheck NP buttons
            np_4.isChecked = false
            np_5.isChecked = false
            np_6.isChecked = false

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

            Toast.makeText(applicationContext, "AutoSkill command copied to clipboard", Toast.LENGTH_SHORT).show()

            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("AutoSkill Command", skillCmd)
            clipboardManager.setPrimaryClip(clip)

            finish()
        }

        enemy_target_radio.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)

            if (radioButton?.isChecked == true) {
                when(checkedId) {
                    R.id.enemy_target_1 -> setEnemyTarget(1)
                    R.id.enemy_target_2 -> setEnemyTarget(2)
                    R.id.enemy_target_3 -> setEnemyTarget(3)
                }
            }
        }

        setupOrderChange()
    }

    private fun setEnemyTarget(Target: Int) {
        // Merge consecutive target changes
        if (skillCmd.length >= 2 && skillCmd[skillCmd.length - 2] == 't')
        {
            skillCmd.delete(skillCmd.length - 1, 1)
                .append(Target)
        }
        else
        {
            skillCmd.append("t${Target}")
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
            skillCmd.append("x${xSelectedParty}${xSelectedSub}")

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
            skillCmd.append(currentSkill)

            if (TargetCommand != null) {
                skillCmd.append(TargetCommand)
            }

            gotToMain()
        }

        no_target_btn.setOnClickListener { onTarget(null) }
        target_1.setOnClickListener { onTarget('1') }
        target_2.setOnClickListener { onTarget('2') }
        target_3.setOnClickListener { onTarget('3') }
    }

    private fun addNpsToSkillCmd() {
        if (npSequence.isNotEmpty()) {
            when (cards_before_np_rad.checkedRadioButtonId) {
                R.id.cards_before_np_1 -> skillCmd.append("n1")
                R.id.cards_before_np_2 -> skillCmd.append("n2")
            }
        }

        skillCmd.append(npSequence)

        if (skillCmd.isNotEmpty() && skillCmd[skillCmd.length - 1] == ',') {
            skillCmd.append('0')
        }

        npSequence = ""
    }

    private fun onGoToNext(Separator: String) {
        // Uncheck selected targets
        enemy_target_radio.clearCheck()

        addNpsToSkillCmd()

        if (skillCmd.isEmpty()) {
            skillCmd.append('0')
        }

        skillCmd.append(Separator)

        ++turn
        updateStageAndTurn()

        gotToMain()
    }

    private fun getStateView(State: AutoSkillMakerState) = when(State) {
        AutoSkillMakerState.Atk -> autoskill_view_atk
        AutoSkillMakerState.Target -> autoskill_view_target
        AutoSkillMakerState.OrderChange -> autoskill_view_order_change
        else -> autoskill_view_main
    }

    private fun changeState(NewState: AutoSkillMakerState) {
        // Hide current
        getStateView(currentView).visibility = View.GONE

        // Hide the default view just in case
        getStateView(AutoSkillMakerState.Main).visibility = View.GONE

        // Show new state
        currentView = NewState
        getStateView(NewState).visibility = View.VISIBLE
    }

    private fun onNpClick(NpCommand: String) {
        if (npSequence.contains(NpCommand)) {
            npSequence = npSequence.replace(NpCommand, "")
        }
        else npSequence += NpCommand
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

        outState.putString(::skillCmd.name, skillCmd.toString())
        outState.putString(::npSequence.name, npSequence)
        outState.putInt(::currentView.name, currentView.ordinal)
        outState.putInt(::stage.name, stage)
        outState.putInt(::turn.name, turn)
        outState.putInt(::xSelectedParty.name, xSelectedParty)
        outState.putInt(::xSelectedSub.name, xSelectedSub)
        outState.putChar(::currentSkill.name, currentSkill)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val b = savedInstanceState ?: return

        skillCmd = StringBuilder(b.getString(::skillCmd.name, ""))
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
