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
fun TextResultScreen(result: String, qrImageUri: Uri?) {
    val context = LocalContext.current
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
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Text Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Text",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
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
            // Result text display
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
                    text = result,
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
                ButtonData(R.drawable.web, "Web search") {
                    val searchUrl = "https://www.google.com/search?q=${Uri.encode(result)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                    context.startActivity(intent)
                },
                ButtonData(R.drawable.send, "Send SMS") {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:")
                        putExtra("sms_body", result)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send SMS"))
                },
                ButtonData(R.drawable.send, "Send email") {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_SUBJECT, "QR Code Content")
                        putExtra(Intent.EXTRA_TEXT, result)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                },
                ButtonData(R.drawable.copy, "Copy") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("QR Code Content", result)
                    clipboard.setPrimaryClip(clip)
                },
                ButtonData(R.drawable.share, "Share") {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, result)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
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

            // Display scanned QR image
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