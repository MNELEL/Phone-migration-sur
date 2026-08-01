package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Storefront
import com.example.service.ProjectZipExporter
import com.example.service.ZipExportResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToDeviceAdmin: () -> Unit = {},
    onNavigateToLivePreview: () -> Unit = {}
) {
    var backupMode by remember { mutableStateOf("Google Drive") } // "Google Drive", "Dropbox", "Manual Encrypted"
    var isEncryptionEnabled by remember { mutableStateOf(true) }
    var autoSyncOnWifi by remember { mutableStateOf(true) }
    var includeMediaInBackup by remember { mutableStateOf(true) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var encryptionKey by remember { mutableStateOf("AES256-SECRET-KEY-984") }

    // Scheduled Upload State
    var isScheduledUploadEnabled by remember { mutableStateOf(true) }
    var scheduledTime by remember { mutableStateOf("02:00") } // Default 2 AM
    var requireCharging by remember { mutableStateOf(true) }
    var requireWifiOnly by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var isExportingZip by remember { mutableStateOf(false) }
    var zipExportResult by remember { mutableStateOf<ZipExportResult?>(null) }
    var showZipExportDialog by remember { mutableStateOf(false) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDataDeleteDialog by remember { mutableStateOf(false) }
    var dataDeleteSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showPlayStoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הגדרות מעבר וגיבוי", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "יעד הגיבוי ובחירת אחסון",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("בחר שיטת גיבוי מעבר מועדפת:", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = backupMode == "Google Drive",
                                onClick = { backupMode = "Google Drive" }
                            )
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Google Drive (סנכרון ענן אוטומטי)")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = backupMode == "Dropbox",
                                onClick = { backupMode = "Dropbox" }
                            )
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Dropbox (אחסון מוצפן מאובטח)")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = backupMode == "Manual Encrypted",
                                onClick = { backupMode = "Manual Encrypted" }
                            )
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(8.dp))
                            Text("גיבוי מקומי מוצפן ידני (.enc)")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "הצפנה ואבטחת מידע",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("הצפנת קבצי מעבר ברמת AES-256", fontWeight = FontWeight.Bold)
                                    Text("הצפן תמונות ואנשי קשר לפני העלאה לענן", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isEncryptionEnabled,
                                onCheckedChange = { isEncryptionEnabled = it }
                            )
                        }

                        if (isEncryptionEnabled) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showPasswordDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("הגדר מפתח הצפנה אישי")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "תזמון העלאה לענן בלילה",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("הפעל העלאה לענן מתוזמנת", fontWeight = FontWeight.Bold)
                                    Text("העלאת קבצים אוטומטית כשהמכשיר במנוחה בלילה", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isScheduledUploadEnabled,
                                onCheckedChange = { isScheduledUploadEnabled = it }
                            )
                        }

                        if (isScheduledUploadEnabled) {
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("שעת העלאה מתוזמנת:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = scheduledTime == "02:00",
                                        onClick = { scheduledTime = "02:00" },
                                        label = { Text("02:00 בלילה") }
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    FilterChip(
                                        selected = scheduledTime == "04:00",
                                        onClick = { scheduledTime = "04:00" },
                                        label = { Text("04:00 לפנות בוקר") }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("העלה רק כשהמכשיר בטעינה", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = requireCharging,
                                    onCheckedChange = { requireCharging = it }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("העלה רק בחיבור Wi-Fi פעיל", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = requireWifiOnly,
                                    onCheckedChange = { requireWifiOnly = it }
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
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
                                        text = "העלאה לענן מתוזמנת להיום ב-$scheduledTime (מותנה בטעינה ו-Wi-Fi)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "הרשאות מתקדמות ושיבוט מכשיר",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("מרכז ניהול מכשיר", fontWeight = FontWeight.Bold)
                                Text("התקנה שקטה של אפליקציות, סנכרון הרשאות ושיבוט מלא", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToDeviceAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("פתח מנהל מכשיר ומשכפל אפליקציות", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onNavigateToLivePreview,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("פתח תצוגה מקדימה בזמן אמת (Live UI Preview)")
                        }

                        Spacer(Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = {
                                isExportingZip = true
                                val result = ProjectZipExporter.exportProjectZip(context)
                                zipExportResult = result
                                isExportingZip = false
                                showZipExportDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("ייצא קובצי פרויקט וקונפיגורציות ל-ZIP (Android Studio)")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "הגדרות רשת וסנכרון",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("סנכרן והעלה רק בחיבור Wi-Fi", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = autoSyncOnWifi,
                                onCheckedChange = { autoSyncOnWifi = it }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("כלול תמונות וסרטונים בגיבוי הראשוני", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = includeMediaInBackup,
                                onCheckedChange = { includeMediaInBackup = it }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "פרטיות ומחיקת נתונים (Google Play Policy)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { showPrivacyDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("צפה במדיניות הפרטיות (Privacy Policy)")
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showDataDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("מחק את כל נתוני המעבר והגיבויים שלי")
                        }

                        dataDeleteSuccessMessage?.let { msg ->
                            Spacer(Modifier.height(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "צ'ק ליסט לפרסום ב-Google Play",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "מדריך שלב-אחר-שלב לדרישות חתימה, מניפסט, כיווץ AAB ופרסום לחנות",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showPlayStoreDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text("פתח מדריך פרסום ל-Google Play", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    PlayStorePublishingDialog(
        showDialog = showPlayStoreDialog,
        onDismiss = { showPlayStoreDialog = false }
    )

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("מדיניות פרטיות והגנת מידע") },
            text = {
                Column {
                    Text(
                        "אפליקציית Smart Device Migration מקפידה על הגנה מלאה של המידע האישי שלך לפי הנחיות Google Play:\n\n" +
                        "1. הנתונים (אנשי קשר, מדיה, אפליקציות) מועברים בצורה מוצפנת בלבד (AES-256 / TLS 1.3).\n" +
                        "2. לא נעשה שום שימוש במידע למטרות פרסום או שיתוף עם צד ג'.\n" +
                        "3. באפשרותך למחוק את כל המידע שנסרק ונשמר בכל עת דרך כפתור 'מחיקת נתונים'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("אישור והבנתי")
                }
            }
        )
    }

    if (showDataDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDataDeleteDialog = false },
            title = { Text("מחיקת כל הנתונים") },
            text = {
                Text("האם אתה בטוח שברצונך למחוק את כל נתוני הגיבוי והסנכרון המקומיים והענניים? פעולה זו הינה לצמיתות.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDataDeleteDialog = false
                        dataDeleteSuccessMessage = "כל נתוני הגיבוי והמעבר נמחקו בהצלחה מהמכשיר והענן."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("מחק לצמיתות")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDeleteDialog = false }) {
                    Text("ביטול")
                }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text("מפתח הצפנה אישי") },
            text = {
                Column {
                    Text("הזן מפתח הצפנה לפענוח קבצי הגיבוי במכשיר החדש:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = encryptionKey,
                        onValueChange = { encryptionKey = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPasswordDialog = false }) {
                    Text("שמור מפתח")
                }
            }
        )
    }

    if (showZipExportDialog && zipExportResult != null) {
        val result = zipExportResult!!
        AlertDialog(
            onDismissRequest = { showZipExportDialog = false },
            icon = { Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("ייצוא פרויקט ZIP ל-Android Studio הושלם!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("נוצר קובץ ארכיון מלא המכיל את כל קובצי הקונפיגורציה, Gradle build files, Manifest, Resources וקוד המקור:")
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("שם הקובץ: ${result.zipFile.name}", fontWeight = FontWeight.Bold)
                            Text("גודל הקובץ: ${result.formattedSize}")
                            Text("מספר קבצים ב-ZIP: ${result.fileCount}")
                            Text("נתיב שמירה: ${result.zipFile.parent}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ProjectZipExporter.shareZipFile(context, result.zipFile)
                    }
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("שתף / שמור קובץ ZIP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showZipExportDialog = false }) {
                    Text("סגור")
                }
            }
        )
    }
}
