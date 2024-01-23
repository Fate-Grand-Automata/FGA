package io.github.fate_grand_automata.ui.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialViewModel @Inject constructor(
    val battleConfig: IBattleConfig,
    val battleConfigCore: BattleConfigCore
) : ViewModel() {

    private val _selectedMaterials = battleConfigCore.materials

    val selectedMaterials = _selectedMaterials.asFlow().map { it.toList().sortedBy { it.rarity } }

    private val _materialsListTracker = MutableStateFlow(emptyList<UndoTracker>())

    val materialsListTracker = _materialsListTracker.asStateFlow()

    private val _initialSavedMaterials = MutableStateFlow(emptyList<MaterialEnum>())

    val enableReset = selectedMaterials.combine(_initialSavedMaterials) { selected, initial ->
        selected.sorted() != initial.sorted()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _initialSavedMaterials.update {
                _selectedMaterials.get().toList()
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")

    val searchQuery = _searchQuery.asStateFlow()

    fun addMaterial(material: MaterialEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            battleConfigCore.materials.set(setOf(material) + _selectedMaterials.get())

            _materialsListTracker.update {
                it + UndoTracker(setOf(material), UndoAction.ADD)
            }
        }
    }

    fun removeMaterial(material: MaterialEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            battleConfigCore.materials.set(_selectedMaterials.get() - material)

            _materialsListTracker.update {
                it + UndoTracker(setOf(material), UndoAction.REMOVE)
            }
        }
    }

    fun removeAllMaterials() {
        viewModelScope.launch(Dispatchers.IO) {
            _materialsListTracker.update {
                it + UndoTracker(_selectedMaterials.get(), UndoAction.REMOVE_ALL)
            }
            battleConfigCore.materials.set(emptySet())


        }
    }

    fun undo() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastMaterial = _materialsListTracker.value.last()

            when (lastMaterial.undoAction) {
                UndoAction.ADD ->
                    battleConfigCore.materials.set(_selectedMaterials.get() - lastMaterial.materialSet.first())
                UndoAction.REMOVE ->
                    battleConfigCore.materials.set(_selectedMaterials.get() + lastMaterial.materialSet.first())
                UndoAction.REMOVE_ALL ->
                    battleConfigCore.materials.set(_selectedMaterials.get() + lastMaterial.materialSet)
            }


            _materialsListTracker.update {
                it - lastMaterial
            }
        }
    }

    fun reset() {
        viewModelScope.launch(Dispatchers.IO) {
            battleConfigCore.materials.set(_initialSavedMaterials.value.toSet())

            _materialsListTracker.update {
                emptyList()
            }
        }
    }

    fun onQueryUpdated(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchQuery.emit(query)
        }
    }

}