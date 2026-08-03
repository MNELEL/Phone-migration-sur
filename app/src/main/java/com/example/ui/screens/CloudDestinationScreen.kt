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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.GoogleAuthService
import com.example.service.GoogleSignInResult
import kotlinx.coroutines.launch

/**
 * Web Client ID for Google Sign-In, from the nathan-migration Firebase project
 * (Authentication -> Sign-in method -> Google -> Web SDK configuration).
 */
private const val GOOGLE_WEB_CLIENT_ID = "19370589231-jmnue14soiqhi14kk7ksbr8iue6naiil.apps.googleusercontent.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudDestinationScreen(
    onBack: () -> Unit,
    onContinue: (selectedDestination: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authService = remember { GoogleAuthService(context) }

    var selectedDestination by remember { mutableStateOf("Google Drive") }
    var account by remember { mutableStateOf(authService.currentAccount()) }
    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

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
                    text = "בחר את יעד הענן עבור סנכרון התקדמות המעבר",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "מסנכרן מטא-דאטה בלבד (שמות אפליקציות, כמויות, סטטוס השלמה) — לא מעלה קבצים אישיים כמו תמונות או אנשי קשר בפועל.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                CloudStorageCard(
                    title = "Google Drive",
                    description = "התחברות אמיתית באמצעות חשבון Google (Firebase Auth)",
                    icon = Icons.Default.Cloud,
                    isConnected = account != null,
                    isSelected = selectedDestination == "Google Drive",
                    statusText = when {
                        isSigningIn -> "מתחבר..."
                        account != null -> account?.displayName ?: account?.email ?: ""
                        else -> "לא מחובר"
                    },
                    accountEmail = account?.email,
                    onSelect = { selectedDestination = "Google Drive" },
                    onConnectToggle = {
                        if (account != null) {
                            authService.signOut()
                            account = null
                        } else {
                            signInError = null
                            isSigningIn = true
                            scope.launch {
                                when (val result = authService.signIn(GOOGLE_WEB_CLIENT_ID)) {
                                    is GoogleSignInResult.Success -> {
                                        account = result.account
                                    }
                                    is GoogleSignInResult.Failure -> {
                                        signInError = result.message
                                    }
                                }
                                isSigningIn = false
                            }
                        }
                    }
                )

                if (signInError != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = signInError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                CloudStorageCard(
                    title = "גיבוי מקומי (JSON)",
                    description = "שמירת קובץ סטטוס מעבר בזיכרון המכשיר להעברה ידנית",
                    icon = Icons.Default.Folder,
                    isConnected = true,
                    isSelected = selectedDestination == "Local",
                    statusText = "זמין תמיד, ללא צורך בחיבור",
                    accountEmail = null,
                    onSelect = { selectedDestination = "Local" },
                    onConnectToggle = {}
                )
            }

            Button(
                onClick = { onContinue(selectedDestination) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedDestination != "Google Drive" || account != null
            ) {
                Text(
                    text = "אשר והמשך בבחירה ($selectedDestination)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CloudStorageCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isConnected: Boolean,
    isSelected: Boolean,
    statusText: String,
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
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
