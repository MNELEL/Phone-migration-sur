package com.example.service

import com.example.domain.ScanReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val pairingCode: String? = null,
    val totalAppsPackaged: Int = 0,
    val totalContactsPackaged: Int = 0,
    val totalMediaMBPackaged: Long = 0,
    val targetDeviceConnected: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Drives the transfer flow off real data:
 *  - app/contact/media counts come from the ScanReport already produced by DeepScanner
 *  - the "upload" step pushes real checklist-completion state to Firestore via CloudSyncService
 *  - the "target device connected" step is a real Firestore listener, not a simulated button
 *
 * Scope note: this syncs *metadata and completion state* (counts, names, package IDs,
 * checked/unchecked flags) — it does not transfer the underlying files themselves (photos,
 * contact records, APKs). See AppListScreen for the real per-app Play Store install flow.
 */
class MigrationOrchestrator(private val cloudSyncService: CloudSyncService) {
    private val _state = MutableStateFlow(OrchestratorState())
    val state = _state.asStateFlow()

    fun startTransferFlow(
        scope: CoroutineScope,
        report: ScanReport?,
        syncCode: String,
        provider: String = "Google Drive"
    ) {
        scope.launch(Dispatchers.Main) {
            _state.value = OrchestratorState(
                stage = OrchestratorStage.PACKAGING_DATA,
                progress = 0.15f,
                statusMessage = "אוסף נתוני מכשיר אמיתיים (אפליקציות, אנשי קשר ומדיה)...",
                destinationProvider = provider,
                pairingCode = syncCode
            )

            val appCount = report?.apps?.size ?: 0
            val contactCount = report?.contacts?.total ?: 0
            val mediaMB = (report?.media?.totalSize ?: 0L) / (1024 * 1024)

            _state.value = _state.value.copy(
                progress = 0.4f,
                statusMessage = "נמצאו $appCount אפליקציות, $contactCount אנשי קשר ו-${mediaMB}MB מדיה",
                totalAppsPackaged = appCount,
                totalContactsPackaged = contactCount,
                totalMediaMBPackaged = mediaMB
            )

            // Real upload: push checklist/completion state to Firestore.
            _state.value = _state.value.copy(
                stage = OrchestratorStage.UPLOADING_CLOUD,
                progress = 0.6f,
                statusMessage = "מעלה מצב סנכרון ל-$provider..."
            )
            cloudSyncService.pushLocalStateToCloud(syncCode, scope)

            _state.value = _state.value.copy(
                progress = 0.8f,
                statusMessage = "מצב הסנכרון הועלה. ממתין לחיבור מהמכשיר החדש..."
            )

            _state.value = _state.value.copy(
                stage = OrchestratorStage.PROMPTING_TARGET_INSTALL,
                progress = 0.85f,
                statusMessage = "התקן את האפליקציה במכשיר החדש והזן את קוד הסנכרון: $syncCode",
                pairingCode = syncCode
            )

            // Real listener: fires only when the *other device* actually writes
            // a completed state back to the same Firestore document — not a
            // button that pretends a connection happened.
            cloudSyncService.startRealtimeSync(syncCode, scope) {
                onTargetDeviceSynced(scope)
            }
        }
    }

    private fun onTargetDeviceSynced(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            if (_state.value.stage != OrchestratorStage.PROMPTING_TARGET_INSTALL &&
                _state.value.stage != OrchestratorStage.RECOVERING_DATA
            ) return@launch

            _state.value = _state.value.copy(
                targetDeviceConnected = true,
                stage = OrchestratorStage.COMPLETED,
                progress = 1.0f,
                statusMessage = "הסנכרון בין המכשירים הושלם בהצלחה!"
            )
        }
    }

    fun reset() {
        cloudSyncService.stopSync()
        _state.value = OrchestratorState()
    }
}
