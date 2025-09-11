package com.example.myapplication.resultscan

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import java.io.OutputStream

fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap, fileName: String = "qr_${System.currentTimeMillis()}.png"): Uri? {
    val resolver = context.contentResolver
    var uri: Uri? = null

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QRScanner") // Thư mục trong Gallery
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri = imageUri

        if (imageUri != null) {
            resolver.openOutputStream(imageUri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Toast.makeText(context, "Đã lưu vào Gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Lưu thất bại", Toast.LENGTH_SHORT).show()
    }
    return uri
}
