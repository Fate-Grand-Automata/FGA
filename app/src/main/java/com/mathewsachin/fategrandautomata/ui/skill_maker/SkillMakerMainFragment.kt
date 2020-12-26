package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.SkillMakerMainBinding
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerMainFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()
    val adapter = SkillMakerHistoryAdapter {
        viewModel.setCurrentIndex(it)
    }
    lateinit var binding: SkillMakerMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerMainBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.skillHistory
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewModel.skillCommand.observe(viewLifecycleOwner) {
            val currentIndex = viewModel.currentIndex.value ?: 0
            adapter.update(it, currentIndex)

            recyclerView.scrollToPosition(currentIndex)
        }
    }

    fun enemyTargetRadio(target: Int) = when (target) {
        1 -> R.id.enemy_target_1
        2 -> R.id.enemy_target_2
        3 -> R.id.enemy_target_3
        else -> -1
    }

    fun onClear() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.skill_maker_confirm_clear_title)
            .setMessage(R.string.skill_maker_confirm_clear_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { _, _ -> viewModel.clearAll() }
            .show()
    }

    fun goToAtk() {
        viewModel.initAtk()

        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerAtkFragment()

        nav(action)
    }

    fun goToMasterSkills() {
        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerMasterSkillsFragment()

        nav(action)
    }

    fun onSkill(SkillCode: Char) {
        viewModel.initSkill(SkillCode)

        val showSpaceIshtar = SkillCode in listOf('b', 'e', 'h')
        val showEmiya = SkillCode in listOf('c', 'f', 'i')

        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerTargetFragment(
                showSpaceIshtar,
                showEmiya
            )

        nav(action)
    }

    fun onDone() {
        viewModel.battleConfig.skillCommand = viewModel.finish()
        activity?.finish()
    }
}