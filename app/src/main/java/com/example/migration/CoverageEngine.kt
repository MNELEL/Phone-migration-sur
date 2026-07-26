package com.example.migration

import com.example.domain.*

class CoverageEngine {

    fun analyze(item: InventoryItem): CoverageDecision {
        return when (item.type) {
            InventoryType.CONTACTS -> CoverageDecision(
                source = CoverageSource.GOOGLE_BUILTIN,
                method = "Google Backup",
                instruction = "Verify that contacts are synchronized",
                verified = false
            )
            InventoryType.PHOTOS -> CoverageDecision(
                source = CoverageSource.GOOGLE_BUILTIN,
                method = "Google Photos",
                instruction = "Verify Google Photos backup",
                verified = false
            )
            InventoryType.APP -> {
                // Determine based on package name or rules. Simple fallback for MVP
                if (item.category == "SECURITY" || item.category == "FINANCE") {
                    CoverageDecision(
                        source = CoverageSource.MANUAL,
                        method = "Manual Login/Export",
                        instruction = "Log in manually or export tokens",
                        verified = false
                    )
                } else {
                    CoverageDecision(
                        source = CoverageSource.MANUFACTURER,
                        method = "Android Backup / Store",
                        instruction = "Verify installation and app data",
                        verified = false
                    )
                }
            }
            else -> CoverageDecision(
                source = CoverageSource.MANUAL,
                method = "Manual",
                instruction = "Perform manual verification",
                verified = false
            )
        }
    }
}
