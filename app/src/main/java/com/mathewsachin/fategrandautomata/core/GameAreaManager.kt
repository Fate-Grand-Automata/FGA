package com.mathewsachin.fategrandautomata.core

class GameAreaManager {
    companion object {
        var ScriptDimension: CompareSettings? = null
        var CompareDimension: CompareSettings? = null

        private var gameArea: Region? = null

        var GameArea get() = gameArea ?: AutomataApi.WindowRegion
            set(value) { gameArea = value }

        fun reset() {
            ScriptDimension = null
            CompareDimension = null
            gameArea = null
        }
    }
}