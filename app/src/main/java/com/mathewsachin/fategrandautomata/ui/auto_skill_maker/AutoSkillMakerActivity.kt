package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import com.mathewsachin.fategrandautomata.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSkillMakerActivity : AppCompatActivity() {
    val vm: AutoSkillMakerViewModel by viewModels()
    val args: AutoSkillMakerActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.auto_skill_maker)
    }

    fun isMainScreen() = findNavController(R.id.nav_host_fragment_auto_skill)
        .currentDestination?.id == R.id.main_auto_skill_maker

    override fun onBackPressed() {
        if (isMainScreen()) {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? AutoSkill command will be lost.")
                .setTitle("Confirm Exit")
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
