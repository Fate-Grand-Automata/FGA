package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportFriendChecker @Inject constructor(
    api: IFgoAutomataApi,
) : IFgoAutomataApi by api {
    fun isFriend(bounds: SupportBounds? = null): Boolean {
        val friendRegion = bounds?.region?.clip(locations.support.friendRegion)
            ?: locations.support.friendRegion

        return sequenceOf(
            images[Images.Friend],
            images[Images.Guest],
            images[Images.Follow],
        ).any { it in friendRegion }
    }
}
