package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.service.AppQueryService
import com.example.service.UserAppInfo
import com.example.ui.components.ScanningLoadingIndicator

/**
 * Opens the app's Play Store listing so the user can install it themselves
 * on the new device. There is no API on stock Android that lets a normal
 * app silently install other apps in the background — the only legitimate
 * path is handing the user a real install link and letting them tap
 * "Install" like they would for any app.
 */
private fun openPlayStoreListing(context: Context, packageName: String) {
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    try {
        context.startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        context.startActivity(webIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appQueryService = remember { AppQueryService(context) }

    var isLoading by remember { mutableStateOf(true) }
    var appsList by remember { mutableStateOf<List<UserAppInfo>>(emptyList()) }
    // Packages the user has manually marked as "already installed on the new
    // device". This is just a local checklist — nothing is installed for them.
    var installedOnNewDevice by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        isLoading = true
        appsList = appQueryService.queryUserApplications()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("אפליקציות מותקנות (${appsList.size})", fontWeight = FontWeight.Bold) },
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
                    statusText = "טוען אפליקציות מותקנות...",
                    subStatusText = "סורק את PackageManager ומסנן אפליקציות מערכת"
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "רשימה זו מכילה אפליקציות שהותקנו ע\"י המשתמש בלבד (ללא אפליקציות מערכת). לחץ \"פתח בחנות\" על כל אפליקציה במכשיר החדש כדי להתקין אותה משם — Android אינו מאפשר לאפליקציה זו להתקין אפליקציות אחרות עבורך.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    items(appsList, key = { it.packageName }) { appInfo ->
                        UserAppItemCard(
                            app = appInfo,
                            isMarkedInstalled = appInfo.packageName in installedOnNewDevice,
                            onOpenStore = { openPlayStoreListing(context, appInfo.packageName) },
                            onToggleInstalled = {
                                installedOnNewDevice = if (appInfo.packageName in installedOnNewDevice) {
                                    installedOnNewDevice - appInfo.packageName
                                } else {
                                    installedOnNewDevice + appInfo.packageName
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserAppItemCard(
    app: UserAppInfo,
    isMarkedInstalled: Boolean,
    onOpenStore: () -> Unit,
    onToggleInstalled: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMarkedInstalled)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app.icon != null) {
                    val bitmap = remember(app.icon) { app.icon.toBitmap(56, 56).asImageBitmap() }
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.appName,
                        modifier = Modifier.size(44.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (app.versionName.isNotEmpty()) {
                        Text(
                            text = "גרסה: ${app.versionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (app.canBackup) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "ניתן לגיבוי",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenStore,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("פתח בחנות", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onToggleInstalled,
                    modifier = Modifier.weight(1f),
                    colors = if (isMarkedInstalled) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    if (isMarkedInstalled) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("הותקן", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Text("סמן כהותקן", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
