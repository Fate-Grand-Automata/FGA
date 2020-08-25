package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = AutoskillMakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vm = skillCmdVm
        binding.activity = this
        binding.lifecycleOwner = this

        val recyclerView = binding.autoSkillMain.autoSkillHistory
        recyclerView.adapter = skillCmdVm.adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        setupOrderChange(binding)
    }

    private fun setupOrderChange(binding: AutoskillMakerBinding) {
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

    fun onUndo() {
        skillCmdVm.onUndo {
            AlertDialog.Builder(this)
                .setTitle("Confirm NP deletion")
                .setMessage("If you delete Battle/Turn separator, NPs and cards before NP for that turn will also be deleted. Are you sure?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { _, _ -> it() }
                .show()
        }
    }

    fun onDone() {
        skillCmdVm.addNpsToSkillCmd()

        val autoSkillPrefs = prefs.forAutoSkillConfig(args.key)
        autoSkillPrefs.skillCommand = skillCmdVm.getSkillCmdString()
        finish()
    }

    override fun onBackPressed() {
        if (skillCmdVm.canGoBack()) {
            skillCmdVm.goBack()
            return
        }

        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit? AutoSkill command will be lost.")
            .setTitle("Confirm Exit")
            .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }
}
