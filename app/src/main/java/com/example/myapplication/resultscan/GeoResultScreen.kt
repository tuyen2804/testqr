package com.example.myapplication.resultscan

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
fun GeoResultScreen(result: String, qrImageUri: Uri?) {
    val context = LocalContext.current

    val geoInfo = parseGeoContent(result)
    val displayText = buildDisplayText(geoInfo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2F36))
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Location Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                ButtonData(R.drawable.location, "Show on map") {
                    // Mở ứng dụng bản đồ với tọa độ
                    val uri = Uri.parse("geo:${geoInfo.latitude},${geoInfo.longitude}${geoInfo.query?.let { "?q=$it" } ?: ""}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(Intent.createChooser(intent, "Show on Map"))
                },
                ButtonData(R.drawable.send, "Navigation") {
                    // Mở điều hướng đến tọa độ
                    val uri = Uri.parse("google.navigation:q=${geoInfo.latitude},${geoInfo.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(Intent.createChooser(intent, "Navigate to Location"))
                },
                ButtonData(R.drawable.copy, "Copy") {
                    // Sao chép thông tin tọa độ
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Geo Location", displayText)
                    clipboard.setPrimaryClip(clip)
                },
                ButtonData(R.drawable.share, "Share") {
                    // Chia sẻ thông tin tọa độ
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, displayText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Location"))
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

data class GeoInfo(
    val latitude: Double,
    val longitude: Double,
    val query: String? = null
)

private fun parseGeoContent(result: String): GeoInfo {
    if (result.startsWith("geo:", ignoreCase = true)) {
        val parts = result.removePrefix("geo:").split(",", "?", limit = 3)
        val latitude = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val longitude = parts.getOrNull(1)?.substringBefore("?")?.toDoubleOrNull() ?: 0.0
        val query = parts.getOrNull(2)?.substringAfter("q=")?.trim()
        return GeoInfo(latitude, longitude, query)
    }
    return GeoInfo(0.0, 0.0, result)
}

private fun buildDisplayText(geoInfo: GeoInfo): String {
    val parts = mutableListOf<String>()
    parts.add("Latitude: ${geoInfo.latitude}")
    parts.add("Longitude: ${geoInfo.longitude}")
    geoInfo.query?.let { parts.add("Query: $it") }
    return parts.joinToString("\n")
}