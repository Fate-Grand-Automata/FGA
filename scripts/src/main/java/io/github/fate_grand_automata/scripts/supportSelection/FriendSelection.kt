package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FriendSelection @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {
    fun check(friendNames: List<String>, bounds: SupportBounds): Boolean {
        if (friendNames.isEmpty())
            return true

        val searchRegion = bounds.region.clip(locations.support.friendsRegion)

        return friendNames
            .flatMap { entry -> images.loadSupportPattern(SupportImageKind.Friend, entry) }
            .any {
                it in searchRegion
            }
    }
}