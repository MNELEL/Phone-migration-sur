package com.example.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OrchestratorStage {
    IDLE,
    PACKAGING_DATA,
    UPLOADING_CLOUD,
    PROMPTING_TARGET_INSTALL,
    RECOVERING_DATA,
    COMPLETED,
    FAILED
}

data class OrchestratorState(
    val stage: OrchestratorStage = OrchestratorStage.IDLE,
    val progress: Float = 0f,
    val statusMessage: String = "מוכן לתחילת תהליך מעבר",
    val destinationProvider: String = "Google Drive",
    val pairingCode: String = "MIG-9842-X",
    val totalAppsPackaged: Int = 0,
    val totalContactsPackaged: Int = 0,
    val totalMediaMBPackaged: Long = 0,
    val targetDeviceConnected: Boolean = false,
    val errorMessage: String? = null
)

class MigrationOrchestrator {
    private val _state = MutableStateFlow(OrchestratorState())
    val state = _state.asStateFlow()

    fun startTransferFlow(scope: CoroutineScope, provider: String = "Google Drive") {
        scope.launch(Dispatchers.Main) {
            _state.value = OrchestratorState(
                stage = OrchestratorStage.PACKAGING_DATA,
                progress = 0.1f,
                statusMessage = "אורז נתוני מכשיר (אפליקציות, אנשי קשר ומדיה)...",
                destinationProvider = provider
            )

            // Step 1: Packaging
            delay(1200)
            _state.value = _state.value.copy(
                progress = 0.35f,
                statusMessage = "אורז ומצפין רשימת אפליקציות ומדיה...",
                totalAppsPackaged = 18,
                totalContactsPackaged = 340,
                totalMediaMBPackaged = 2450L
            )

            // Step 2: Uploading
            delay(1500)
            _state.value = _state.value.copy(
                stage = OrchestratorStage.UPLOADING_CLOUD,
                progress = 0.65f,
                statusMessage = "מעלה חבילת מעבר מוצפנת ל-$provider..."
            )

            delay(1800)
            _state.value = _state.value.copy(
                progress = 0.85f,
                statusMessage = "העלאה ל-$provider הושלמה בהצלחה!"
            )

            // Step 3: Prompt target device install
            delay(1000)
            val generatedCode = "MIG-" + (1000..9999).random() + "-X"
            _state.value = _state.value.copy(
                stage = OrchestratorStage.PROMPTING_TARGET_INSTALL,
                progress = 0.9f,
                statusMessage = "אנא התקן את האפליקציה במכשיר החדש והסרק את קוד ה-QR להמשך שחזור.",
                pairingCode = generatedCode
            )
        }
    }

    fun simulateTargetConnection(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(
                targetDeviceConnected = true,
                stage = OrchestratorStage.RECOVERING_DATA,
                progress = 0.95f,
                statusMessage = "מכשיר חדש חובר! מתחיל בהורדה ושחזור נתונים..."
            )

            delay(2000)
            _state.value = _state.value.copy(
                stage = OrchestratorStage.COMPLETED,
                progress = 1.0f,
                statusMessage = "תהליך המעבר והשחזור הושלם בהצלחה!"
            )
        }
    }

    fun reset() {
        _state.value = OrchestratorState()
    }
}
