package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSkillMakerMainFragment : Fragment() {
    val viewModel: AutoSkillMakerViewModel by activityViewModels()
    val adapter = AutoSkillMakerHistoryAdapter()
    lateinit var binding: AutoskillMakerMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AutoskillMakerMainBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.autoSkillHistory
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewModel.skillCommand.observe(viewLifecycleOwner) {
            adapter.update(it)

            recyclerView.scrollToPosition(viewModel.currentIndex)
        }
    }

    fun enemyTargetRadio(target: Int) = when (target) {
        1 -> R.id.enemy_target_1
        2 -> R.id.enemy_target_2
        3 -> R.id.enemy_target_3
        else -> -1
    }

    fun onUndo() {
        viewModel.onUndo {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm NP deletion")
                .setMessage("If you delete Battle/Turn separator, NPs and cards before NP for that turn will also be deleted. Are you sure?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { _, _ -> it() }
                .show()
        }
    }

    fun goToAtk() {
        viewModel.initAtk()

        val action = AutoSkillMakerMainFragmentDirections
            .actionAutoSkillMakerMainFragmentToAutoSkillMakerAtkFragment()

        findNavController().navigate(action)
    }

    fun goToMasterSkills() {
        val action = AutoSkillMakerMainFragmentDirections
            .actionAutoSkillMakerMainFragmentToAutoSkillMakerMasterSkillsFragment()

        findNavController().navigate(action)
    }

    fun onSkill(SkillCode: Char) {
        viewModel.initSkill(SkillCode)

        val action = AutoSkillMakerMainFragmentDirections
            .actionAutoSkillMakerMainFragmentToAutoSkillMakerTargetFragment()

        findNavController().navigate(action)
    }
}