package io.github.fate_grand_automata.util

import io.github.fate_grand_automata.accessibility.TapperService
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.GameServers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The server the user plays on.
 *
 * [io.github.fate_grand_automata.scripts.prefs.IPreferences.gameServer] can't answer that outside a
 * script run — it is only assigned when a run starts — so anything in the UI has to resolve the
 * `Game Server` setting itself. The rules are the same ones a run uses: the explicit choice, or with
 * auto-detect whatever server the accessibility service last saw in front of the game.
 */
@Singleton
class GameServerResolver @Inject constructor(
    private val prefsCore: PrefsCore
) {
    fun resolve(): GameServer {
        val configured = prefsCore.gameServerRaw.get()

        if (configured == PrefsCore.GAME_SERVER_AUTO_DETECT) {
            return TapperService.instance?.detectedFgoServer ?: GameServers.default
        }

        return GameServers.deserialize(configured) ?: GameServers.default
    }
}
