package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.databinding.SkillMakerOrderChangeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerOrderChangeFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerOrderChangeBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun goBack() {
        findNavController().popBackStack()
    }

    fun orderChangeOk() {
        viewModel.commitOrderChange()

        goBack()
    }
}