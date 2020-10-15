package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import com.mathewsachin.fategrandautomata.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerActivity : AppCompatActivity(R.layout.skill_maker) {
    val vm: SkillMakerViewModel by viewModels()
    val args: SkillMakerActivityArgs by navArgs()

    fun isMainScreen() = findNavController(R.id.nav_host_fragment_skill_maker)
        .currentDestination?.id == R.id.main_skill_maker

    override fun onBackPressed() {
        if (isMainScreen()) {
            AlertDialog.Builder(this)
                .setMessage(R.string.skill_maker_confirm_exit_message)
                .setTitle(R.string.skill_maker_confirm_exit_title)
                .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                .setNegativeButton(android.R.string.no, null)
                .show()
            return
        }

        super.onBackPressed()
    }

    override fun onPause() {
        vm.saveState()

        super.onPause()
    }
}
