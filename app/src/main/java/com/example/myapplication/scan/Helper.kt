package com.example.myapplication.scan


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.myapplication.model.QrCodeInfo
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrScanEntity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    isBatchScan: Boolean,
    context: android.content.Context,
    scannedQRs: MutableList<QrCodeInfo>,
    dao: QrScanDao,
    onProcessed: (Boolean, QrCodeInfo?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (isBatchScan) {
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        if (!scannedQRs.any { it.value == rawValue }) {
                            val qrUri = extractQRImage(imageProxy, barcode, context)
                            val qrInfo = QrCodeInfo(
                                value = rawValue,
                                type = barcode.valueType,
                                imageUri = qrUri?.toString()
                            )
                            scannedQRs.add(qrInfo)
                            CoroutineScope(Dispatchers.IO).launch {
                                dao.insert(
                                    QrScanEntity(
                                        value = rawValue,
                                        type = barcode.valueType,
                                        imagePath = qrUri?.path ?: ""
                                    )
                                )
                            }
                            onProcessed(true, qrInfo)
                        }
                    }
                    onProcessed(true, null)
                } else {
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        val rawValue = barcode.rawValue ?: return@addOnSuccessListener
                        if (!scannedQRs.any { it.value == rawValue }) {
                            val qrUri = extractQRImage(imageProxy, barcode, context)
                            val qrInfo = QrCodeInfo(
                                value = rawValue,
                                type = barcode.valueType,
                                imageUri = qrUri?.toString()
                            )
                            scannedQRs.add(qrInfo)
                            CoroutineScope(Dispatchers.IO).launch {
                                dao.insert(
                                    QrScanEntity(
                                        value = rawValue,
                                        type = barcode.valueType,
                                        imagePath = qrUri?.path ?: ""
                                    )
                                )
                            }
                            onProcessed(false, qrInfo)
                        }
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

@OptIn(ExperimentalGetImage::class)
private fun extractQRImage(imageProxy: ImageProxy, barcode: Barcode, context: android.content.Context): Uri? {
    try {
        val bitmap = imageProxyToBitmap(imageProxy) ?: return null
        val boundingBox = barcode.boundingBox ?: return null

        // Kiểm tra giới hạn cắt để tránh kích thước không hợp lệ
        val left = boundingBox.left.coerceAtLeast(0)
        val top = boundingBox.top.coerceAtLeast(0)
        val width = boundingBox.width().coerceAtMost(bitmap.width - left)
        val height = boundingBox.height().coerceAtMost(bitmap.height - top)

        if (width <= 0 || height <= 0) {
            Log.e("QrScanner", "Kích thước vùng cắt không hợp lệ: width=$width, height=$height")
            return null
        }

        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
        val imagePath = saveBitmapToInternalStorage(context, croppedBitmap) ?: return null
        return Uri.fromFile(File(imagePath))
    } catch (e: Exception) {
        Log.e("QrScanner", "Lỗi khi cắt hình ảnh QR", e)
        return null
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val image = imageProxy.image ?: return null
    if (image.format != ImageFormat.YUV_420_888) return null

    // Sử dụng toBitmap() để chuyển đổi YUV sang Bitmap
    val bitmap = imageProxy.toBitmap() ?: return null

    // Áp dụng xoay nếu cần
    val matrix = android.graphics.Matrix().apply {
        postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

// Hàm mở rộng ImageProxy sang Bitmap
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toBitmap(): Bitmap? {
    val image = this.image ?: return null
    val yuvImage = android.graphics.YuvImage(
        this.toNv21(),
        ImageFormat.NV21,
        image.width,
        image.height,
        null
    )
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
    val jpegBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun ImageProxy.toNv21(): ByteArray {
    val image = this.image ?: throw IllegalStateException("Image is null")
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    return nv21
}
private fun saveBitmapToInternalStorage(context: android.content.Context, bitmap: Bitmap): String? {
    return try {
        val filename = "qr_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle() // Giải phóng bitmap
        file.absolutePath
    } catch (e: Exception) {
        Log.e("QrScanner", "Lỗi khi lưu bitmap", e)
        null
    }
}

// Hàm xử lý ảnh từ URI (gallery)
suspend fun processImageFromUri(
    uri: Uri,
    context: android.content.Context,
    isBatchScan: Boolean,
    scannedQRs: MutableList<QrCodeInfo>,
    dao: QrScanDao,
    onProcessed: (QrCodeInfo?) -> Unit
) {
    try {
        val scanner = BarcodeScanning.getClient()
        val image = InputImage.fromFilePath(context, uri)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes[0] // Lấy mã đầu tiên
                    val rawValue = barcode.rawValue ?: return@addOnSuccessListener
                    if (!scannedQRs.any { it.value == rawValue }) {
                        val qrInfo = QrCodeInfo(
                            value = rawValue,
                            type = barcode.valueType,
                            imageUri = uri.toString() // Sử dụng URI của ảnh gốc
                        )
                        scannedQRs.add(qrInfo)
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.insert(
                                QrScanEntity(
                                    value = rawValue,
                                    type = barcode.valueType,
                                    imagePath = uri.toString()
                                )
                            )
                        }
                        onProcessed(qrInfo)
                    } else {
                        onProcessed(null) // QR đã tồn tại
                    }
                } else {
                    onProcessed(null) // Không tìm thấy mã QR
                }
            }
            .addOnFailureListener { e ->
                Log.e("QrScannerScreen", "Lỗi quét ảnh từ gallery: ${e.message}")
                onProcessed(null)
            }
    } catch (e: Exception) {
        Log.e("QrScannerScreen", "Lỗi xử lý ảnh từ gallery: ${e.message}")
        onProcessed(null)
    }
}

// Inline function để xử lý deprecation
@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraSafely(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableArrayListExtra(key, T::class.java)
    } else {
        this.getParcelableArrayListExtra(key)
    }
}