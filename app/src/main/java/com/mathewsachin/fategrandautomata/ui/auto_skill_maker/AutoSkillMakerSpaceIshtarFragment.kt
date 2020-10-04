package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerSpaceIshtarBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSkillMakerSpaceIshtarFragment : Fragment() {
    val viewModel: AutoSkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AutoskillMakerSpaceIshtarBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun onSkillTarget(TargetCommand: Char?) {
        viewModel.targetSkill(TargetCommand)

        findNavController().popBackStack()
    }
}