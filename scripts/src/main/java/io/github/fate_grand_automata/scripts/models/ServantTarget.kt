package io.github.fate_grand_automata.scripts.models

sealed class ServantTarget(
    val autoSkillCode: Char,
    val specialTarget: String = ""
) {
    data object A : ServantTarget('1')
    data object B : ServantTarget('2')
    data object C : ServantTarget('3')

    // Emiya
    data object Left : ServantTarget('7')
    data object Right : ServantTarget('8')

    // Kukulkan
    data object Option1 : ServantTarget('K')
    data object Option2 : ServantTarget('U')

    // Mélusine
    data object Melusine : ServantTarget('M')

    sealed class SpecialTarget(
        targetCode: String
    ) : ServantTarget(autoSkillCode = '[', specialTarget = targetCode) {

        companion object {
            private val codes = mutableSetOf<String>()

            private fun validateString(targetCode: String) {
                for (existingCode in codes) {
                    require(!(targetCode.startsWith(existingCode) || existingCode.startsWith(targetCode))) {
                        "Special target code " +
                                "$targetCode conflicts with existing code $existingCode"

                    }
                }
                codes.add(targetCode)
            }
        }

        init {
            validateString(targetCode)
        }

        // Soujuurou or Charlotte
        data object TriChoice1 : SpecialTarget("TCh1")
        data object TriChoice2 : SpecialTarget("TCh2")
        data object TriChoice3 : SpecialTarget("TCh3")
    }

    companion object {
        val list by lazy {
            listOf(
                A, B, C,
                Left, Right,
                Option1, Option2,
                Melusine,
                SpecialTarget.TriChoice1,
                SpecialTarget.TriChoice2,
                SpecialTarget.TriChoice3
            )
        }
    }
}