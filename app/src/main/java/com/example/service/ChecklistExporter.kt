package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.domain.ChecklistItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChecklistExporter {
    private const val TAG = "ChecklistExporter"

    enum class ExportFormat {
        PDF,
        TEXT
    }

    data class ExportResult(
        val file: File,
        val uri: Uri,
        val format: ExportFormat,
        val itemCount: Int,
        val completedCount: Int
    )

    fun exportChecklist(
        context: Context,
        items: List<ChecklistItem>,
        format: ExportFormat,
        onlyCompleted: Boolean = false
    ): ExportResult? {
        val filteredItems = if (onlyCompleted) items.filter { it.completed } else items
        val completedCount = filteredItems.count { it.completed }
        val totalCount = filteredItems.size

        return try {
            val file = when (format) {
                ExportFormat.PDF -> generatePdfFile(context, filteredItems, totalCount, completedCount)
                ExportFormat.TEXT -> generateTextFile(context, filteredItems, totalCount, completedCount)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ExportResult(
                file = file,
                uri = uri,
                format = format,
                itemCount = totalCount,
                completedCount = completedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting checklist: ${e.message}", e)
            null
        }
    }

    private fun generateTextFile(
        context: Context,
        items: List<ChecklistItem>,
        totalCount: Int,
        completedCount: Int
    ): File {
        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        if (!exportDir.exists()) exportDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val file = File(exportDir, "Migration_Checklist_$timestamp.txt")

        val percentage = if (items.isNotEmpty()) (completedCount * 100) / items.size else 0

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("      PHONE MIGRATION CHECKLIST RECORD / דוח מעבר")
        sb.appendLine("==================================================")
        sb.appendLine("Date Generated: $dateString")
        sb.appendLine("Total Items: ${items.size}")
        sb.appendLine("Completed Items: $completedCount ($percentage%)")
        sb.appendLine("==================================================")
        sb.appendLine()

        val completedItems = items.filter { it.completed }
        val pendingItems = items.filter { !it.completed }

        if (completedItems.isNotEmpty()) {
            sb.appendLine("--------------------------------------------------")
            sb.appendLine(" COMPLETED TASKS (${completedItems.size})")
            sb.appendLine("--------------------------------------------------")
            completedItems.forEachIndexed { index, item ->
                sb.appendLine("${index + 1}. [✓] ${item.title}")
                sb.appendLine("    Category: ${item.category} | Source: ${item.source}")
                if (item.instruction.isNotBlank()) {
                    sb.appendLine("    Details: ${item.instruction}")
                }
                if (!item.action.isNullOrBlank()) {
                    sb.appendLine("    Action: ${item.action}")
                }
                if (item.size > 0) {
                    val formattedSize = if (item.size > 1024 * 1024) "${item.size / (1024 * 1024)} MB" else "${item.size / 1024} KB"
                    sb.appendLine("    Size: $formattedSize")
                }
                sb.appendLine()
            }
        }

        if (pendingItems.isNotEmpty()) {
            sb.appendLine("--------------------------------------------------")
            sb.appendLine(" PENDING TASKS (${pendingItems.size})")
            sb.appendLine("--------------------------------------------------")
            pendingItems.forEachIndexed { index, item ->
                sb.appendLine("${index + 1}. [ ] ${item.title}")
                sb.appendLine("    Category: ${item.category} | Source: ${item.source}")
                if (item.instruction.isNotBlank()) {
                    sb.appendLine("    Details: ${item.instruction}")
                }
                if (!item.action.isNullOrBlank()) {
                    sb.appendLine("    Action: ${item.action}")
                }
                sb.appendLine()
            }
        }

        sb.appendLine("==================================================")
        sb.appendLine("Generated by PhoneMigrate App")
        sb.appendLine("==================================================")

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    private fun generatePdfFile(
        context: Context,
        items: List<ChecklistItem>,
        totalCount: Int,
        completedCount: Int
    ): File {
        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        if (!exportDir.exists()) exportDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val file = File(exportDir, "Migration_Checklist_$timestamp.pdf")

        val pdfDocument = PdfDocument()

        // Page specifications (A4 width=595, height=842)
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#1F1B16")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#53433F")
            textSize = 12f
            isAntiAlias = true
        }

        val sectionPaint = TextPaint().apply {
            color = Color.parseColor("#2C6B2F")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val itemTitlePaint = TextPaint().apply {
            color = Color.parseColor("#1C1B1F")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#49454F")
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E7E0EC")
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F7F2FA")
        }

        val checkPaint = TextPaint().apply {
            color = Color.parseColor("#2E7D32")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val uncheckPaint = TextPaint().apply {
            color = Color.parseColor("#B3261E")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var y = 40f

        // Draw Top Header Banner
        canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 110f, headerBgPaint)
        
        canvas.drawText("Phone Migration Checklist / דוח מעבר מידע", 35f, y + 10f, titlePaint)
        y += 35f
        canvas.drawText("Generated: $dateString", 35f, y + 5f, subtitlePaint)
        y += 20f

        val percentage = if (items.isNotEmpty()) (completedCount * 100) / items.size else 0
        canvas.drawText("Progress: $completedCount / ${items.size} tasks completed ($percentage%)", 35f, y + 5f, subtitlePaint)
        
        y = 130f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, linePaint)
        y += 20f

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > pageHeight - 40) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
        }

        items.forEachIndexed { index, item ->
            checkPageBreak(50f)

            val isDone = item.completed
            val statusText = if (isDone) "[✓] COMPLETED" else "[ ] PENDING"
            val statusPaint = if (isDone) checkPaint else uncheckPaint

            canvas.drawText(statusText, 35f, y, statusPaint)
            
            // Item Title
            val titleText = "${index + 1}. ${item.title}"
            canvas.drawText(titleText, 140f, y, itemTitlePaint)
            y += 16f

            // Category & Source
            val metaText = "Category: ${item.category}  |  Source: ${item.source}"
            canvas.drawText(metaText, 140f, y, bodyPaint)
            y += 14f

            // Instruction if present
            if (item.instruction.isNotBlank()) {
                val detailText = "Details: ${item.instruction}"
                val staticLayout = StaticLayout.Builder.obtain(
                    detailText, 0, detailText.length, bodyPaint, pageWidth - 175
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(140f, y - 10f)
                staticLayout.draw(canvas)
                canvas.restore()

                y += staticLayout.height + 4f
            }

            y += 8f
            canvas.drawLine(35f, y, (pageWidth - 35).toFloat(), y, linePaint)
            y += 12f
        }

        // Footer on last page
        checkPageBreak(30f)
        canvas.drawText("PhoneMigrate Official Summary Report", 35f, pageHeight - 30f, subtitlePaint)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun shareExportedFile(context: Context, result: ExportResult) {
        val mimeType = when (result.format) {
            ExportFormat.PDF -> "application/pdf"
            ExportFormat.TEXT -> "text/plain"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, result.uri)
            putExtra(Intent.EXTRA_SUBJECT, "Phone Migration Checklist Record")
            putExtra(Intent.EXTRA_TEXT, "Attached is my Phone Migration Checklist record (${result.completedCount}/${result.itemCount} completed).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "שתף דוח מעבר מידע")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openExportedFile(context: Context, result: ExportResult) {
        val mimeType = when (result.format) {
            ExportFormat.PDF -> "application/pdf"
            ExportFormat.TEXT -> "text/plain"
        }

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(result.uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // Fallback to share intent if no default viewer app is found
            shareExportedFile(context, result)
        }
    }
}
