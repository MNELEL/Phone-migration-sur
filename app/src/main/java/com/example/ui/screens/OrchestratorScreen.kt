package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.MigrationOrchestrator
import com.example.service.OrchestratorStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrchestratorScreen(
    orchestrator: MigrationOrchestrator,
    onBack: () -> Unit,
    onNavigateToQrWizard: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val state by orchestrator.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("אשף מעבר מתוזמר", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "מנצח מעבר נתונים אקטיבי",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "זרימה מלאה: אריזה -> העלאה לענן -> התקנת אפליקציה במכשיר היעד -> שחזור",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Main Flow Progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "${(state.progress * 100).toInt()}% הושלמו",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                // Step Cards Overview
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrchestratorStepRow(
                        stepNumber = 1,
                        title = "אריזת הנתונים והצפנה",
                        subtitle = if (state.totalAppsPackaged > 0) "אורז ${state.totalAppsPackaged} אפליקציות, ${state.totalContactsPackaged} אנשי קשר ו-${state.totalMediaMBPackaged} MB מדיה" else "איסוף והצפנת AES-256 של הנתונים",
                        isCompleted = state.stage.ordinal > OrchestratorStage.PACKAGING_DATA.ordinal,
                        isActive = state.stage == OrchestratorStage.PACKAGING_DATA,
                        icon = Icons.Default.Lock
                    )

                    OrchestratorStepRow(
                        stepNumber = 2,
                        title = "העלאת גיבוי מוצפן לענן",
                        subtitle = "יעד אחסון: ${state.destinationProvider}",
                        isCompleted = state.stage.ordinal > OrchestratorStage.UPLOADING_CLOUD.ordinal,
                        isActive = state.stage == OrchestratorStage.UPLOADING_CLOUD,
                        icon = Icons.Default.CloudUpload
                    )

                    OrchestratorStepRow(
                        stepNumber = 3,
                        title = "התקנת האפליקציה במכשיר היעד",
                        subtitle = "אנא התקן את האפליקציה במכשיר החדש והשתמש בקוד: ${state.pairingCode}",
                        isCompleted = state.stage.ordinal > OrchestratorStage.PROMPTING_TARGET_INSTALL.ordinal,
                        isActive = state.stage == OrchestratorStage.PROMPTING_TARGET_INSTALL,
                        icon = Icons.Default.PhoneAndroid
                    )

                    OrchestratorStepRow(
                        stepNumber = 4,
                        title = "שחזור והגדרת המכשיר החדש",
                        subtitle = if (state.targetDeviceConnected) "הורדה ושחזור בתהליך..." else "ממתין לחיבור ממכשיר היעד",
                        isCompleted = state.stage == OrchestratorStage.COMPLETED,
                        isActive = state.stage == OrchestratorStage.RECOVERING_DATA,
                        icon = Icons.Default.Download
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                when (state.stage) {
                    OrchestratorStage.IDLE -> {
                        Button(
                            onClick = { orchestrator.startTransferFlow(scope) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("התחל תהליך מעבר מתוזמר עכשיו", fontWeight = FontWeight.Bold)
                        }
                    }

                    OrchestratorStage.PROMPTING_TARGET_INSTALL -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = onNavigateToQrWizard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("פתח אשף קוד QR לצימוד המכשיר החדש", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { orchestrator.simulateTargetConnection(scope) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("סימולציית חיבור ממכשיר היעד")
                            }
                        }
                    }

                    OrchestratorStage.COMPLETED -> {
                        Button(
                            onClick = onNavigateToReport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("צפה בדוח המעבר והסנכרון המלא", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        OutlinedButton(
                            onClick = { orchestrator.reset() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("איפוס תהליך")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OrchestratorStepRow(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val bgColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isActive -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val iconColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = iconColor)
                } else {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "שלב $stepNumber: $title",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
