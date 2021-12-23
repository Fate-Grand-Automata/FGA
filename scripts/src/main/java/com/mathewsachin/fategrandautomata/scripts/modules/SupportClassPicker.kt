package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.canAlsoCheckAll
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class SupportClassPicker @Inject constructor(
    private val screen: SupportScreen,
    private val supportPrefs: ISupportPreferences
) {
    fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None)
            return

        screen.click(supportClass)

        screen.delay(Duration.seconds(0.5))
    }

    fun shouldAlsoCheckAll() =
        supportPrefs.alsoCheckAll && supportPrefs.supportClass.canAlsoCheckAll
}