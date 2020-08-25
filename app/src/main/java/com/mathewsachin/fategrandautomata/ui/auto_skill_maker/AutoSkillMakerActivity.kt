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

enum class AutoSkillMakerState {
    Main, Atk, Target, OrderChange
}

@AndroidEntryPoint
class AutoSkillMakerActivity : AppCompatActivity() {
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

        binding.autoSkillMain.atkBtn.setOnClickListener {
            // Uncheck NP buttons
            binding.autoSkillAtk.np4.isChecked = false
            binding.autoSkillAtk.np5.isChecked = false
            binding.autoSkillAtk.np6.isChecked = false

            skillCmdVm.clearNpSequence()

            // Set cards before Np to 0
            skillCmdVm.setCardsBeforeNp(0)

            skillCmdVm.currentView.value = AutoSkillMakerState.Atk
        }

        binding.autoSkillAtk.autoskillDoneBtn.setOnClickListener {
            skillCmdVm.addNpsToSkillCmd()

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

        binding.autoSkillAtk.cardsBeforeNpRad.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)

            if (radioButton?.isChecked == true) {
                val cards = when (checkedId) {
                    R.id.cards_before_np_1 -> 1
                    R.id.cards_before_np_2 -> 2
                    else -> 0
                }

                skillCmdVm.setCardsBeforeNp(cards)
            }
        }

        skillCmdVm.cardsBeforeNp.observe(this) {
            val targetId = when (it) {
                1 -> R.id.cards_before_np_1
                2 -> R.id.cards_before_np_2
                else -> R.id.cards_before_np_0
            }

            binding.autoSkillAtk.cardsBeforeNpRad.check(targetId)
        }

        var lastView = AutoSkillMakerState.Main
        skillCmdVm.currentView.observe(this) {
            // Hide current if not main
            if (lastView != AutoSkillMakerState.Main) {
                getStateView(lastView).visibility = View.GONE
            }

            lastView = it

            // Show new state
            getStateView(it).visibility = View.VISIBLE
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
            '1' -> 1
            '2' -> 2
            '3' -> 3
            else -> return
        }

        wasEnemyTargetUndo = true
        try {
            skillCmdVm.setEnemyTarget(targetRadio)
        } finally {
            wasEnemyTargetUndo = false
        }
    }

    private fun setupOrderChange() {
        val xParty = arrayOf(
            binding.autoSkillOrderChange.xParty1,
            binding.autoSkillOrderChange.xParty2,
            binding.autoSkillOrderChange.xParty3
        )
        val xSub = arrayOf(
            binding.autoSkillOrderChange.xSub1,
            binding.autoSkillOrderChange.xSub2,
            binding.autoSkillOrderChange.xSub3
        )

        skillCmdVm.xSelectedParty.observe(this) {
            setOrderChangeMember(xParty, it)
        }

        skillCmdVm.xSelectedSub.observe(this) {
            setOrderChangeMember(xSub, it)
        }
    }

    private fun getStateView(State: AutoSkillMakerState) = when (State) {
        AutoSkillMakerState.Atk -> binding.autoSkillAtk
        AutoSkillMakerState.Target -> binding.autoSkillTarget
        AutoSkillMakerState.OrderChange -> binding.autoSkillOrderChange
        else -> binding.autoSkillMain
    }.root

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

    override fun onBackPressed() {
        when (skillCmdVm.currentView.value) {
            AutoSkillMakerState.Main -> {
                AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit? AutoSkill command will be lost.")
                    .setTitle("Confirm Exit")
                    .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
            else -> skillCmdVm.gotToMain()
        }
    }
}
