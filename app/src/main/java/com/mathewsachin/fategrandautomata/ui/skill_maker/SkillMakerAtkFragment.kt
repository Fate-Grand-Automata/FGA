package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.databinding.SkillMakerAtkBinding
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SkillMakerAtkFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    @Inject
    lateinit var prefs: IPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        SkillMakerAtkBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    private fun goBack() {
        findNavController().popBackStack()
    }

    fun goToNextStage() {
        viewModel.nextStage()

        goBack()
    }

    fun goToNextTurn() {
        viewModel.nextTurn()

        goBack()
    }
}