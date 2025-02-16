package com.library.editinglibrary.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generatePdfAndSave(
    context: Context,
    bitmaps: List<Bitmap>,
    onStatusChange: (String) -> Unit
) {
    if (bitmaps.isEmpty()) {
        onStatusChange("No bitmaps provided to generate PDF.")
        return
    }

    val pdfDocument = PdfDocument()


    bitmaps.forEachIndexed { index, bitmap ->
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null) // Draw bitmap at top-left
        pdfDocument.finishPage(page)

    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val pdfFileName = "PDF$timeStamp.pdf"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Using MediaStore for Android 10 and above
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Corrected line!
            }

            val pdfUri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            pdfUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            } ?: run {
                throw IOException("Failed to create MediaStore output stream.")
            }
            onStatusChange("PDF saved to Downloads folder (MediaStore)")

        }
        else {
            // Using FileOutputStream for older versions (Example to Documents folder)
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!directory.exists()) {
                directory.mkdirs() // Create directory if it doesn't exist
            }
            val pdfFile = File(directory, pdfFileName)
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            onStatusChange("PDF saved to Documents folder")
        }


    } catch (e: IOException) {
        Log.e("PdfGenerator", "Error saving PDF", e)
        onStatusChange("Error saving PDF: ${e.localizedMessage}")
    } finally {
        pdfDocument.close()
    }
}