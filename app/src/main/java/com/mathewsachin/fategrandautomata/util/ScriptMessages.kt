package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ScriptMessages @Inject constructor(@ApplicationContext val context: Context) : IScriptMessages {
    override val apRanOut: String
        get() = context.getString(R.string.script_msg_ap_ran_out)
}