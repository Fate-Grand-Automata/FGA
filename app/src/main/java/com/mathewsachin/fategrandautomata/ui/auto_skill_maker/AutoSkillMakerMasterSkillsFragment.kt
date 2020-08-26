package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerMasterSkillsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSkillMakerMasterSkillsFragment : Fragment() {
    val viewModel: AutoSkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AutoskillMakerMasterSkillsBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun onSkill(SkillCode: Char) {
        viewModel.initSkill(SkillCode)

        val action = AutoSkillMakerMasterSkillsFragmentDirections
            .actionAutoSkillMakerMasterSkillsFragmentToAutoSkillMakerTargetFragment()

        findNavController().navigate(action)
    }

    fun goToOrderChange() {
        viewModel.initOrderChange()

        val action = AutoSkillMakerMasterSkillsFragmentDirections
            .actionAutoSkillMakerMasterSkillsFragmentToAutoSkillMakerOrderChangeFragment()

        findNavController().navigate(action)
    }
}