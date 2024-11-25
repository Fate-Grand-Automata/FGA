package io.github.fate_grand_automata.scripts.models

sealed class ServantTarget(
    val autoSkillCode: Char,
    val specialTarget: String = ""
) {
    data object A : ServantTarget('1')
    data object B : ServantTarget('2')
    data object C : ServantTarget('3')

    // Emiya/BB Dubai
    data object Left : ServantTarget('7')
    data object Right : ServantTarget('8')

    @Deprecated("Use SpecialTarget instead", replaceWith = ReplaceWith("ServantTarget.SpecialTarget.Choice2OptionA"))
    data object Option1 : ServantTarget('K')
    @Deprecated("Use SpecialTarget instead", replaceWith = ReplaceWith("ServantTarget.SpecialTarget.Choice2OptionB"))
    data object Option2 : ServantTarget('U')

    // Mélusine/Ptolemaios
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

        // Kukulkan/UKD-Barghest
        data object Choice2OptionA : SpecialTarget("Ch2A")
        data object Choice2OptionB : SpecialTarget("Ch2B")

        // Soujuurou/Charlotte/Hakuno
        data object Choice3OptionA : SpecialTarget("Ch3A")
        data object Choice3OptionB : SpecialTarget("Ch3B")
        data object Choice3OptionC : SpecialTarget("Ch3C")
    }

    companion object {
        val list by lazy {
            listOf(
                A, B, C,
                Left, Right,
                Option1, Option2, // for old configs
                Transform,
                SpecialTarget.Choice2OptionA,
                SpecialTarget.Choice2OptionB,
                SpecialTarget.Choice3OptionA,
                SpecialTarget.Choice3OptionB,
                SpecialTarget.Choice3OptionC
            )
        }
    }
}