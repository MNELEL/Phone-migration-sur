package com.example.migration

import com.example.domain.*

class InventoryBuilder {
    fun build(report: ScanReport): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()

        report.apps.forEach { app ->
            items.add(
                InventoryItem(
                    id = app.packageName,
                    name = app.appName,
                    type = InventoryType.APP,
                    packageName = app.packageName,
                    size = 0L,
                    priority = Priority.NORMAL,
                    category = "UNKNOWN" // Would be classified by AppClassifier
                )
            )
        }

        if (report.contacts.total > 0) {
            items.add(
                InventoryItem(
                    id = "contacts",
                    name = "Contacts",
                    type = InventoryType.CONTACTS,
                    packageName = null,
                    size = 0L,
                    priority = Priority.ESSENTIAL,
                    category = "DATA"
                )
            )
        }

        if (report.media.photos > 0 || report.media.videos > 0) {
            items.add(
                InventoryItem(
                    id = "photos",
                    name = "Photos & Videos",
                    type = InventoryType.PHOTOS,
                    packageName = null,
                    size = report.media.totalSize,
                    priority = Priority.ESSENTIAL,
                    category = "MEDIA"
                )
            )
        }

        return items
    }
}
