package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerTargetBinding
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSkillMakerTargetFragment : Fragment() {
    val viewModel: AutoSkillMakerViewModel by activityViewModels()
    val args: AutoSkillMakerTargetFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AutoskillMakerTargetBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.showSpaceIshtar = args.showSpaceIshtar
                it.showEmiya = args.showEmiya
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    // Data Binding doesn't seem to work with default parameters or null
    fun onSkillTarget() = onSkillTarget(null)

    fun onSkillTarget(TargetCommand: Char?) {
        viewModel.targetSkill(TargetCommand)

        findNavController().popBackStack()
    }

    fun onSpaceIshtar() {
        val action = AutoSkillMakerTargetFragmentDirections
            .actionTargetAutoSkillMakerToAutoSkillMakerSpaceIshtarFragment()

        nav(action)
    }

    fun onEmiya() {
        val action = AutoSkillMakerTargetFragmentDirections
            .actionTargetAutoSkillMakerToAutoSkillMakerEmiyaFragment()

        nav(action)
    }
}