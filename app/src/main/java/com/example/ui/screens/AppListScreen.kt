package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.service.AppCategorizationService
import com.example.service.AppFolderCategory
import com.example.service.AppFolderGroup
import com.example.service.AppQueryService
import com.example.service.UserAppInfo
import com.example.ui.components.ScanningLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appQueryService = remember { AppQueryService(context) }
    val categorizationService = remember { AppCategorizationService() }

    var isLoading by remember { mutableStateOf(true) }
    var appsList by remember { mutableStateOf<List<UserAppInfo>>(emptyList()) }
    var selectedCategoryFilter by remember { mutableStateOf<AppFolderCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var collapsedFolderIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        isLoading = true
        appsList = appQueryService.queryUserApplications()
        isLoading = false
    }

    val folderGroups = remember(appsList, searchQuery, selectedCategoryFilter) {
        val filteredApps = appsList.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || app.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }

        categorizationService.groupAppsByFolder(
            apps = filteredApps,
            getPackageName = { it.packageName },
            getAppName = { it.appName },
            getCategoryOverride = { it.category.id }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("אפליקציות לפי תיקיות נושא", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${appsList.size} אפליקציות מותקנות ב-${folderGroups.size} תיקיות",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLoading = true
                            appsList = emptyList()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "רענן")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                ScanningLoadingIndicator(
                    statusText = "מסווג אפליקציות לתיקיות נושא...",
                    subStatusText = "מנתח מאפייני חבילה וקטגוריות ליצירת תוכנית העברה נוחה"
                )
            } else if (appsList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("לא נמצאו אפליקציות משתמש מותקנות", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search & Filter Header Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("חפש אפליקציה או חבילה...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "ניקוי")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            // Category Filter Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryFilter == null,
                                        onClick = { selectedCategoryFilter = null },
                                        label = { Text("הכל (${appsList.size})", fontWeight = FontWeight.Bold) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                items(AppFolderCategory.values()) { category ->
                                    val countInFolder = appsList.count { it.category == category }
                                    if (countInFolder > 0) {
                                        val isSelected = selectedCategoryFilter == category
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedCategoryFilter = if (isSelected) null else category
                                            },
                                            label = {
                                                Text("${category.titleHebrew} ($countInFolder)")
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = category.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(category.accentColorHex)
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Categorized Folders List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(folderGroups, key = { it.category.id }) { group ->
                            val isCollapsed = collapsedFolderIds.contains(group.category.id)

                            AppFolderCard(
                                group = group,
                                isCollapsed = isCollapsed,
                                onToggleCollapse = {
                                    collapsedFolderIds = if (isCollapsed) {
                                        collapsedFolderIds - group.category.id
                                    } else {
                                        collapsedFolderIds + group.category.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppFolderCard(
    group: AppFolderGroup<UserAppInfo>,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    val category = group.category

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(category.accentColorHex).copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Folder Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCollapse() }
                    .background(Color(category.accentColorHex).copy(alpha = 0.08f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(category.accentColorHex).copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = Color(category.accentColorHex),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.titleHebrew,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = category.descriptionHebrew,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(category.accentColorHex),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "${group.count}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Icon(
                    imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (isCollapsed) "הרחב" else "כווץ",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded Folder Content
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.items.forEach { appInfo ->
                        UserAppFolderItemRow(app = appInfo, folderCategory = category)
                    }
                }
            }
        }
    }
}

@Composable
fun UserAppFolderItemRow(app: UserAppInfo, folderCategory: AppFolderCategory) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                val bitmap = remember(app.icon) { app.icon.toBitmap(48, 48).asImageBitmap() }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(folderCategory.accentColorHex)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (app.canBackup) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "מגובה",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
