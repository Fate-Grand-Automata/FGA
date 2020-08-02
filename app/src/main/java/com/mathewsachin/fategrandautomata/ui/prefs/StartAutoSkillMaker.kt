package com.mathewsachin.fategrandautomata.ui.prefs

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillCommandKey
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerActivity

class StartAutoSkillMaker : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(
            context,
            AutoSkillMakerActivity::class.java
        )

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra(AutoSkillCommandKey)
        } else null
    }
}