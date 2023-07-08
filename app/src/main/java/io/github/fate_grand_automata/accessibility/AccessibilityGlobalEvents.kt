package io.github.fate_grand_automata.accessibility

import android.accessibilityservice.AccessibilityService
import io.github.lib_automata.GlobalEventService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AccessibilityGlobalEvents @Inject constructor() : GlobalEventService {
    override fun pressBack() = runBlocking {
        TapperService.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
}