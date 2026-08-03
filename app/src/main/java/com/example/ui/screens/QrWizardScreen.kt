package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scanner.QrCodeAnalyzer
import com.example.scanner.QrCodeGenerator
import com.example.ui.ScanViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrWizardScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onPairingComplete: () -> Unit
) {
    val scanState by viewModel.state.collectAsState()
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(1) } // 1: Role, 2: Display QR / Scan, 3: Connecting, 4: Success
    var isSourceDevice by remember { mutableStateOf(true) }
    var enteredCode by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    // The source device needs a real sync code to encode into the QR. Reuse
    // one already in progress (e.g. set from ChecklistScreen), or generate a
    // fresh one — this is the same code format used elsewhere in the app.
    val sourceSyncCode = scanState.syncCode ?: remember { "MIG" + (100..999).random() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("אשף חיבור מכשירים", fontWeight = FontWeight.Bold) },
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WizardStepChip(step = 1, title = "תפקיד", activeStep = currentStep)
                    WizardStepChip(step = 2, title = "קוד QR", activeStep = currentStep)
                    WizardStepChip(step = 3, title = "חיבור", activeStep = currentStep)
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
                            pairingCode = sourceSyncCode,
                            enteredCode = enteredCode,
                            onCodeChange = { enteredCode = it; scanError = null },
                            onCodeScanned = { scanned ->
                                enteredCode = scanned
                                scanError = null
                            },
                            scanError = scanError
                        )
                        3 -> Step3Verifying(
                            isSourceDevice = isSourceDevice,
                            isConnected = scanState.isSyncing && scanState.isCloudAvailable,
                            isCloudAvailable = scanState.isCloudAvailable
                        )
                        4 -> Step4Success()
                    }
                }
            }

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

                val canProceedStep2 = if (isSourceDevice) true else enteredCode.isNotBlank()

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> currentStep = 2
                            2 -> {
                                val code = if (isSourceDevice) sourceSyncCode else enteredCode.trim().uppercase()
                                if (code.isBlank()) {
                                    scanError = "יש לסרוק או להזין קוד סנכרון"
                                } else if (!scanState.isCloudAvailable) {
                                    scanError = "שירות הענן אינו זמין כרגע"
                                } else {
                                    viewModel.startCloudSync(code)
                                    currentStep = 3
                                }
                            }
                            3 -> if (scanState.isSyncing) currentStep = 4
                            else -> onPairingComplete()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = when (currentStep) {
                        2 -> canProceedStep2
                        3 -> scanState.isSyncing
                        else -> true
                    }
                ) {
                    Text(
                        text = when (currentStep) {
                            1 -> "המשך לשלב 2"
                            2 -> if (isSourceDevice) "המשך" else "אמת קוד"
                            3 -> "המשך"
                            else -> "סיים"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                    Text("יוצר קוד QR עם קוד הסנכרון של המכשיר", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("סורק את קוד ה-QR ומתחבר לאותו סנכרון", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onCodeScanned: (String) -> Unit,
    scanError: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isSourceDevice) "שלב 2: הצג את קוד ה-QR" else "שלב 2: סרוק את קוד הזיווג",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isSourceDevice) "פתח את המצלמה במכשיר החדש וסרוק את קוד ה-QR שלהלן:" else "כוון את מצלמת המכשיר לקוד שמוצג במכשיר הישן, או הקלד את הקוד ידנית:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        if (isSourceDevice) {
            val qrBitmap = remember(pairingCode) { QrCodeGenerator.generate(pairingCode, 512) }
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
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "קוד QR לסנכרון",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = pairingCode,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        } else {
            QrScannerView(onCodeScanned = onCodeScanned)

            Spacer(Modifier.height(16.dp))

            if (scanError != null) {
                Text(
                    text = scanError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = "או הקלד קוד זיווג ידני:",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = enteredCode,
                onValueChange = onCodeChange,
                placeholder = { Text("לדוגמה: MIG482") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Real camera preview with live QR decoding via CameraX + ML Kit. Falls back
 * to a message (not a fake "scanning..." animation) if camera permission
 * hasn't been granted — permission is requested up front in PermissionScreen.
 */
@Composable
fun QrScannerView(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasCameraPermission) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(
                    "נדרשת הרשאת מצלמה כדי לסרוק. אפשר להעניק אותה במסך ההרשאות, או להקליד את הקוד ידנית למטה.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val analysisExecutor = Executors.newSingleThreadExecutor()
                val analyzer = QrCodeAnalyzer { code -> onCodeScanned(code) }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { it.setAnalyzer(analysisExecutor, analyzer) }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        // Camera unavailable (e.g. emulator without one) — the
                        // manual code entry field below still works.
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun Step3Verifying(isSourceDevice: Boolean, isConnected: Boolean, isCloudAvailable: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        if (isConnected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "מחובר לסנכרון בזמן אמת",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "שני המכשירים מסונכרנים כעת דרך אותו קוד. שינויים ברשימת המעבר יתעדכנו בשני הצדדים.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (!isCloudAvailable) "שירות הענן אינו זמין" else "מתחבר לסנכרון...",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
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
            text = "המכשירים מסונכרנים!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "שני המכשירים מחוברים כעת לאותו קוד סנכרון. התקדמות ברשימת המעבר תתעדכן בזמן אמת בשניהם.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
