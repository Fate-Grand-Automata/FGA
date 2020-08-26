package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.AutoskillMakerAtkBinding
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutoSkillMakerAtkFragment : Fragment() {
    val viewModel: AutoSkillMakerViewModel by activityViewModels()

    @Inject
    lateinit var prefs: IPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AutoskillMakerAtkBinding.inflate(inflater, container, false)
            .also {
                it.vm = viewModel
                it.ui = this
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

    fun cardsBeforeNpRadio(cards: Int) = when (cards) {
        1 -> R.id.cards_before_np_1
        2 -> R.id.cards_before_np_2
        else -> R.id.cards_before_np_0
    }

    fun onDone() {
        val autoSkillPrefs = prefs.forAutoSkillConfig(viewModel.autoSkillItemKey)
        autoSkillPrefs.skillCommand = viewModel.finish()
        activity?.finish()
    }

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