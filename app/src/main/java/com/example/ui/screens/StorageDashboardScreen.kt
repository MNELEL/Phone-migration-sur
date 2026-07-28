package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDashboardScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onNavigateToOrchestrator: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Calculate Storage Breakdown
    val appsMB = 14200L // ~14.2 GB installed apps
    val mediaMB = 28500L // ~28.5 GB Photos & Videos
    val dataMB = 2100L   // ~2.1 GB Contacts, System & App Caches
    val totalMB = appsMB + mediaMB + dataMB

    val appsPct = (appsMB.toFloat() / totalMB)
    val mediaPct = (mediaMB.toFloat() / totalMB)
    val dataPct = (dataMB.toFloat() / totalMB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("לוח בקרה: התפלגות אחסון", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                // Storage Overview Card with Donut Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "התפלגות נפח האחסון להעברה",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "סך הכל ${String.format("%.1f", totalMB / 1024f)} GB במכשיר זה",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

                        // Custom Donut Chart Canvas
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(180.dp)
                        ) {
                            StorageDonutChart(
                                appsPct = appsPct,
                                mediaPct = mediaPct,
                                dataPct = dataPct
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format("%.1f", totalMB / 1024f)} GB",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "להעברה",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Chart Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LegendItem(color = MaterialTheme.colorScheme.primary, label = "אפליקציות", value = "${String.format("%.1f", appsMB / 1024f)} GB (${(appsPct * 100).toInt()}%)")
                            LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "תמונות ומדיה", value = "${String.format("%.1f", mediaMB / 1024f)} GB (${(mediaPct * 100).toInt()}%)")
                            LegendItem(color = MaterialTheme.colorScheme.secondary, label = "אנשי קשר ונתונים", value = "${String.format("%.1f", dataMB / 1024f)} GB (${(dataPct * 100).toInt()}%)")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "המלצות העברה: מה כדאי להעביר קודם?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                RecommendationCard(
                    title = "1. תמונות וסרטונים (מדיה כבדה)",
                    subtitle = "מדיה מהווה ${(mediaPct * 100).toInt()}% מסך האחסון (${String.format("%.1f", mediaMB / 1024f)} GB). מומלץ להתחיל בהעלאת המדיה לענן תחילה.",
                    icon = Icons.Default.Image,
                    badgeText = "עדיפות ראשונה",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            item {
                RecommendationCard(
                    title = "2. אפליקציות חיוניות (Essential)",
                    subtitle = "אפליקציות בנקים, אבטחה וטלפוניה שוקלות כ-${String.format("%.1f", appsMB / 1024f)} GB. סנכרן אותן ראשונות לזמינות מיידית.",
                    icon = Icons.Default.Apps,
                    badgeText = "עדיפות גבוהה",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                RecommendationCard(
                    title = "3. אנשי קשר ונתוני מערכת",
                    subtitle = "אנשי הקשר והגדרות המכשיר שוקלים מעט (${String.format("%.1f", dataMB / 1024f)} GB) ומתעדכנים תוך שניות ספורות בענן.",
                    icon = Icons.Default.ContactPage,
                    badgeText = "מהיר מאוד",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToOrchestrator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("התחל אשף מעבר מתוזמר (Orchestrator)", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StorageDonutChart(
    appsPct: Float,
    mediaPct: Float,
    dataPct: Float
) {
    val appsColor = MaterialTheme.colorScheme.primary
    val mediaColor = MaterialTheme.colorScheme.tertiary
    val dataColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 28.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f

        // Draw Apps arc
        val appsSweep = appsPct * 360f
        drawArc(
            color = appsColor,
            startAngle = startAngle,
            sweepAngle = appsSweep - 4f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += appsSweep

        // Draw Media arc
        val mediaSweep = mediaPct * 360f
        drawArc(
            color = mediaColor,
            startAngle = startAngle,
            sweepAngle = mediaSweep - 4f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += mediaSweep

        // Draw Data arc
        val dataSweep = dataPct * 360f
        drawArc(
            color = dataColor,
            startAngle = startAngle,
            sweepAngle = dataSweep - 4f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RecommendationCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
