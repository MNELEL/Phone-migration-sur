package com.example.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Real QR/barcode decoding for a CameraX ImageAnalysis use case, backed by
 * Google ML Kit's on-device barcode scanner. Decodes actual camera frames —
 * this is not a mock or a delay-based simulation.
 *
 * [onCodeDetected] is invoked on a background thread with the first
 * successfully decoded value. Call [reset] to resume scanning after handling
 * a detected code (e.g. once the user leaves the confirmation step).
 */
class QrCodeAnalyzer(
    private val onCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    @Volatile private var isPaused = false

    fun reset() {
        isPaused = false
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                if (value != null && !isPaused) {
                    isPaused = true
                    onCodeDetected(value)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
