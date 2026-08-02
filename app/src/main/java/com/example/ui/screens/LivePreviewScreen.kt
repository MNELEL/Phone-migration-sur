package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Form Factors
enum class DeviceFormFactor(
    val title: String,
    val widthDp: Dp,
    val heightDp: Dp,
    val icon: ImageVector
) {
    MOBILE("נייד (310x540)", 310.dp, 540.dp, Icons.Default.Smartphone),
    TABLET("טאבלט (470x380)", 470.dp, 380.dp, Icons.Default.Tablet),
    FOLDABLE("מתקפל (400x500)", 400.dp, 500.dp, Icons.Default.Devices)
}

// Enums for Preview Customization
enum class PreviewTab(val label: String, val icon: ImageVector) {
    HOME_LAUNCHER("מסך הבית", Icons.Default.Home),
    MIGRATION_CARD("לוח מעבר", Icons.Default.MoveToInbox),
    LOCK_SCREEN("מסך נעילה", Icons.Default.Lock)
}

enum class GridOption(val title: String, val columns: Int, val rows: Int) {
    COMPACT("3 x 4 (מרווח)", 3, 4),
    BALANCED("4 x 5 (סטנדרטי)", 4, 5),
    DENSE("5 x 6 (צפוף)", 5, 6)
}

enum class IconShapeOption(val title: String, val cornerPercent: Int) {
    CIRCLE("עגול", 50),
    SQUIRCLE("מרובע מעוגל", 28),
    ROUNDED_SQUARE("פינות מעוגלות", 16),
    SQUARE("מרובע", 0)
}

enum class WallpaperTheme(val title: String, val colors: List<Color>) {
    OCEAN_DEEP("אוקיינוס עמוק", listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
    SUNSET_HORIZON("שקיעה חמה", listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))),
    CYBER_NEON("סייבר ניאון", listOf(Color(0xFF141E30), Color(0xFF243B55))),
    EMERALD_MINIMAL("אמרלד מינימלי", listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
    AMOLED_DARK("אמולד כהה", listOf(Color(0xFF050505), Color(0xFF181818)))
}

enum class AccentColorScheme(val title: String, val color: Color) {
    CYAN("תכלת אוקיינוס", Color(0xFF00B4D8)),
    EMERALD("ירוק אמרלד", Color(0xFF10B981)),
    PURPLE("סגול דינמי", Color(0xFF8B5CF6)),
    ORANGE("כתום זהב", Color(0xFFF59E0B)),
    ROSE("ורוד רוז", Color(0xFFF43F5E))
}

data class PreviewAppIcon(
    val name: String,
    val icon: ImageVector,
    val containerColor: Color,
    val isMigrated: Boolean = true
)

data class PrebuiltTemplateItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ui_design_drafts", Context.MODE_PRIVATE) }

    // Customization states
    var selectedFormFactor by remember { mutableStateOf(DeviceFormFactor.MOBILE) }
    var selectedTab by remember { mutableStateOf(PreviewTab.HOME_LAUNCHER) }
    var selectedGrid by remember { mutableStateOf(GridOption.BALANCED) }
    var selectedIconShape by remember { mutableStateOf(IconShapeOption.SQUIRCLE) }
    var selectedWallpaper by remember { mutableStateOf(WallpaperTheme.OCEAN_DEEP) }
    var selectedAccent by remember { mutableStateOf(AccentColorScheme.CYAN) }
    var isDarkMode by remember { mutableStateOf(true) }
    var isWidgetVisible by remember { mutableStateOf(true) }
    var isStorageWidgetVisible by remember { mutableStateOf(true) }
    var fontScale by remember { mutableFloatStateOf(1f) }
    var showGesturesBar by remember { mutableStateOf(true) }
    var liveFeedbackMsg by remember { mutableStateOf("שינויים משתקפים בזמן אמת בחלון התצוגה") }

    // Auto-save state
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var isSavingDraft by remember { mutableStateOf(false) }
    var lastSavedTimestamp by remember { mutableStateOf(prefs.getString("last_saved", "טרם נשמר") ?: "טרם נשמר") }

    // Pre-built Component Template Library state
    var insertedComponents by remember {
        mutableStateOf(
            listOf("hero_banner", "storage_gauge", "live_speed")
        )
    }
    var showLibrarySheet by remember { mutableStateOf(false) }
    var showDesignTipsSheet by remember { mutableStateOf(false) }

    // Available pre-built component templates
    val libraryTemplates = remember {
        listOf(
            PrebuiltTemplateItem("hero_banner", "כרזת מעבר ראשית", "באנר", "כרזה מרכזית להצגת קצב הסנכרון והסטטוס", Icons.Default.CloudSync),
            PrebuiltTemplateItem("storage_gauge", "כרטיס מד אחסון", "וידג'ט", "מד פריסת דיסק ונפח פנוי בזמן אמת", Icons.Default.Storage),
            PrebuiltTemplateItem("live_speed", "מד רוחב פס וקצב", "וידג'ט", "תצוגת מהירות העברה ב-MB/s ותעבורת ענן", Icons.Default.Speed),
            PrebuiltTemplateItem("device_admin", "פאנל התקדמות מעבר", "אבטחה", "חיווי סטטוס להעברת אפליקציות ונתונים", Icons.Default.Security),
            PrebuiltTemplateItem("cloud_backup", "תג סטטוס גיבוי", "אבטחה", "אינדיקטור להתקדמות הגיבוי בזמן מעבר", Icons.Default.Lock),
            PrebuiltTemplateItem("quick_actions", "רשת פעולות מהירות", "פקדים", "כפתורי קיצור דרך להתחלת מעבר ואיפוס", Icons.Default.GridView)
        )
    }

    // Auto-Save Effect
    LaunchedEffect(selectedGrid, selectedIconShape, selectedWallpaper, selectedAccent, fontScale, insertedComponents, selectedFormFactor, autoSaveEnabled) {
        if (autoSaveEnabled) {
            delay(7000) // Auto-save after 7 seconds of quiet state
            isSavingDraft = true
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            prefs.edit()
                .putString("grid", selectedGrid.name)
                .putString("shape", selectedIconShape.name)
                .putString("wallpaper", selectedWallpaper.name)
                .putString("accent", selectedAccent.name)
                .putFloat("font_scale", fontScale)
                .putString("form_factor", selectedFormFactor.name)
                .putString("inserted_components", insertedComponents.joinToString(","))
                .putString("last_saved", timeStr)
                .apply()
            delay(400)
            isSavingDraft = false
            lastSavedTimestamp = timeStr
            liveFeedbackMsg = "העיצוב נשמר אוטומטית ($timeStr)"
        }
    }

    // Apps list
    val appsList = remember {
        listOf(
            PreviewAppIcon("WhatsApp", Icons.Default.Smartphone, Color(0xFF25D366)),
            PreviewAppIcon("Chrome", Icons.Default.Language, Color(0xFF4285F4)),
            PreviewAppIcon("לאומי Bank", Icons.Default.AccountBalance, Color(0xFF003399)),
            PreviewAppIcon("Waze", Icons.Default.Navigation, Color(0xFF33CCFF)),
            PreviewAppIcon("Instagram", Icons.Default.CameraAlt, Color(0xFFE1306C)),
            PreviewAppIcon("Gmail", Icons.Default.Email, Color(0xFFEA4335)),
            PreviewAppIcon("Spotify", Icons.Default.MusicNote, Color(0xFF1DB954)),
            PreviewAppIcon("Telegram", Icons.Default.Send, Color(0xFF0088CC)),
            PreviewAppIcon("YouTube", Icons.Default.PlayCircle, Color(0xFFFF0000)),
            PreviewAppIcon("Drive", Icons.Default.Cloud, Color(0xFFFFBA00)),
            PreviewAppIcon("Settings", Icons.Default.Settings, Color(0xFF607D8B)),
            PreviewAppIcon("Photos", Icons.Default.Image, Color(0xFFAB47BC))
        )
    }

    val animatedWidth by animateDpAsState(targetValue = selectedFormFactor.widthDp, label = "widthAnim")
    val animatedHeight by animateDpAsState(targetValue = selectedFormFactor.heightDp, label = "heightAnim")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("תצוגה מקדימה בזמן אמת (Live Preview)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(onClick = { showDesignTipsSheet = true }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "הנחיות עיצוב M3", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        // Manual Save
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        prefs.edit()
                            .putString("grid", selectedGrid.name)
                            .putString("shape", selectedIconShape.name)
                            .putString("wallpaper", selectedWallpaper.name)
                            .putString("accent", selectedAccent.name)
                            .putFloat("font_scale", fontScale)
                            .putString("form_factor", selectedFormFactor.name)
                            .putString("inserted_components", insertedComponents.joinToString(","))
                            .putString("last_saved", timeStr)
                            .apply()
                        lastSavedTimestamp = timeStr
                        liveFeedbackMsg = "הטיוטה נשמרה ידנית בהצלחה ($timeStr)"
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "שמור טיוטה")
                    }
                    IconButton(onClick = {
                        selectedGrid = GridOption.BALANCED
                        selectedIconShape = IconShapeOption.SQUIRCLE
                        selectedWallpaper = WallpaperTheme.OCEAN_DEEP
                        selectedAccent = AccentColorScheme.CYAN
                        isDarkMode = true
                        isWidgetVisible = true
                        isStorageWidgetVisible = true
                        fontScale = 1f
                        insertedComponents = listOf("hero_banner", "storage_gauge", "live_speed")
                        liveFeedbackMsg = "הגדרות התצוגה אופסו לברירת המחדל"
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "איפוס עיצוב")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Status Banner & Auto-Save Indicator
            Surface(
                color = selectedAccent.color.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(selectedAccent.color, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = liveFeedbackMsg,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSavingDraft) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = selectedAccent.color)
                            Spacer(Modifier.width(6.dp))
                            Text("שומר...", style = MaterialTheme.typography.labelSmall, color = selectedAccent.color)
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp), tint = selectedAccent.color)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "נשמר: $lastSavedTimestamp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Screen Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = selectedAccent.color
            ) {
                PreviewTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            liveFeedbackMsg = "מוצגת קומפוזיציית ${tab.label}"
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tab.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Form Factor & Screen Size Selector Tool
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(8.dp))
                                Text("גודל מסך ותבנית מכשיר:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DeviceFormFactor.values().forEach { formFactor ->
                                    FilterChip(
                                        selected = selectedFormFactor == formFactor,
                                        onClick = {
                                            selectedFormFactor = formFactor
                                            liveFeedbackMsg = "גודל תצוגה שונה ל-${formFactor.title}"
                                        },
                                        label = { Text(formFactor.title.split(" ")[0], fontSize = 11.sp) },
                                        leadingIcon = {
                                            Icon(formFactor.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Phone Device Window Rendering
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "חלון תצוגת מכשיר היעד (${selectedFormFactor.title})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        // Device Mockup Frame Container with Smooth Size Transition
                        Surface(
                            modifier = Modifier
                                .width(animatedWidth)
                                .height(animatedHeight)
                                .shadow(16.dp, RoundedCornerShape(36.dp))
                                .border(4.dp, Color(0xFF2C2C2E), RoundedCornerShape(36.dp)),
                            shape = RoundedCornerShape(36.dp),
                            color = if (isDarkMode) Color(0xFF101010) else Color(0xFFF5F5F7)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Wallpaper Background
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(selectedWallpaper.colors))
                                )

                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Status Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "09:41",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )

                                        // Punch hole camera
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color.Black, CircleShape)
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    // Content rendering based on selectedTab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        when (selectedTab) {
                                            PreviewTab.HOME_LAUNCHER -> {
                                                HomeLauncherComposition(
                                                    gridOption = selectedGrid,
                                                    iconShape = selectedIconShape,
                                                    accentColor = selectedAccent.color,
                                                    showWidget = isWidgetVisible,
                                                    showStorageWidget = isStorageWidgetVisible,
                                                    fontScale = fontScale,
                                                    apps = appsList,
                                                    insertedComponents = insertedComponents,
                                                    formFactor = selectedFormFactor
                                                )
                                            }
                                            PreviewTab.MIGRATION_CARD -> {
                                                MigrationCardComposition(
                                                    accentColor = selectedAccent.color,
                                                    fontScale = fontScale,
                                                    insertedComponents = insertedComponents
                                                )
                                            }
                                            PreviewTab.LOCK_SCREEN -> {
                                                LockScreenComposition(
                                                    accentColor = selectedAccent.color,
                                                    fontScale = fontScale
                                                )
                                            }
                                        }
                                    }

                                    // Navigation Bar / Gestures Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (showGesturesBar) {
                                            Box(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(4.dp)
                                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                            )
                                        } else {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                modifier = Modifier.fillMaxWidth(0.6f)
                                            ) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Icon(Icons.Default.Circle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Icon(Icons.Default.CropSquare, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // UI Component & Template Library Button
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("ספריית תבניות ורכיבי UI", fontWeight = FontWeight.Bold)
                                        Text("בחר והוסף רכיבים מוכנים מראש לקומפוזיציה", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Button(
                                    onClick = { showLibrarySheet = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("הוסף רכיבים", fontSize = 12.sp)
                                }
                            }

                            if (insertedComponents.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text("רכיבים פעילים בתצוגה:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(insertedComponents) { compId ->
                                        val template = libraryTemplates.find { it.id == compId }
                                        template?.let { item ->
                                            InputChip(
                                                selected = true,
                                                onClick = { },
                                                label = { Text(item.title, fontSize = 11.sp) },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "הסר",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable {
                                                                insertedComponents = insertedComponents.filter { it != compId }
                                                                liveFeedbackMsg = "הרכיב '${item.title}' הוסר מהתצוגה"
                                                            }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Controls Section Header
                item {
                    Text(
                        text = "הגדרות עיצוב וקומפוזיציה בזמן אמת",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // App Grid Selection Control
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GridView, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("מבנה רשת האפליקציות (Launcher App Grid)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(10.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GridOption.values()) { grid ->
                                    FilterChip(
                                        selected = selectedGrid == grid,
                                        onClick = {
                                            selectedGrid = grid
                                            liveFeedbackMsg = "מבנה הרשת שונה ל-${grid.title}"
                                        },
                                        label = { Text(grid.title, fontSize = 12.sp) },
                                        leadingIcon = if (selectedGrid == grid) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }

                // Icon Shape Control
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("צורת אייקוני המערכת (Icon Shapes)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(10.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(IconShapeOption.values()) { shape ->
                                    FilterChip(
                                        selected = selectedIconShape == shape,
                                        onClick = {
                                            selectedIconShape = shape
                                            liveFeedbackMsg = "צורת האייקונים שונתה ל-${shape.title}"
                                        },
                                        label = { Text(shape.title, fontSize = 12.sp) },
                                        leadingIcon = if (selectedIconShape == shape) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }

                // Wallpaper & Accent Color Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("רקע וצבעי דגש (Wallpaper & Accent Palette)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(12.dp))

                            Text("רקע תצוגה:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(WallpaperTheme.values()) { wp ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.horizontalGradient(wp.colors))
                                            .border(
                                                width = if (selectedWallpaper == wp) 2.dp else 0.dp,
                                                color = if (selectedWallpaper == wp) Color.White else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedWallpaper = wp
                                                liveFeedbackMsg = "רקע המסך עודכן ל-${wp.title}"
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedWallpaper == wp) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text("צבע דגש (Accent Color):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(AccentColorScheme.values()) { accent ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(accent.color)
                                            .border(
                                                width = if (selectedAccent == accent) 3.dp else 0.dp,
                                                color = if (selectedAccent == accent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedAccent = accent
                                                liveFeedbackMsg = "צבע הדגש שונה ל-${accent.title}"
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedAccent == accent) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Auto-Save & Storage Preferences
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoMode, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("מנגנון שמירה אוטומטית (Auto-Save Engine)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("שמירת טיוטה ברקע בזמן אמת", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("שומר שינויים ב-SharedPreferences מקומי כל 7 שניות", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = autoSaveEnabled,
                                    onCheckedChange = { autoSaveEnabled = it }
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            liveFeedbackMsg = "עיצוב הקומפוזיציה הוחל בהצלחה על המכשיר החדש!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedAccent.color)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("החל עיצוב זה במכשיר היעד", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // UI Component Template Library Dialog / Sheet
    if (showLibrarySheet) {
        AlertDialog(
            onDismissRequest = { showLibrarySheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("ספריית רכיבים ותבניות UI מוכנות")
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(libraryTemplates) { item ->
                        val isAdded = insertedComponents.contains(item.id)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isAdded) {
                                            insertedComponents = insertedComponents.filter { it != item.id }
                                        } else {
                                            insertedComponents = insertedComponents + item.id
                                        }
                                    },
                                    colors = if (isAdded) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isAdded) "הסר" else "הוסף לתצוגה", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLibrarySheet = false }) {
                    Text("סגור")
                }
            }
        )
    }

    // Material Design 3 Design Tips Overlay
    if (showDesignTipsSheet) {
        AlertDialog(
            onDismissRequest = { showDesignTipsSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFB703))
                    Spacer(Modifier.width(8.dp))
                    Text("הנחיות וטיפי עיצוב (Material Design 3)")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    M3TipItem("יחס ניגודיות וצבעי M3", "צבע הדגש '${selectedAccent.title}' שנבחר מספק ניגודיות גבוהה מעל 4.5:1 מול הרקע הכהה, בהתאם לסטנדרט WCAG AA.")
                    M3TipItem("נגישות Touch Targets", "כל האייקונים והכפתורים ברשת המערכת מוגדרים עם מרווח מגע מינימלי של 48dp x 48dp להקשה נוחה.")
                    M3TipItem("רספונסיביות למסכים רחבים", "בעת מעבר למצב טאבלט (Tablet) או מתקפל (Foldable), הפריסה מתאימה עצמה אוטומטית לקומפוזיציית Supporting Pane.")
                    M3TipItem("שמירה ברקע (Auto-Save)", "כל שינוי במבנה, בצבעים ובגופנים נשמר אוטומטית למניעת אובדן עבודה.")
                }
            },
            confirmButton = {
                Button(onClick = { showDesignTipsSheet = false }) {
                    Text("אישור והבנתי")
                }
            }
        )
    }
}

@Composable
fun M3TipItem(title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
        }
    }
}

// Composables rendering phone screens inside mockup
@Composable
fun HomeLauncherComposition(
    gridOption: GridOption,
    iconShape: IconShapeOption,
    accentColor: Color,
    showWidget: Boolean,
    showStorageWidget: Boolean,
    fontScale: Float,
    apps: List<PreviewAppIcon>,
    insertedComponents: List<String>,
    formFactor: DeviceFormFactor
) {
    val isWideScreen = formFactor == DeviceFormFactor.TABLET

    if (isWideScreen) {
        // Two-pane supporting layout for tablets
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Pane: Components & Widgets
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                insertedComponents.forEach { compId ->
                    RenderInsertedComponent(compId, accentColor, fontScale)
                }
            }

            // Right Pane: Apps Grid
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridOption.columns),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps.take(gridOption.columns * gridOption.rows)) { app ->
                        AppIconItem(app, iconShape, fontScale)
                    }
                }
            }
        }
    } else {
        // Mobile vertical layout
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Render inserted components
            insertedComponents.forEach { compId ->
                RenderInsertedComponent(compId, accentColor, fontScale)
            }

            // Apps Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridOption.columns),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps.take(gridOption.columns * gridOption.rows)) { app ->
                    AppIconItem(app, iconShape, fontScale)
                }
            }
        }
    }
}

@Composable
fun RenderInsertedComponent(compId: String, accentColor: Color, fontScale: Float) {
    when (compId) {
        "hero_banner" -> {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "סנכרון ענן פעיל - 85%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (10 * fontScale).sp
                        )
                        Text(
                            text = "12/18 אפליקציות הועברו",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (8 * fontScale).sp
                        )
                    }
                }
            }
        }
        "storage_gauge" -> {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "אחסון פנוי: 84.2 GB",
                            color = Color.White,
                            fontSize = (9 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "OK",
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    )
                }
            }
        }
        "live_speed" -> {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("קצב העברה: 82 MB/s", color = Color.White, fontSize = (9 * fontScale).sp)
                    }
                }
            }
        }
        "device_admin" -> {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("מעבר אפליקציות: 12 מתוך 18 הותקנו", color = Color.White, fontSize = (9 * fontScale).sp)
                }
            }
        }
        "cloud_backup" -> {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("גיבוי בענן פעיל", color = Color.White, fontSize = (9 * fontScale).sp)
                }
            }
        }
    }
}

@Composable
fun AppIconItem(app: PreviewAppIcon, iconShape: IconShapeOption, fontScale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(iconShape.cornerPercent))
                .background(app.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(app.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text = app.name,
            color = Color.White,
            fontSize = (8.5f * fontScale).sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MigrationCardComposition(
    accentColor: Color,
    fontScale: Float,
    insertedComponents: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "תהליך שיבוט בתנועה",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * fontScale).sp
                    )

                    Text(
                        text = "82 MB/s",
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (11 * fontScale).sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = accentColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "מעביר: WhatsApp Database (3.2 GB)...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = (10 * fontScale).sp
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("מצב הרשאות", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (11 * fontScale).sp)
                Spacer(Modifier.height(4.dp))
                Text("הרשאות אושרו - מוכן להעברת נתונים", color = accentColor, fontSize = (9 * fontScale).sp)
            }
        }
    }
}

@Composable
fun LockScreenComposition(
    accentColor: Color,
    fontScale: Float
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "09:41",
                color = Color.White,
                fontSize = (42 * fontScale).sp,
                fontWeight = FontWeight.Light
            )

            Text(
                text = "יום שני, 27 ביולי",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (12 * fontScale).sp
            )
        }

        // Notification summary
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("מעבר מכשיר הושלם בהצלחה", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (11 * fontScale).sp)
                    Text("כל 18 האפליקציות והרשאותיהן מוכנות לשימוש", color = Color.White.copy(alpha = 0.7f), fontSize = (9 * fontScale).sp)
                }
            }
        }
    }
}
