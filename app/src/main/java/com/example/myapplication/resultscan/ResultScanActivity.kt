package com.example.myapplication.resultscan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.mlkit.vision.barcode.common.Barcode

class ResultScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("testbatch", "onCreate: "+"111")
        val result = intent.getStringExtra("scan_result") ?: ""
        val type = intent.getIntExtra("scan_type", Barcode.TYPE_TEXT)
        val qrImageUri = intent.getStringExtra("qr_image_uri")?.let { Uri.parse(it) }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (type) {
                            Barcode.TYPE_TEXT -> TextResultScreen(result,qrImageUri)
                            Barcode.TYPE_URL -> UrlResultScreen(result,qrImageUri)
                            Barcode.TYPE_CONTACT_INFO -> ContactResultScreen(result,qrImageUri)
                            Barcode.TYPE_EMAIL -> EmailResultScreen(result,qrImageUri)
                            Barcode.TYPE_PHONE -> PhoneResultScreen(result,qrImageUri)
                            Barcode.TYPE_SMS -> SmsResultScreen(result,qrImageUri)
                            Barcode.TYPE_WIFI -> WifiResultScreen(result,qrImageUri)
                            Barcode.TYPE_DRIVER_LICENSE -> DriverLicenseScreen(result,qrImageUri)
                            Barcode.TYPE_GEO -> GeoResultScreen(result,qrImageUri)
                            Barcode.TYPE_PRODUCT -> ProductResultScreen(result,qrImageUri)
                            Barcode.TYPE_CALENDAR_EVENT -> CalendarEventResultScreen(result,qrImageUri)
                            else -> TextResultScreen(result,qrImageUri)
                        }
                    }
                }
            }
        }
    }
}
