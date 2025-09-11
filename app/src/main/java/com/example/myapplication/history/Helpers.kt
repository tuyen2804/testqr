package com.example.myapplication.history

import com.google.mlkit.vision.barcode.common.Barcode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun typeToString(type: Int): String = when (type) {
    0 -> "Text"
    1 -> "URL"
    2 -> "Product"
    else -> "Other"
}

fun formatDate(millis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(millis))
}

fun isToday(date: String): Boolean {
    val today = formatDate(System.currentTimeMillis(), "yyyy-MM-dd")
    return date == today
}

fun formatDateLabel(date: String): String {
    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfOutput = SimpleDateFormat("EEE", Locale.getDefault())
    val parsed = sdfInput.parse(date)
    return parsed?.let { sdfOutput.format(it) } ?: date
}
fun getBarcodeTypeString(type: Int): String {
    return when (type) {
        // Barcode formats
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_CODE_93 -> "Code 93"
        Barcode.FORMAT_CODABAR -> "Codabar"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_ALL_FORMATS -> "All Formats"
        Barcode.FORMAT_UNKNOWN -> "Unknown Format"

        Barcode.TYPE_CONTACT_INFO -> "Contact Info"
        Barcode.TYPE_EMAIL -> "Email"
        Barcode.TYPE_ISBN -> "ISBN"
        Barcode.TYPE_PHONE -> "Phone"
        Barcode.TYPE_PRODUCT -> "Product"
        Barcode.TYPE_SMS -> "SMS"
        Barcode.TYPE_TEXT -> "Text"
        Barcode.TYPE_URL -> "URL"
        Barcode.TYPE_WIFI -> "WiFi"
        Barcode.TYPE_GEO -> "Geo Location"
        Barcode.TYPE_CALENDAR_EVENT -> "Calendar Event"
        Barcode.TYPE_DRIVER_LICENSE -> "Driver License"
        Barcode.TYPE_UNKNOWN -> "Unknown"

        else -> "Unknown"
    }
}