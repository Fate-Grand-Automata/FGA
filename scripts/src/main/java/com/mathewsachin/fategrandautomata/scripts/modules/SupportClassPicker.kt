package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.canAlsoCheckAll
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class SupportClassPicker @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences
) : IFgoAutomataApi by api {
    fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None)
            return

        locations.support.locate(supportClass).click()

        Duration.seconds(0.5).wait()
    }

    fun shouldAlsoCheckAll() =
        supportPrefs.alsoCheckAll && supportPrefs.supportClass.canAlsoCheckAll
}