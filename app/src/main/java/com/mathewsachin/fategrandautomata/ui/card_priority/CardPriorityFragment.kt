package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardPriorityFragment : Fragment() {
    val vm: CardPriorityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.card_priority_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_card_priority_info -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.card_priority_info_content)
                    .setTitle(R.string.card_priority_info_title)
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}