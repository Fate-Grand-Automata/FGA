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
    data object Transform : ServantTarget('M')

    sealed class SpecialTarget(
        targetCode: String
    ) : ServantTarget(autoSkillCode = SpecialCommand.StartSpecialTarget.autoSkillCode, specialTarget = targetCode) {

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

        // Soujuurou/Charlotte/Hakunon
        data object Choice3OptionA : SpecialTarget("Ch3A")
        data object Choice3OptionB : SpecialTarget("Ch3B")
        data object Choice3OptionC : SpecialTarget("Ch3C")
    }

    companion object {
        val list by lazy {
            listOf(
                A, B, C,
                Left, Right,
                Option1, Option2,
                Transform,
                SpecialTarget.Choice3OptionA,
                SpecialTarget.Choice3OptionB,
                SpecialTarget.Choice3OptionC
            )
        }
    }
}