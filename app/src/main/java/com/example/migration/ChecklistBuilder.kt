package com.example.migration

import com.example.domain.*

class ChecklistBuilder(private val coverageEngine: CoverageEngine) {

    fun build(inventory: List<InventoryItem>): List<ChecklistItem> {
        return inventory.map { item ->
            val decision = coverageEngine.analyze(item)
            ChecklistItem(
                id = item.id,
                title = item.name,
                source = decision.source,
                instruction = decision.instruction,
                action = decision.method,
                verified = decision.verified,
                completed = false,
                category = item.category
            )
        }
    }
}
