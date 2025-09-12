package com.example.myapplication.resultscan

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.resultscan.widget.ButtonData
import com.example.myapplication.resultscan.widget.CustomButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiResultScreen(result: String, qrImageUri: Uri?) {
    val context = LocalContext.current

    val wifiInfo = parseWifiContent(result)
    val displayText = buildDisplayText(wifiInfo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2F36))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.wifi),
                        contentDescription = "Wi-Fi Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wi-Fi",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    // Quay lại màn hình trước đó
                    (context as? Activity)?.finish()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Handle help */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.help),
                        contentDescription = "Help",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Handle favorite */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2C2F36)
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Wi-Fi display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3A3D44)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = displayText,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val buttonList = listOf(
                ButtonData(R.drawable.wifi, "Connect to Wi-Fi") {
                    val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                    context.startActivity(Intent.createChooser(intent, "Connect to Wi-Fi"))
                },
                ButtonData(R.drawable.copy, "Copy password") {
                    wifiInfo.password?.let { password ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Wi-Fi Password", password)
                        clipboard.setPrimaryClip(clip)
                    }
                },
                ButtonData(R.drawable.copy, "Copy") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Wi-Fi Info", displayText)
                    clipboard.setPrimaryClip(clip)
                },
                ButtonData(R.drawable.share, "Share") {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, displayText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Wi-Fi"))
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(buttonList) { button ->
                    CustomButton(
                        painter = painterResource(button.iconRes),
                        text = button.text,
                        onClick = button.onClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            qrImageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            } ?: Text(
                text = "Unable to display QR code",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Feedback or suggestion",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class WifiInfo(
    val ssid: String? = null,
    val securityType: String? = null,
    val password: String? = null
)

private fun parseWifiContent(result: String): WifiInfo {
    if (result.startsWith("WIFI:", ignoreCase = true)) {
        val parts = result.removePrefix("WIFI:").split(";")
        var ssid: String? = null
        var securityType: String? = null
        var password: String? = null

        for (part in parts) {
            when {
                part.startsWith("S:", ignoreCase = true) -> ssid = part.removePrefix("S:").trim()
                part.startsWith("T:", ignoreCase = true) -> securityType = part.removePrefix("T:").trim()
                part.startsWith("P:", ignoreCase = true) -> password = part.removePrefix("P:").trim()
            }
        }
        return WifiInfo(ssid, securityType, password)
    }
    return WifiInfo(result, null, null)
}

private fun buildDisplayText(wifiInfo: WifiInfo): String {
    val parts = mutableListOf<String>()
    wifiInfo.ssid?.let { parts.add("SSID: $it") }
    wifiInfo.securityType?.let { parts.add("Security: $it") }
    wifiInfo.password?.let { parts.add("Password: $it") }
    return if (parts.isNotEmpty()) parts.joinToString("\n") else "No Wi-Fi information"
}