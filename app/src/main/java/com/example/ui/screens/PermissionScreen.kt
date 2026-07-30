package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.OptIn
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(onContinue: () -> Unit) {
    val permissions = remember {
        buildList {
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                @Suppress("DEPRECATION")
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    var showExplanationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onContinue()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("הרשאות נדרשות להעברה", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(20.dp)
                            .size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "אפשרו גישה להשלמת העברת המכשיר",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "כדי ליצור רשימת מעבר מדויקת ולסכום את הקבצים, האפליקציה זקוקה להרשאות הבאות:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                PermissionExplanationItem(
                    icon = Icons.Default.Contacts,
                    title = "אנשי קשר (READ_CONTACTS)",
                    description = "ספירת אנשי הקשר ליצירת גיבוי ואימות מעבר לחשבון Google."
                )

                Spacer(Modifier.height(12.dp))

                PermissionExplanationItem(
                    icon = Icons.Default.FolderSpecial,
                    title = "קבצי מדיה ואחסון",
                    description = "חישוב נפח האחסון של תמונות וסרטונים לקראת הסנכרון לענן."
                )

                Spacer(Modifier.height(12.dp))

                PermissionExplanationItem(
                    icon = Icons.Default.CameraAlt,
                    title = "מצלמה (CAMERA)",
                    description = "סריקת קוד QR לצימוד בין המכשיר הישן למכשיר החדש."
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (permissionsState.shouldShowRationale) {
                            showExplanationDialog = true
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("אשר והמשך בסריקה", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onContinue) {
                    Text("המשך ללא הרשאות (מצב הדגמה)")
                }
            }
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = { Text("למה נדרשות ההרשאות?") },
            text = {
                Text("האפליקציה אינה משתפת את המידע האישי שלך עם צד שלישי. ההרשאות משמשות אך ורק לקריאת ספירת הנתונים במכשיר וצימוד QR למעבר חלק.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExplanationDialog = false
                        permissionsState.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("הבנתי, בקש הרשאות")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("ביטול")
                }
            }
        )
    }
}

@Composable
fun PermissionExplanationItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
