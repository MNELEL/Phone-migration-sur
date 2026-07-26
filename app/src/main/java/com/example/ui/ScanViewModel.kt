package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.ChecklistItem
import com.example.domain.InventoryItem
import com.example.domain.ScanReport
import com.example.migration.ChecklistBuilder
import com.example.migration.CoverageEngine
import com.example.migration.InventoryBuilder
import com.example.scanner.DeepScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanState(
    val running: Boolean = false,
    val progress: Int = 0,
    val stage: String = "",
    val report: ScanReport? = null,
    val inventory: List<InventoryItem> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val error: String? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ScanState())
    val state = _state.asStateFlow()
    
    private val scanner = DeepScanner(application)

    fun startScan() {
        if (_state.value.running) return
        
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(running = true, stage = "Scanning apps, contacts, and media...", error = null)
                
                val result = scanner.scan()
                
                _state.value = _state.value.copy(stage = "Building inventory...")
                val inventoryBuilder = InventoryBuilder()
                val inventory = inventoryBuilder.build(result)
                
                _state.value = _state.value.copy(stage = "Generating migration checklist...")
                val checklistBuilder = ChecklistBuilder(CoverageEngine())
                val checklist = checklistBuilder.build(inventory)
                
                _state.value = ScanState(
                    running = false,
                    progress = 100,
                    stage = "Complete",
                    report = result,
                    inventory = inventory,
                    checklist = checklist
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    running = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun toggleChecklistItem(id: String) {
        val currentChecklist = _state.value.checklist.toMutableList()
        val index = currentChecklist.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = currentChecklist[index]
            currentChecklist[index] = item.copy(completed = !item.completed)
            _state.value = _state.value.copy(checklist = currentChecklist)
        }
    }
}
