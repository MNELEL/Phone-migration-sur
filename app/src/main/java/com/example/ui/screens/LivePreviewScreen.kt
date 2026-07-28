package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewScreen(
    onBack: () -> Unit
) {
    // Customization states
    var selectedTab by remember { mutableStateOf(PreviewTab.HOME_LAUNCHER) }
    var selectedGrid by remember { mutableStateOf(GridOption.BALANCED) }
    var selectedIconShape by remember { mutableStateOf(IconShapeOption.SQUIRCLE) }
    var selectedWallpaper by remember { mutableStateOf(WallpaperTheme.OCEAN_DEEP) }
    var selectedAccent by remember { mutableStateOf(AccentColorScheme.CYAN) }
    var isDarkMode by remember { mutableStateOf(true) }
    var isWidgetVisible by remember { mutableStateOf(true) }
    var isStorageWidgetVisible by remember { mutableStateOf(true) }
    var fontScale by remember { mutableFloatStateOf(1f) } // 0.85f, 1f, 1.15f
    var showGesturesBar by remember { mutableStateOf(true) }
    var liveFeedbackMsg by remember { mutableStateOf("שינויים משתקפים בזמן אמת בחלון התצוגה") }

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
                    IconButton(onClick = {
                        selectedGrid = GridOption.BALANCED
                        selectedIconShape = IconShapeOption.SQUIRCLE
                        selectedWallpaper = WallpaperTheme.OCEAN_DEEP
                        selectedAccent = AccentColorScheme.CYAN
                        isDarkMode = true
                        isWidgetVisible = true
                        isStorageWidgetVisible = true
                        fontScale = 1f
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
            // Live Status Banner
            Surface(
                color = selectedAccent.color.copy(alpha = 0.15f),
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

                    Text(
                        text = "LIVE UI ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = selectedAccent.color,
                        letterSpacing = 1.sp
                    )
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

                // Live Phone Device Window Rendering
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "חלון תצוגת מכשיר היעד (Target Device Render Window)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        // Device Mockup Frame Container
                        Surface(
                            modifier = Modifier
                                .width(310.dp)
                                .height(560.dp)
                                .shadow(16.dp, RoundedCornerShape(38.dp))
                                .border(4.dp, Color(0xFF2C2C2E), RoundedCornerShape(38.dp)),
                            shape = RoundedCornerShape(38.dp),
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
                                                    apps = appsList
                                                )
                                            }
                                            PreviewTab.MIGRATION_CARD -> {
                                                MigrationCardComposition(
                                                    accentColor = selectedAccent.color,
                                                    fontScale = fontScale
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

                // Widgets & Layout Options Toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Widgets, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("רכיבים ווידג'טים במסך הבית", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("הצג ווידג'ט סטטוס מעבר וסנכרון", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = isWidgetVisible,
                                    onCheckedChange = {
                                        isWidgetVisible = it
                                        liveFeedbackMsg = if (it) "ווידג'ט מעבר נגלה" else "ווידג'ט מעבר הוסתר"
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("הצג ווידג'ט נפח אחסון ודיסק", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = isStorageWidgetVisible,
                                    onCheckedChange = {
                                        isStorageWidgetVisible = it
                                        liveFeedbackMsg = if (it) "ווידג'ט אחסון נגלה" else "ווידג'ט אחסון הוסתר"
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("פס ניווט במחוות (Gestures Bar)", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = showGesturesBar,
                                    onCheckedChange = { showGesturesBar = it }
                                )
                            }
                        }
                    }
                }

                // Font Scaling Control
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatSize, contentDescription = null, tint = selectedAccent.color)
                                Spacer(Modifier.width(10.dp))
                                Text("גודל גופנים וקנה מידה (Text Scale)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Pair("קטן (0.85x)", 0.85f),
                                    Pair("רציף (1.0x)", 1.0f),
                                    Pair("מוגדל (1.15x)", 1.15f)
                                ).forEach { (label, scale) ->
                                    FilterChip(
                                        selected = fontScale == scale,
                                        onClick = {
                                            fontScale = scale
                                            liveFeedbackMsg = "גודל הטקסט עודכן ל-$label"
                                        },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
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
    apps: List<PreviewAppIcon>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Widget 1: Migration Progress Status
        if (showWidget) {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "סנכרון ענן פעיל - 85%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (11 * fontScale).sp
                        )
                        Text(
                            text = "12/18 אפליקציות הועברו",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (9 * fontScale).sp
                        )
                    }
                }
            }
        }

        // Widget 2: Storage Status
        if (showStorageWidget) {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp),
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
                            fontSize = (10 * fontScale).sp,
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

        // Apps Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridOption.columns),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps.take(gridOption.columns * gridOption.rows)) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(iconShape.cornerPercent))
                            .background(app.containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(app.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = app.name,
                        color = Color.White,
                        fontSize = (9 * fontScale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MigrationCardComposition(
    accentColor: Color,
    fontScale: Float
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
                Text("מצב הרשאות מערכת", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (11 * fontScale).sp)
                Spacer(Modifier.height(4.dp))
                Text("הרשאת מנהל מכשיר פעילה - התקנה שקטה ברקע מאושרת", color = accentColor, fontSize = (9 * fontScale).sp)
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
