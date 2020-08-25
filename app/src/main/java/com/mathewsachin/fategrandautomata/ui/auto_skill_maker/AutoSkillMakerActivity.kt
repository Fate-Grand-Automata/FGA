package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerBinding
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutoSkillMakerActivity : AppCompatActivity() {
    val vm: AutoSkillMakerViewModel by viewModels()
    val args: AutoSkillMakerActivityArgs by navArgs()

    @Inject
    lateinit var prefs: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = AutoskillMakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vm = vm
        binding.activity = this
        binding.lifecycleOwner = this

        val recyclerView = binding.autoSkillMain.autoSkillHistory
        recyclerView.adapter = vm.adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    fun onUndo() {
        vm.onUndo {
            AlertDialog.Builder(this)
                .setTitle("Confirm NP deletion")
                .setMessage("If you delete Battle/Turn separator, NPs and cards before NP for that turn will also be deleted. Are you sure?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { _, _ -> it() }
                .show()
        }
    }

    fun onDone() {
        val autoSkillPrefs = prefs.forAutoSkillConfig(args.key)
        autoSkillPrefs.skillCommand = vm.finish()
        finish()
    }

    override fun onBackPressed() {
        if (vm.canGoBack()) {
            vm.goBack()
            return
        }

        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit? AutoSkill command will be lost.")
            .setTitle("Confirm Exit")
            .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    fun enemyTargetRadio(target: Int) = when (target) {
        1 -> R.id.enemy_target_1
        2 -> R.id.enemy_target_2
        3 -> R.id.enemy_target_3
        else -> -1
    }

    fun cardsBeforeNpRadio(cards: Int) = when (cards) {
        1 -> R.id.cards_before_np_1
        2 -> R.id.cards_before_np_2
        else -> R.id.cards_before_np_0
    }

    override fun onPause() {
        vm.saveState()
        
        super.onPause()
    }
}
