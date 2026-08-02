package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DataSafetyDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PrivacyTip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "גילוי נאות ובטיחות נתונים (Data Safety)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "אפליקציה זו מותאמת למדיניות הפרטיות ובטיחות הנתונים של Google Play.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "אילו נתונים נסרקים ואיך נעשה בהם שימוש?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    DataSafetyItem(
                        title = "אפליקציות מותקנות (Installed Apps)",
                        description = "האפליקציה סורקת שמות חבילה (Package Names) של אפליקציות מותקנות בלבד כדי לקטלג אותן מחדש וליצור רשימת מעבר.",
                        icon = Icons.Default.CheckCircle,
                        tag = "שימוש מקומי + סנכרון רשימה"
                    )
                }

                item {
                    DataSafetyItem(
                        title = "אנשי קשר (Contacts Count)",
                        description = "נסרק מספר אנשי הקשר הכולל לצורך אימות גיבוי Google Account. תוכן אנשי הקשר אינו מועבר לצד שלישי.",
                        icon = Icons.Default.CheckCircle,
                        tag = "ספירה בלבד"
                    )
                }

                item {
                    DataSafetyItem(
                        title = "תמונות וסרטונים (Media Metadata)",
                        description = "נסרקים גודל הקבצים והספירה הכוללת בלבד לחישוב נפח אחסון. תוכן התמונות והסרטונים עצמם אינו נשמר או מועלה על ידי אפליקציה זו בשום שלב.",
                        icon = Icons.Default.CheckCircle,
                        tag = "מטא-דאטה בלבד"
                    )
                }

                item {
                    DataSafetyItem(
                        title = "הצפנה ואבטחה (TLS)",
                        description = "מטא-דאטה וסטטוס השלמה המסונכרנים דרך Firestore מוגנים ב-TLS בזמן ההעברה, לפי תשתית האבטחה הסטנדרטית של Firebase. אין הצפנה נוספת ברמת האפליקציה כרגע.",
                        icon = Icons.Default.Lock,
                        tag = "TLS בזמן העברה"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("הבנתי, אישור")
            }
        }
    )
}

@Composable
fun DataSafetyItem(
    title: String,
    description: String,
    icon: ImageVector,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
