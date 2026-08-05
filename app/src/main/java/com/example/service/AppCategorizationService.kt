package com.example.service

import android.content.pm.ApplicationInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppFolderCategory(
    val id: String,
    val titleHebrew: String,
    val descriptionHebrew: String,
    val icon: ImageVector,
    val accentColorHex: Long
) {
    SOCIAL(
        id = "social",
        titleHebrew = "רשתות חברתיות ותקשורת",
        descriptionHebrew = "WhatsApp, Telegram, Facebook, Instagram, TikTok ורשתות נוספות",
        icon = Icons.Default.Share,
        accentColorHex = 0xFF2563EB
    ),
    PRODUCTIVITY(
        id = "productivity",
        titleHebrew = "פרודוקטיביות ועבודה",
        descriptionHebrew = "דוא\"ל, מסמכים, Office, Slack, Zoom, Notion ויומנים",
        icon = Icons.Default.Work,
        accentColorHex = 0xFF0F766E
    ),
    GAMES(
        id = "games",
        titleHebrew = "משחקים ופנאי",
        descriptionHebrew = "משחקי פעולה, אסטרטגיה, פאזלים, קלפים ופנאי",
        icon = Icons.Default.SportsEsports,
        accentColorHex = 0xFF7C3AED
    ),
    FINANCE_SECURITY(
        id = "finance_security",
        titleHebrew = "פיננסים ואבטחה",
        descriptionHebrew = "אפליקציות בנק, ארנקים דיגיטליים, 2FA ומזהי אבטחה",
        icon = Icons.Default.Shield,
        accentColorHex = 0xFFDC2626
    ),
    MEDIA_ENTERTAINMENT(
        id = "media_entertainment",
        titleHebrew = "מדיה ובידור",
        descriptionHebrew = "נגני מוזיקה, סרטונים, YouTube, Spotify, Netflix ותמונות",
        icon = Icons.Default.Movie,
        accentColorHex = 0xFFD97706
    ),
    SHOPPING_TRAVEL(
        id = "shopping_travel",
        titleHebrew = "קניות, ניווט וטיולים",
        descriptionHebrew = "Waze, Google Maps, Uber, Gett, AliExpress, Booking",
        icon = Icons.Default.ShoppingBag,
        accentColorHex = 0xFF059669
    ),
    TOOLS_UTILITIES(
        id = "tools_utilities",
        titleHebrew = "כלים ומערכת",
        descriptionHebrew = "מחשבון, דפדפן, מנהל קבצים, שעון והגדרות",
        icon = Icons.Default.Build,
        accentColorHex = 0xFF4B5563
    ),
    OTHER(
        id = "other",
        titleHebrew = "אחר / ללא סיווג",
        descriptionHebrew = "אפליקציות שונות וכלליות",
        icon = Icons.Default.Category,
        accentColorHex = 0xFF6B7280
    );

    companion object {
        fun fromId(id: String): AppFolderCategory {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) }
                ?: when (id.uppercase()) {
                    "ESSENTIAL" -> FINANCE_SECURITY
                    "FINANCE" -> FINANCE_SECURITY
                    "SECURITY" -> FINANCE_SECURITY
                    "GAMES" -> GAMES
                    "PRODUCTIVITY" -> PRODUCTIVITY
                    "SOCIAL" -> SOCIAL
                    "MEDIA" -> MEDIA_ENTERTAINMENT
                    "TOOLS" -> TOOLS_UTILITIES
                    "SHOPPING" -> SHOPPING_TRAVEL
                    else -> OTHER
                }
        }
    }
}

data class AppFolderGroup<T>(
    val category: AppFolderCategory,
    val items: List<T>
) {
    val count: Int get() = items.size
}

class AppCategorizationService {

    fun classifyApp(packageName: String, appName: String = "", appCategoryInt: Int? = null): AppFolderCategory {
        val pkg = packageName.lowercase()
        val name = appName.lowercase()

        // 1. Check system ApplicationInfo category if available (API 26+)
        if (appCategoryInt != null && appCategoryInt != ApplicationInfo.CATEGORY_UNDEFINED) {
            when (appCategoryInt) {
                ApplicationInfo.CATEGORY_GAME -> return AppFolderCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppFolderCategory.MEDIA_ENTERTAINMENT
                ApplicationInfo.CATEGORY_SOCIAL -> return AppFolderCategory.SOCIAL
                ApplicationInfo.CATEGORY_NEWS -> return AppFolderCategory.MEDIA_ENTERTAINMENT
                ApplicationInfo.CATEGORY_MAPS -> return AppFolderCategory.SHOPPING_TRAVEL
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppFolderCategory.PRODUCTIVITY
            }
        }

        // 2. Keyword & Package Pattern Matching heuristics

        // Social & Messaging
        if (pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") ||
            pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("tiktok") ||
            pkg.contains("twitter") || pkg.contains("x.corp") || pkg.contains("snapchat") ||
            pkg.contains("discord") || pkg.contains("viber") || pkg.contains("messenger") ||
            pkg.contains("reddit") || name.contains("whatsapp") || name.contains("טלגרם") ||
            name.contains("פייסבוק") || name.contains("אינסטגרם")
        ) {
            return AppFolderCategory.SOCIAL
        }

        // Finance, Security & Banking
        if (pkg.contains("bank") || pkg.contains("pay") || pkg.contains("wallet") ||
            pkg.contains("finance") || pkg.contains("auth") || pkg.contains("security") ||
            pkg.contains("pass") || pkg.contains("health") || pkg.contains("mfa") ||
            pkg.contains("crypto") || pkg.contains("bit") || pkg.contains("pepper") ||
            pkg.contains("discount") || pkg.contains("leumi") || pkg.contains("hapoalim") ||
            pkg.contains("mizrahi") || pkg.contains("cal") || pkg.contains("max") ||
            name.contains("בנק") || name.contains("אשראי") || name.contains("ביט") ||
            name.contains("פייפאל") || name.contains("paypal")
        ) {
            return AppFolderCategory.FINANCE_SECURITY
        }

        // Games
        if (pkg.contains("game") || pkg.contains("arcade") || pkg.contains("puzzle") ||
            pkg.contains("casino") || pkg.contains("racing") || pkg.contains("sports") ||
            pkg.contains("chess") || pkg.contains("cards") || pkg.contains("rpg") ||
            pkg.contains("simulation") || pkg.contains("clash") || pkg.contains("candy") ||
            pkg.contains("roblox") || pkg.contains("pubg") || pkg.contains("epicgames") ||
            name.contains("משחק") || name.contains("שחמט") || name.contains("קלפים")
        ) {
            return AppFolderCategory.GAMES
        }

        // Shopping, Travel & Navigation
        if (pkg.contains("map") || pkg.contains("waze") || pkg.contains("uber") ||
            pkg.contains("gett") || pkg.contains("flight") || pkg.contains("hotel") ||
            pkg.contains("amazon") || pkg.contains("ebay") || pkg.contains("ali") ||
            pkg.contains("booking") || pkg.contains("tenbis") || pkg.contains("wolth") ||
            pkg.contains("shein") || pkg.contains("shufersal") || name.contains("וויז") ||
            name.contains("מפה") || name.contains("מונית") || name.contains("קניות")
        ) {
            return AppFolderCategory.SHOPPING_TRAVEL
        }

        // Media, Audio & Video
        if (pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("spotify") ||
            pkg.contains("podcast") || pkg.contains("music") || pkg.contains("video") ||
            pkg.contains("media") || pkg.contains("tv") || pkg.contains("camera") ||
            pkg.contains("gallery") || pkg.contains("photos") || pkg.contains("vlc") ||
            pkg.contains("disney") || pkg.contains("hot") || pkg.contains("yes") ||
            name.contains("יוטיוב") || name.contains("נגן") || name.contains("סרט")
        ) {
            return AppFolderCategory.MEDIA_ENTERTAINMENT
        }

        // Productivity & Work
        if (pkg.contains("mail") || pkg.contains("office") || pkg.contains("note") ||
            pkg.contains("doc") || pkg.contains("sheet") || pkg.contains("drive") ||
            pkg.contains("pdf") || pkg.contains("slack") || pkg.contains("teams") ||
            pkg.contains("zoom") || pkg.contains("notion") || pkg.contains("keep") ||
            pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("calendar") ||
            pkg.contains("scanner") || name.contains("מייל") || name.contains("מסמך")
        ) {
            return AppFolderCategory.PRODUCTIVITY
        }

        // Tools & Utilities
        if (pkg.contains("tool") || pkg.contains("calc") || pkg.contains("clock") ||
            pkg.contains("file") || pkg.contains("cleaner") || pkg.contains("setting") ||
            pkg.contains("battery") || pkg.contains("speed") || pkg.contains("zip") ||
            name.contains("מחשבון") || name.contains("שעון") || name.contains("קבצים")
        ) {
            return AppFolderCategory.TOOLS_UTILITIES
        }

        return AppFolderCategory.OTHER
    }

    fun <T> groupAppsByFolder(
        apps: List<T>,
        getPackageName: (T) -> String,
        getAppName: (T) -> String = { "" },
        getCategoryOverride: (T) -> String? = { null }
    ): List<AppFolderGroup<T>> {
        val groups = AppFolderCategory.values().associateWith { mutableListOf<T>() }

        apps.forEach { app ->
            val override = getCategoryOverride(app)
            val category = if (!override.isNullOrEmpty()) {
                AppFolderCategory.fromId(override)
            } else {
                classifyApp(getPackageName(app), getAppName(app))
            }
            groups[category]?.add(app)
        }

        return groups.map { (cat, items) -> AppFolderGroup(cat, items) }
            .filter { it.items.isNotEmpty() }
            .sortedBy { it.category.ordinal }
    }
}
