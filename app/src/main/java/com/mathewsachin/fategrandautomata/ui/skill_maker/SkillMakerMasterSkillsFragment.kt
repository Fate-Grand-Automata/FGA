package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.databinding.SkillMakerMasterSkillsBinding
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerMasterSkillsFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerMasterSkillsBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun onSkill(SkillCode: Char) {
        viewModel.initSkill(SkillCode)

        val action = SkillMakerMasterSkillsFragmentDirections
            .actionSkillMakerMasterSkillsFragmentToSkillMakerTargetFragment()

        nav(action)
    }

    fun goToOrderChange() {
        viewModel.initOrderChange()

        val action = SkillMakerMasterSkillsFragmentDirections
            .actionSkillMakerMasterSkillsFragmentToSkillMakerOrderChangeFragment()

        nav(action)
    }
}