package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardPriorityFragment : Fragment() {
    val vm: CardPriorityViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    CardPriorityView(items = vm.cardPriorityItems)
                }
            }
        }

    override fun onPause() {
        super.onPause()

        vm.save()
    }
}