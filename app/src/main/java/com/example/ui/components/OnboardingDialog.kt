package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun OnboardingDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPermissionsGranted: () -> Unit
) {
    if (!showDialog) return

    val permissionsToRequest = mutableListOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.GET_ACCOUNTS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onPermissionsGranted()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "כיצד הסריקה עובדת",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "כדי להפוך את המעבר למכשיר החדש שלך לקל וללא מאמץ, אנו מבצעים ניתוח מהיר ומאובטח של הטלפון. הנה מה שחשוב לדעת:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Card 1: Local & Private
                    OnboardingInfoCard(
                        icon = Icons.Default.Lock,
                        title = "100% פרטי ומקומי",
                        description = "הקבצים, המדיה ואנשי הקשר שלך לעולם אינם עוזבים את המכשיר. הניתוח מתבצע באופן מקומי ולא מקוון לחלוטין.",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Card 2: Intended Smart Flow
                    OnboardingInfoCard(
                        icon = Icons.Default.Info,
                        title = "בדיקת סנכרון עם גוגל",
                        description = "אנו מנתחים את האפליקציות שלך אל מול הגיבוי של גוגל ומגבלות הסנכרון האוטומטי, ומראים לך בדיוק מה בטוח לפרמוט.",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Card 3: Permissions needed
                    OnboardingInfoCard(
                        icon = Icons.Default.CheckCircle,
                        title = "הרשאות נדרשות",
                        description = "• אנשי קשר: לאימות סנכרון אנשי קשר.\n• מדיה: לקטלוג תמונות וסרטונים.\n• חשבונות: לבדיקת פרופיל הגיבוי.",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ביטול", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(permissionsToRequest)
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("אישור והמשך", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (contentColor == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
