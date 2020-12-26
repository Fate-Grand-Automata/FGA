package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.databinding.SkillMakerEmiyaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerEmiyaFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerEmiyaBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun onArts() = onSkillTarget('7')
    fun onBuster() = onSkillTarget('8')

    private fun onSkillTarget(TargetCommand: Char?) {
        viewModel.targetSkill(TargetCommand)

        findNavController().popBackStack()
    }
}