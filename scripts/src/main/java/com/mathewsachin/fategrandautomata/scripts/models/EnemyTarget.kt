package com.mathewsachin.fategrandautomata.scripts.models

sealed class EnemyTarget(val autoSkillCode: Char) {
    object A : EnemyTarget('1')
    object B : EnemyTarget('2')
    object C : EnemyTarget('3')

    companion object {
        val list = listOf(A, B, C)
    }
}