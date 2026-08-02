package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import com.example.service.ChecklistExporter
import com.example.domain.ChecklistItem
import com.example.domain.CoverageSource
import com.example.ui.ScanViewModel
import com.example.ui.components.DataSafetyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToOrchestrator: () -> Unit = {},
    onNavigateToCloudDestination: () -> Unit = {},
    onNavigateToQrWizard: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppList: () -> Unit = {},
    onNavigateToLivePreview: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "ESSENTIAL", "PRODUCTIVITY", "GAMES"
    var sortOption by remember { mutableStateOf("DEFAULT") } // "DEFAULT", "SIZE", "USAGE"
    var showDataSafetyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPlayStoreDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbarMessage()
        }
    }

    val filteredList = state.checklist.filter { item ->
        val matchesQuery = item.title.contains(searchQuery, ignoreCase = true) ||
                item.instruction.contains(searchQuery, ignoreCase = true) ||
                (item.action?.contains(searchQuery, ignoreCase = true) == true)
        
        val invItem = state.inventory.find { it.id == item.id }
        val category = invItem?.category ?: item.category
        
        val matchesCategory = when (selectedCategoryFilter) {
            "ALL" -> true
            "ESSENTIAL" -> category == "ESSENTIAL" || category == "SECURITY" || category == "FINANCE" || item.id == "contacts" || item.id == "photos"
            "PRODUCTIVITY" -> category == "PRODUCTIVITY" || category == "UTILITIES" || category == "DATA"
            "GAMES" -> category == "GAMES"
            else -> true
        }

        matchesQuery && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("מרכז מעבר ולוח בקרה", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזור")
                    }
                },
                actions = {
                    IconButton(onClick = { showDataSafetyDialog = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.PrivacyTip, contentDescription = "בטיחות נתונים")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "הגדרות")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "כלי מעבר וסנכרון מהירים",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            // Quick Tools Action Hub Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickToolChip(
                            title = "סורק ומצלמה",
                            icon = Icons.Default.CameraAlt,
                            onClick = onNavigateToCamera
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "ייצוא פרויקט ZIP",
                            icon = Icons.Default.FolderZip,
                            onClick = onNavigateToSettings
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "אפליקציות מותקנות",
                            icon = Icons.Default.Android,
                            onClick = onNavigateToAppList
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "נפח אחסון",
                            icon = androidx.compose.material.icons.Icons.Default.Storage,
                            onClick = onNavigateToDashboard
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "אשף מעבר",
                            icon = androidx.compose.material.icons.Icons.Default.Cloud,
                            onClick = onNavigateToOrchestrator
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "יעד ענן",
                            icon = androidx.compose.material.icons.Icons.Default.Cloud,
                            onClick = onNavigateToCloudDestination
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "מנהל מכשיר",
                            icon = androidx.compose.material.icons.Icons.Default.AdminPanelSettings,
                            onClick = onNavigateToDeviceAdmin
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "תצוגה מקדימה",
                            icon = androidx.compose.material.icons.Icons.Default.Smartphone,
                            onClick = onNavigateToLivePreview
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "פרסום ב-Google Play",
                            icon = Icons.Default.Storefront,
                            onClick = { showPlayStoreDialog = true }
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "ייצוא PDF / TXT",
                            icon = Icons.Default.PictureAsPdf,
                            onClick = { showExportDialog = true }
                        )
                    }
                    item {
                        QuickToolChip(
                            title = "דוח סנכרון",
                            icon = androidx.compose.material.icons.Icons.Default.CheckCircle,
                            onClick = onNavigateToReport
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("התקדמות העברה", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                val completed = state.checklist.count { it.completed }
                                val total = state.checklist.size
                                val percent = if (total == 0) 0 else (completed * 100) / total
                                Text("${percent}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showExportDialog = true },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("ייצוא PDF / TXT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.shareChecklist(context) },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ) {
                                    Text("JSON", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        val completed = state.checklist.count { it.completed }
                        val total = state.checklist.size
                        val percent = if (total == 0) 0 else (completed * 100) / total
                        val progressFloat = if (total == 0) 0f else completed.toFloat() / total.toFloat()
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                    }
                }
            }
            
            item {
                val total = state.checklist.size
                val completed = state.checklist.count { it.completed }
                val isAllCompleted = total > 0 && total == completed

                AnimatedVisibility(
                    visible = isAllCompleted,
                    enter = expandVertically() + fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = shrinkVertically() + fadeOut() + scaleOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = "הושלם",
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(scale),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "כל הכבוד!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "כל משימות ההעברה הושלמו בהצלחה. המכשיר שלך מוכן לשימוש!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showExportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryContainer, contentColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ייצא דוח מעבר PDF / TXT לתיעוד", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                CloudSyncCard(state = state, viewModel = viewModel)
            }

            item {
                DeviceAdminCard()
            }

            item {
                GoogleDriveBackupCard(state = state, viewModel = viewModel)
            }

            item {
                CloudRestoreCard(state = state, viewModel = viewModel)
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("חפש אפליקציות ואנשי קשר...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "סמל חיפוש")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "נקה חיפוש")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                // Category Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = { selectedCategoryFilter = "ALL" },
                            label = { Text("הכל") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ESSENTIAL",
                            onClick = { selectedCategoryFilter = "ESSENTIAL" },
                            label = { Text("חיוני") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "PRODUCTIVITY",
                            onClick = { selectedCategoryFilter = "PRODUCTIVITY" },
                            label = { Text("פרודוקטיביות") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "GAMES",
                            onClick = { selectedCategoryFilter = "GAMES" },
                            label = { Text("משחקים") }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "מיין לפי:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 8.dp, end = 8.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = sortOption == "DEFAULT",
                            onClick = { sortOption = "DEFAULT" },
                            label = { Text("ברירת מחדל") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = sortOption == "SIZE",
                            onClick = { sortOption = "SIZE" },
                            label = { Text("גודל קובץ") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = sortOption == "USAGE",
                            onClick = { sortOption = "USAGE" },
                            label = { Text("תדירות שימוש") }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "משימות העברה מסווגות",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
            }
            
            val sortedList = filteredList.sortedWith(
                when (sortOption) {
                    "SIZE" -> compareByDescending<ChecklistItem> { it.size }
                    "USAGE" -> compareByDescending<ChecklistItem> { it.usageFrequency }
                    else -> compareBy<ChecklistItem> { it.source != CoverageSource.GOOGLE_BUILTIN }
                }
            )
            
            items(sortedList) { item ->
                ChecklistRow(item = item, sortOption = sortOption) {
                    viewModel.toggleChecklistItem(item.id)
                }
            }
            
            item {
                Spacer(Modifier.height(24.dp))
            }
        }

        DataSafetyDialog(
            showDialog = showDataSafetyDialog,
            onDismiss = { showDataSafetyDialog = false }
        )

        ExportChecklistDialog(
            showDialog = showExportDialog,
            onDismiss = { showExportDialog = false },
            checklistItems = state.checklist,
            onExportSuccess = { viewModel.showSnackbarMessage(it) },
            onExportError = { viewModel.showSnackbarMessage(it) }
        )

        PlayStorePublishingDialog(
            showDialog = showPlayStoreDialog,
            onDismiss = { showPlayStoreDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun ExportChecklistDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    checklistItems: List<ChecklistItem>,
    onExportSuccess: (String) -> Unit,
    onExportError: (String) -> Unit
) {
    if (!showDialog) return

    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ChecklistExporter.ExportFormat.PDF) }
    var onlyCompleted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("ייצוא רשימת מעבר", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "ייצא את רשימת משימות ההעברה והסטטוס שלהן כקובץ PDF מעוצב או כקובץ טקסט מובנה למעקב ותיעוד אישי.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "פורמט הקובץ:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFormat == ChecklistExporter.ExportFormat.PDF,
                        onClick = { selectedFormat = ChecklistExporter.ExportFormat.PDF },
                        label = { Text("מסמך PDF") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFormat == ChecklistExporter.ExportFormat.TEXT,
                        onClick = { selectedFormat = ChecklistExporter.ExportFormat.TEXT },
                        label = { Text("קובץ טקסט (.txt)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = onlyCompleted,
                        onCheckedChange = { onlyCompleted = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ייצא משימות שהושלמו בלבד", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val result = ChecklistExporter.exportChecklist(
                            context = context,
                            items = checklistItems,
                            format = selectedFormat,
                            onlyCompleted = onlyCompleted
                        )
                        if (result != null) {
                            ChecklistExporter.openExportedFile(context, result)
                            onExportSuccess("הקובץ נוצר בהצלחה ונפתח!")
                            onDismiss()
                        } else {
                            onExportError("שגיאה ביצירת הקובץ")
                        }
                    }
                ) {
                    Text("פתח קובץ")
                }
                Button(
                    onClick = {
                        val result = ChecklistExporter.exportChecklist(
                            context = context,
                            items = checklistItems,
                            format = selectedFormat,
                            onlyCompleted = onlyCompleted
                        )
                        if (result != null) {
                            ChecklistExporter.shareExportedFile(context, result)
                            onExportSuccess("הקובץ מוכן לשיתוף!")
                            onDismiss()
                        } else {
                            onExportError("שגיאה ביצירת הקובץ")
                        }
                    }
                ) {
                    Text("שתף קובץ")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}

@Composable
fun ChecklistRow(item: ChecklistItem, sortOption: String, onChecked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = { onChecked() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.instruction} • ${item.action}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sortOption == "SIZE" && item.size > 0) {
                    val formattedSize = if (item.size > 1024 * 1024) "${item.size / (1024 * 1024)} MB" else "${item.size / 1024} KB"
                    Text(
                        text = "גודל: $formattedSize",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (sortOption == "USAGE" && item.usageFrequency > 0) {
                    Text(
                        text = "שימוש: ${item.usageFrequency} פעמים",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            val sourceLabel = when (item.source) {
                CoverageSource.GOOGLE_BUILTIN -> "גוגל"
                CoverageSource.MANUFACTURER -> "מערכת"
                CoverageSource.AUTO_VERIFIED -> "מאומת"
                CoverageSource.MANUAL -> "ידני"
            }
            
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 8.dp),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = sourceLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CloudSyncCard(state: com.example.ui.ScanState, viewModel: ScanViewModel) {
    var syncInput by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state.syncCode != null) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "סמל ענן",
                        tint = if (state.syncCode != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "סנכרון ענן בין מכשירים",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (state.syncCode != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (state.syncCode != null) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "בשידור חי",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (state.syncCode == null) {
                Text(
                    text = "סנכרן את התקדמות רשימת המטלות שלך בין מכשירים מרובים בזמן אמת. הזן קוד סנכרון משותף למטה או צור קוד חדש כדי להתחיל.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = syncInput,
                        onValueChange = { syncInput = it.take(12).uppercase() },
                        placeholder = { Text("הזן קוד סנכרון", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            if (syncInput.isNotBlank()) {
                                viewModel.startCloudSync(syncInput)
                            }
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("התחבר", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        val randomCode = "MIG" + (100..999).random()
                        syncInput = randomCode
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("צור קוד חדש", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "המכשיר שלך מחובר להפעלת הענן. פתח אפליקציה זו במכשיר אחר והתחבר באמצעות אותו קוד סנכרון כדי לראות שינויים בהתקדמות באופן מיידי!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "קוד פעיל",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = state.syncCode,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    Button(
                        onClick = { viewModel.stopCloudSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("נתק חיבור", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (!state.isCloudAvailable) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⚠️ חיבור ל-Firestore נעקף. מצב ארגז חול מקומי פעיל. חבר קובץ google-services.json אמיתי כדי לסנכרן בין מכשירים פיזיים שונים.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun GoogleDriveBackupCard(state: com.example.ui.ScanState, viewModel: ScanViewModel) {
    val syncCode = state.syncCode
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "גיבוי לענן",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "גיבוי והעלאה לענן (Google Drive)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "העלה את המכשיר הישן במלואו לענן כדי לשחזר אותו ישירות במכשיר החדש",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (syncCode == null) {
                Text(
                    text = "אנא התחבר או צור קוד סנכרון תחילה כדי לגבות לענן.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                if (state.isBackingUp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.backupMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = { state.backupProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.performCloudBackup(syncCode) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("גבה מכשיר לענן עכשיו", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CloudRestoreCard(state: com.example.ui.ScanState, viewModel: ScanViewModel) {
    val context = LocalContext.current
    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    val adminComponent = remember { ComponentName(context, MyDeviceAdminReceiver::class.java) }
    val isAdminActive = dpm.isAdminActive(adminComponent)
    val syncCode = state.syncCode
    var showSuccessToast by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "שחזור מכשיר",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "התקנה ושחזור מלא במכשיר חדש",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "שכפל והעתק את כל נתוני המכשיר הישן למכשיר חדש זה",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (syncCode == null) {
                Text(
                    text = "אנא התחבר לקוד סנכרון של המכשיר הישן כדי למשוך את הגיבוי.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!isAdminActive) {
                Text(
                    text = "עליך להפעיל תחילה את הרשאת מנהל מכשיר למעלה כדי לאפשר שחזור מערכת.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                if (state.isRestoring) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.restoreMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = { state.restoreProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.performCloudRestore(syncCode) { success ->
                                if (success) {
                                    showSuccessToast = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("העתק ושחזר מכשיר ישן לחדש", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    
    if (showSuccessToast) {
        android.widget.Toast.makeText(context, "שחזור והעתקת המכשיר הושלמו בהצלחה!", android.widget.Toast.LENGTH_LONG).show()
        showSuccessToast = false
    }
}

@Composable
fun QuickToolChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(104.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}


