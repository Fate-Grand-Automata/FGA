package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.databinding.SkillMakerTargetBinding
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerTargetFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()
    val args: SkillMakerTargetFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerTargetBinding.inflate(inflater, container, false)
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
        val action = SkillMakerTargetFragmentDirections
            .actionTargetSkillMakerToSkillMakerSpaceIshtarFragment()

        nav(action)
    }

    fun onEmiya() {
        val action = SkillMakerTargetFragmentDirections
            .actionTargetSkillMakerToSkillMakerEmiyaFragment()

        nav(action)
    }
}