package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudDestinationScreen(
    onBack: () -> Unit,
    onContinue: (selectedDestination: String) -> Unit
) {
    var selectedDestination by remember { mutableStateOf("Google Drive") }
    var isGoogleConnected by remember { mutableStateOf(true) }
    var isDropboxConnected by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("user.migration@gmail.com") }
    var showAuthModal by remember { mutableStateOf(false) }
    var authenticatingProvider by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("יעד אחסון בענן", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "בחר את יעד הענן עבור הגיבוי והמעבר",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "קובצי המעבר והנתונים יוצפנו בהצפנת AES-256 ויישמרו באחסון המאובטח שתבחר.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // Google Drive Card
                CloudStorageCard(
                    title = "Google Drive",
                    description = "אחסון ענן רשמי של גוגל - חיבור מהיר באמצעות חשבון Google",
                    icon = Icons.Default.Cloud,
                    isConnected = isGoogleConnected,
                    isSelected = selectedDestination == "Google Drive",
                    quotaText = "12.4 GB מתוך 15 GB בשימוש",
                    accountEmail = if (isGoogleConnected) userEmail else null,
                    onSelect = { selectedDestination = "Google Drive" },
                    onConnectToggle = {
                        if (isGoogleConnected) {
                            isGoogleConnected = false
                        } else {
                            authenticatingProvider = "Google Drive"
                            showAuthModal = true
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Dropbox Card
                CloudStorageCard(
                    title = "Dropbox",
                    description = "אחסון מוצפן בדרגת ארגון באמצעות חשבון Dropbox",
                    icon = Icons.Default.CloudQueue,
                    isConnected = isDropboxConnected,
                    isSelected = selectedDestination == "Dropbox",
                    quotaText = if (isDropboxConnected) "1.2 GB מתוך 2.0 GB בשימוש" else "לא מחובר",
                    accountEmail = if (isDropboxConnected) "user.migration@dropbox.com" else null,
                    onSelect = { selectedDestination = "Dropbox" },
                    onConnectToggle = {
                        if (isDropboxConnected) {
                            isDropboxConnected = false
                        } else {
                            authenticatingProvider = "Dropbox"
                            showAuthModal = true
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Local Encrypted Card
                CloudStorageCard(
                    title = "גיבוי מקומי מוצפן (.enc)",
                    description = "שמירת קובץ גיבוי מוצפן בזיכרון המכשיר להעברה ידנית ב-USB / כרטיס זיכרון",
                    icon = Icons.Default.Folder,
                    isConnected = true,
                    isSelected = selectedDestination == "Local Encrypted",
                    quotaText = "זיכרון פנוי במכשיר: 45.8 GB",
                    accountEmail = "אחסון מקומי מוצפן",
                    onSelect = { selectedDestination = "Local Encrypted" },
                    onConnectToggle = {}
                )
            }

            Button(
                onClick = { onContinue(selectedDestination) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "אשר והמשך בבחירה ($selectedDestination)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAuthModal) {
        AlertDialog(
            onDismissRequest = { showAuthModal = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = "אימות") },
            title = { Text("אימות חשבון $authenticatingProvider") },
            text = {
                Column {
                    Text("הזן כתובת אימייל לאימות וסנכרון ענן מאובטח:")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text("כתובת אימייל") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (authenticatingProvider == "Google Drive") isGoogleConnected = true
                    if (authenticatingProvider == "Dropbox") isDropboxConnected = true
                    showAuthModal = false
                }) {
                    Text("אשר חיבור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthModal = false }) {
                    Text("ביטול")
                }
            }
        )
    }
}

@Composable
fun CloudStorageCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isConnected: Boolean,
    isSelected: Boolean,
    quotaText: String,
    accountEmail: String?,
    onSelect: () -> Unit,
    onConnectToggle: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "נבחר",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (accountEmail != null) {
                        Text(
                            text = accountEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(onClick = onConnectToggle) {
                    Text(if (isConnected) "מחובר" else "חבר חשבון")
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = quotaText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
