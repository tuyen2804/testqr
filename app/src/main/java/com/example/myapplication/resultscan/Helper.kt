package com.example.myapplication.resultscan

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.OutputStream

fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap, fileName: String = "qr_${System.currentTimeMillis()}.png"): Uri? {
    val resolver = context.contentResolver
    var uri: Uri? = null

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Thư mục trong Gallery

                put(MediaStore.Images.Media.IS_PENDING, 1) // Đánh dấu là đang xử lý
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri = imageUri
        Log.d("saveBitmapToGallery", "Inserted imageUri: $imageUri")

        if (imageUri != null) {
            resolver.openOutputStream(imageUri)?.use { out: OutputStream? ->
                if (out != null && bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    Log.d("saveBitmapToGallery", "Bitmap saved successfully to $imageUri")
                } else {
                    Log.e("saveBitmapToGallery", "Failed to compress bitmap or open output stream")
                    throw Exception("Failed to write bitmap")
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // Xác nhận hoàn tất
                resolver.update(imageUri, contentValues, null, null)
            }
            android.media.MediaScannerConnection.scanFile(context, arrayOf(imageUri.path), null) { _, scanUri ->
                Log.d("saveBitmapToGallery", "Scanned file: $scanUri")
            }
            Toast.makeText(context, "Đã lưu vào Gallery", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("saveBitmapToGallery", "imageUri is null after insert")
            Toast.makeText(context, "ImageUri null, kiểm tra quyền hoặc bộ nhớ", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("saveBitmapToGallery", "Error saving bitmap: ${e.message}")
        Toast.makeText(context, "Lưu thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
    }
    return uri
}