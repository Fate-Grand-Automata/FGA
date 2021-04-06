package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.ui.FgaScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardPriorityFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                val vm: CardPriorityViewModel = viewModel()

                FgaScreen {
                    CardPriorityView(items = vm.cardPriorityItems)
                }

                DisposableEffect(vm) {
                    onDispose {
                        vm.save()
                    }
                }
            }
        }
}