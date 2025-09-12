package com.example.myapplication.createqr

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme

class CreateActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lấy screenName từ intent (ví dụ: "url", "wifi", "text")
        val screenName = intent.getStringExtra("screen") ?: "url"

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CreateScreenRouter(screenName)
                }

            }
        }
    }
}


@Composable
fun CreateScreenRouter(screen: String) {
    when (screen.lowercase()) {
        "url" -> CreateUrlScreen()
        "wifi" -> CreateWifiScreen()
        "text" -> CreateTextScreen()
        "contacts" -> CreateContactsScreen()
        "email" -> CreateEmailScreen()
        "tel" -> CreateTelScreen()
        "sms" -> CreateSmsScreen()
        "calendar" -> CreateCalendarScreen()
        "mycard" -> CreateMyCardScreen()
        "facebook" -> CreateFacebookScreen()
        "instagram" -> CreateInstagramScreen()
        "paypal" -> CreatePaypalScreen()
        "spotify" -> CreateSpotifyScreen()
        "viber" -> CreateViberScreen()
        "whatsapp" -> CreateWhatsappScreen()
        "youtube" -> CreateYoutubeScreen()
        "twitter" -> CreateXScreen()
        else -> CreateUrlScreen() // mặc định
    }
}
