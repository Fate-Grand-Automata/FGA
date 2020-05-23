package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.extractSupportImgs
import com.mathewsachin.fategrandautomata.scripts.shouldExtractSupportImages
import com.mathewsachin.fategrandautomata.util.findCeList
import com.mathewsachin.fategrandautomata.util.findServantList
import com.mathewsachin.fategrandautomata.util.preferredSupportOnResume
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class SupportSettingsBaseFragment : PreferenceFragmentCompat() {
    private val scope = MainScope()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    protected fun performSupportImageExtraction() {
        scope.launch {
            extractSupportImgs()
            Toast.makeText(activity, "Support Images Extracted Successfully", Toast.LENGTH_SHORT).show()
            preferredSupportOnResume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        if (shouldExtractSupportImages) {
            performSupportImageExtraction()
        }
        else preferredSupportOnResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.support_clear_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_support_servants -> {
                findServantList()?.values = emptySet()
                true
            }
            R.id.action_clear_support_ces -> {
                findCeList()?.values = emptySet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
