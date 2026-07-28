package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrWizardScreen(
    onBack: () -> Unit,
    onPairingComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1: Role, 2: Display QR / Enter Code, 3: Connecting, 4: Success
    var isSourceDevice by remember { mutableStateOf(true) } // True: Old device, False: New device
    var generatedPairingCode by remember { mutableStateOf("QR-8942-SEC") }
    var enteredCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("אשף חיבור מכשירים בלייב", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Step Indicator Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WizardStepChip(step = 1, title = "תפקיד", activeStep = currentStep)
                    WizardStepChip(step = 2, title = "קוד QR", activeStep = currentStep)
                    WizardStepChip(step = 3, title = "אימות", activeStep = currentStep)
                    WizardStepChip(step = 4, title = "סיום", activeStep = currentStep)
                }

                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    label = "WizardStepTransition"
                ) { step ->
                    when (step) {
                        1 -> Step1RoleSelection(
                            isSourceDevice = isSourceDevice,
                            onRoleSelected = { isSourceDevice = it }
                        )
                        2 -> Step2QrDisplayOrScan(
                            isSourceDevice = isSourceDevice,
                            pairingCode = generatedPairingCode,
                            enteredCode = enteredCode,
                            onCodeChange = { enteredCode = it },
                            onRefreshCode = {
                                generatedPairingCode = "QR-" + (1000..9999).random() + "-SEC"
                            }
                        )
                        3 -> Step3Verifying(
                            isVerifying = isVerifying
                        )
                        4 -> Step4Success()
                    }
                }
            }

            // Bottom Navigation Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1 && currentStep < 4) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("חזור", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Button(
                    onClick = {
                        if (currentStep == 1) {
                            currentStep = 2
                        } else if (currentStep == 2) {
                            currentStep = 3
                            isVerifying = true
                        } else if (currentStep == 3) {
                            currentStep = 4
                        } else {
                            onPairingComplete()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(
                        text = when (currentStep) {
                            1 -> "המשך לשלב 2"
                            2 -> if (isSourceDevice) "פתח סורק / חבר" else "אמת קוד"
                            3 -> "אמת חיבור"
                            else -> "התחל מעבר נתונים"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Auto simulate verification on step 3
    LaunchedEffect(currentStep) {
        if (currentStep == 3) {
            isVerifying = true
            delay(2000)
            isVerifying = false
            currentStep = 4
        }
    }
}

@Composable
fun WizardStepChip(step: Int, title: String, activeStep: Int) {
    val isDone = step < activeStep
    val isActive = step == activeStep
    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isDone -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isActive -> MaterialTheme.colorScheme.onPrimary
        isDone -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Step1RoleSelection(
    isSourceDevice: Boolean,
    onRoleSelected: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "שלב 1: הגדרת תפקיד המכשיר",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "בחר האם מכשיר זה שולח את הנתונים (הישן) או מקבל אותם (החדש).",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    color = if (isSourceDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onRoleSelected(true) },
            colors = CardDefaults.cardColors(
                containerColor = if (isSourceDevice) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "מכשיר ישן",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("זהו המכשיר הישן (שולח)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("יוצר קוד QR מוצפן ומשדר את גיבוי המכשיר", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    color = if (!isSourceDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onRoleSelected(false) },
            colors = CardDefaults.cardColors(
                containerColor = if (!isSourceDevice) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "מכשיר חדש",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("זהו המכשיר החדש (מקבל)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("סורק את קוד ה-QR ומוריד את נתוני המעבר", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun Step2QrDisplayOrScan(
    isSourceDevice: Boolean,
    pairingCode: String,
    enteredCode: String,
    onCodeChange: (String) -> Unit,
    onRefreshCode: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isSourceDevice) "שלב 2: סרוק קוד QR ממכשיר זה" else "שלב 2: הכנס או סרוק קוד זיווג",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isSourceDevice) "פתח את המצלמה במכשיר החדש וסרוק את הברקוד המאובטח שלהלן:" else "כוון את מצלמת המכשיר או הקלד את קוד הזיווג המופיע במכשיר הישן:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        if (isSourceDevice) {
            // Render custom QR code canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CustomQrCodeCanvas(code = pairingCode, sizeDp = 200)

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = pairingCode,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                        IconButton(onClick = onRefreshCode) {
                            Icon(Icons.Default.Refresh, contentDescription = "רענן", tint = Color.Black)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "סורק QR",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "מצלמה סורקת קוד QR...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "או הקלד קוד זיווג ידני:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = onCodeChange,
                        placeholder = { Text("לדוגמה: QR-8942-SEC") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun Step3Verifying(isVerifying: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "מבצע אימות לחישת ידיים (Handshake) מוצפנת...",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "מאמת מפתחות הצפנה בין המכשיר הישן לחדש באמצעות ערוץ P2P מאובטח.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Step4Success() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "בהצלחה",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "חיבור וזיווג המכשירים בוצע בהצלחה!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "המכשירים מחוברים כעת בצימוד מאובטח. אתה מוכן להתחיל בהעברת הנתונים המוצפנת.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CustomQrCodeCanvas(code: String, sizeDp: Int) {
    val darkColor = Color.Black
    val lightColor = Color.White

    Canvas(
        modifier = Modifier
            .size(sizeDp.dp)
            .background(lightColor, RoundedCornerShape(12.dp))
            .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
    ) {
        val width = size.width
        val height = size.height
        val gridSize = 10
        val cellSize = width / gridSize

        // Draw position markers at corners
        drawRect(darkColor, topLeft = Offset(0f, 0f), size = Size(cellSize * 3, cellSize * 3))
        drawRect(lightColor, topLeft = Offset(cellSize, cellSize), size = Size(cellSize, cellSize))

        drawRect(darkColor, topLeft = Offset(cellSize * 7, 0f), size = Size(cellSize * 3, cellSize * 3))
        drawRect(lightColor, topLeft = Offset(cellSize * 8, cellSize), size = Size(cellSize, cellSize))

        drawRect(darkColor, topLeft = Offset(0f, cellSize * 7), size = Size(cellSize * 3, cellSize * 3))
        drawRect(lightColor, topLeft = Offset(cellSize, cellSize * 8), size = Size(cellSize, cellSize))

        // Draw pseudo random data blocks based on code hash
        val hash = code.hashCode()
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                // Skip corner markers
                if ((i < 3 && j < 3) || (i >= 7 && j < 3) || (i < 3 && j >= 7)) continue
                if ((hash xor (i * 31 + j * 17)) % 2 == 0) {
                    drawRect(
                        color = darkColor,
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = Size(cellSize * 0.9f, cellSize * 0.9f)
                    )
                }
            }
        }
    }
}
