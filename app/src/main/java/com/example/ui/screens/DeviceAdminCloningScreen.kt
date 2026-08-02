package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.MyDeviceAdminReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ClonedAppItem(
    val name: String,
    val packageName: String,
    val sizeMB: Int,
    val category: String,
    var status: AppInstallStatus = AppInstallStatus.QUEUED,
    var progress: Float = 0f
)

enum class AppInstallStatus {
    QUEUED,
    DOWNLOADING,
    SILENT_INSTALLING,
    PERMISSIONS_GRANTED,
    COMPLETED,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAdminCloningScreen(
    onBack: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Device Admin status
    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    val adminComponent = remember { ComponentName(context, MyDeviceAdminReceiver::class.java) }
    var isAdminActive by remember { mutableStateOf(dpm.isAdminActive(adminComponent)) }

    // Cloning execution state
    var isCloningActive by remember { mutableStateOf(false) }
    var cloningProgress by remember { mutableFloatStateOf(0f) }
    var currentCloningStage by remember { mutableStateOf("ממתין לתחילת שיבוט האפליקציות") }

    // App queue
    var appList by remember {
        mutableStateOf(
            listOf(
                ClonedAppItem("WhatsApp Messenger", "com.whatsapp", 85, "ESSENTIAL"),
                ClonedAppItem("Google Chrome Browser", "com.android.chrome", 120, "PRODUCTIVITY"),
                ClonedAppItem("לאומי Leumi Banking", "com.leumi.mobile", 95, "ESSENTIAL"),
                ClonedAppItem("Waze Navigation", "com.waze", 110, "PRODUCTIVITY"),
                ClonedAppItem("Instagram", "com.instagram.android", 140, "SOCIAL"),
                ClonedAppItem("Gmail", "com.google.android.gm", 65, "PRODUCTIVITY"),
                ClonedAppItem("Spotify Music", "com.spotify.music", 78, "ENTERTAINMENT"),
                ClonedAppItem("Candy Crush Saga", "com.king.candycrushsaga", 160, "GAMES"),
                ClonedAppItem("Telegram Messenger", "org.telegram.messenger", 92, "ESSENTIAL"),
                ClonedAppItem("Moovit Public Transport", "com.tranzmate", 54, "PRODUCTIVITY")
            )
        )
    }

    // Function to launch device admin activation intent
    fun requestDeviceAdmin() {
        try {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "הרשאת מנהל מכשיר נדרשת לצורך התקנה אוטומטית שקטה של אפליקציות, סנכרון הרשאות ושיבוט מלא של המכשיר הישן למכשיר החדש."
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "שגיאה בפתיחת מסך מנהל מכשיר: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to run bulk app installation simulation
    fun startDeviceAdminCloning() {
        if (!isCloningActive) {
            isCloningActive = true
            scope.launch {
                val totalApps = appList.size
                appList.forEachIndexed { index, app ->
                    // Stage 1: Downloading
                    currentCloningStage = "מוריד קובץ התקנה עבור ${app.name}..."
                    appList = appList.mapIndexed { i, item ->
                        if (i == index) item.copy(status = AppInstallStatus.DOWNLOADING, progress = 0.3f) else item
                    }
                    delay(600)

                    // Stage 2: Device Admin Silent Installation
                    currentCloningStage = "מנהל המכשיר מתקין התקנה שקטה (Silent Install) עבור ${app.name}..."
                    appList = appList.mapIndexed { i, item ->
                        if (i == index) item.copy(status = AppInstallStatus.SILENT_INSTALLING, progress = 0.7f) else item
                    }
                    delay(700)

                    // Stage 3: Auto Grant Permissions
                    currentCloningStage = "מעניק הרשאות מערכת אוטומטיות ל-${app.name}..."
                    appList = appList.mapIndexed { i, item ->
                        if (i == index) item.copy(status = AppInstallStatus.PERMISSIONS_GRANTED, progress = 0.9f) else item
                    }
                    delay(500)

                    // Stage 4: Completed
                    appList = appList.mapIndexed { i, item ->
                        if (i == index) item.copy(status = AppInstallStatus.COMPLETED, progress = 1.0f) else item
                    }

                    cloningProgress = (index + 1).toFloat() / totalApps
                }

                currentCloningStage = "שיבוט כל 10 האפליקציות והרשאותיהן הושלם בהצלחה!"
                isCloningActive = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("מנהל מכשיר ומשכפל אפליקציות", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(onClick = { isAdminActive = dpm.isAdminActive(adminComponent) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "רענן סטטוס")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                // Device Admin Status Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAdminActive)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isAdminActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAdminActive) "הרשאת מנהל מכשיר פעילה" else "הרשאת מנהל מכשיר כבויה",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAdminActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = if (isAdminActive) "האפליקציה מורשית להתקין אפליקציות ברקע, לשכפל הרשאות ולנהל אבטחה" else "יש להפעיל הרשאת מנהל מכשיר כדי לבצע התקנה אוטומטית שקטה ושיבוט מלא",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (!isAdminActive) {
                            Button(
                                onClick = { requestDeviceAdmin() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("הפעל הרשאת מנהל מכשיר כעת", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "כל סמכויות מנהל המכשיר זמינות לשיבוט המכשיר!",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Device Admin Capabilities Matrix
            item {
                Text(
                    text = "יכולות מנהל מכשיר לשיבוט מתקדם",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminCapabilityRow(
                        title = "1. התקנת אפליקציות שקטה ברקע",
                        description = "התקנת כל האפליקציות ברצף ללא צורך בלחיצה ידנית של המשתמש על כל קובץ התקנה",
                        icon = Icons.Default.InstallMobile,
                        enabled = isAdminActive
                    )
                    AdminCapabilityRow(
                        title = "2. מתן הרשאות מערכת אוטומטי",
                        description = "העתקת הרשאות המצלמה, המיקום והאחסון ישירות מהמכשיר הישן לחדש",
                        icon = Icons.Default.SystemUpdate,
                        enabled = isAdminActive
                    )
                    AdminCapabilityRow(
                        title = "3. שיבוט פרופיל והגדרות אבטחה",
                        description = "סנכרון מדיניות סיסמאות, נעילת מסך, מצב כהה ופרופילי עבודה מאובטחים",
                        icon = Icons.Default.Security,
                        enabled = isAdminActive
                    )
                }
            }

            // App Cloning Progress Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "מנוע שיבוט והתקנה אוטומטית",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${(cloningProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { cloningProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = currentCloningStage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { startDeviceAdminCloning() },
                            enabled = !isCloningActive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CopyAll, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isCloningActive) "שיבוט אוטומטי בתהליך..." else "הפעל מנוע שיבוט והתקנה שקטה",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "תור אפליקציות להתקנה וסנכרון במכשיר החדש",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(appList) { app ->
                ClonedAppRow(app = app)
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("צפה בדוח סיוום וסנכרון מלא", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AdminCapabilityRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean
) {
    val tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ClonedAppRow(app: ClonedAppItem) {
    val statusText = when (app.status) {
        AppInstallStatus.QUEUED -> "בתור להתקנה"
        AppInstallStatus.DOWNLOADING -> "מוריד APK..."
        AppInstallStatus.SILENT_INSTALLING -> "התקנה שקטה במנהל מכשיר..."
        AppInstallStatus.PERMISSIONS_GRANTED -> "מעניק הרשאות אוטומטיות..."
        AppInstallStatus.COMPLETED -> "הותקן ושוכפל בהצלחה!"
        AppInstallStatus.FAILED -> "נכשל"
    }

    val statusColor = when (app.status) {
        AppInstallStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        AppInstallStatus.QUEUED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (app.status == AppInstallStatus.COMPLETED) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null, tint = statusColor)
                } else {
                    Icon(Icons.Default.Android, contentDescription = null, tint = statusColor)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(app.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(8.dp))
                    Text("${app.sizeMB} MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
