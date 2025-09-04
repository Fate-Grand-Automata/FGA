package io.github.fate_grand_automata.ui.spam

import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill

data class TargetPickerRequest(
    val skill: Skill,
    val onConfirm: (List<ServantTarget>) -> Unit
)