package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.enums.canAlsoCheckAll
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class SupportClassPicker @Inject constructor(
    private val screen: SupportScreen,
    private val supportPrefs: ISupportPreferences,
) {
    fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None) {
            return
        }

        screen.click(supportClass)

        screen.delay(0.5.seconds)
    }

    fun shouldAlsoCheckAll() =
        supportPrefs.alsoCheckAll && supportPrefs.supportClass.canAlsoCheckAll
}
